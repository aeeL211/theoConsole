package com.aeeL.theokntl.data.repository

import com.aeeL.theokntl.core.ConsoleConstants
import com.aeeL.theokntl.core.ConsoleException
import com.aeeL.theokntl.core.CryptoEngine
import com.aeeL.theokntl.core.ErrorKind
import com.aeeL.theokntl.core.Formatters
import com.aeeL.theokntl.core.isCancellation
import com.aeeL.theokntl.data.log.SessionLog
import com.aeeL.theokntl.data.network.HttpTransport
import com.aeeL.theokntl.data.network.TheoClient
import com.aeeL.theokntl.data.network.TheoHooks
import com.aeeL.theokntl.data.store.AppPreferences
import com.aeeL.theokntl.domain.feature.FeatureSync
import com.aeeL.theokntl.domain.feature.PurchaseFeatures
import com.aeeL.theokntl.domain.feature.PurchaseGateway
import com.aeeL.theokntl.domain.gateway.ConsoleGateway
import com.aeeL.theokntl.domain.model.AccountField
import com.aeeL.theokntl.domain.model.AccountRow
import com.aeeL.theokntl.domain.model.AccountView
import com.aeeL.theokntl.domain.model.ActiveSession
import com.aeeL.theokntl.domain.model.BusyOperation
import com.aeeL.theokntl.domain.model.ConsoleState
import com.aeeL.theokntl.domain.model.EventTotals
import com.aeeL.theokntl.domain.model.FeatureReport
import com.aeeL.theokntl.domain.model.FeatureStatus
import com.aeeL.theokntl.domain.model.LoginResult
import com.aeeL.theokntl.domain.model.PurchaseItem
import com.aeeL.theokntl.domain.model.SessionStatus
import com.aeeL.theokntl.domain.model.SessionView
import com.aeeL.theokntl.domain.model.StatsView
import com.aeeL.theokntl.domain.model.TopUpOutcome
import com.aeeL.theokntl.domain.model.TopUpReview
import com.aeeL.theokntl.domain.usecase.TopUpMath
import com.aeeL.theokntl.domain.validation.AmountError
import com.aeeL.theokntl.domain.validation.AmountValidator
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class ConsoleRepository @Inject constructor(
    private val transport: HttpTransport,
    private val crypto: CryptoEngine,
    private val preferences: AppPreferences,
    private val log: SessionLog,
) : ConsoleGateway {
    private val hooks = TheoHooks(
        onAction = log::action,
        onResponse = log::response,
        onFailure = log::failure,
    )

    private val _state = MutableStateFlow(ConsoleState())
    override val state: StateFlow<ConsoleState> = _state.asStateFlow()

    private val activeOperation = AtomicReference<BusyOperation?>(null)
    private var active: ActiveSession? = null
    private var totals: EventTotals? = null
    private var pending: PendingTopUp? = null
    private var lastTopUp: TopUpOutcome? = null
    private var features: FeatureReport? = null
    private var loginTime: String? = null
    private var lastAction: String? = null

    override suspend fun login(username: String, password: String, remember: Boolean): LoginResult {
        if (active != null && totals != null) {
            throw ConsoleException(ErrorKind.STATE, "Already signed in. Sign out first.")
        }
        val cleanUsername = username.trim()
        if (cleanUsername.isEmpty()) {
            throw ConsoleException(ErrorKind.INPUT, "A forum username is required.")
        }
        if (password.isEmpty()) {
            throw ConsoleException(ErrorKind.INPUT, "A forum password is required.")
        }
        persistRememberedUsername(cleanUsername, remember)

        begin(BusyOperation.LOGIN)
        try {
            val sealed = crypto.sealForumPassword(cleanUsername, password)
            val auth = TheoClient.verifyForum(
                username = cleanUsername,
                sealedPassword = sealed,
                transport = transport,
                crypto = crypto,
                hooks = hooks,
            )
            val data = TheoClient.extractSession(auth)
            val session = ActiveSession(
                data = data,
                login = cleanUsername,
                sealedPassword = sealed,
                keyFingerprint = crypto.keyFingerprint(data.encKey),
            )
            val snapshot = makeClient(session).snapshot()
            currentCoroutineContext().ensureActive()

            active = session
            totals = snapshot
            pending = null
            lastTopUp = null
            features = null
            loginTime = Formatters.nowTime()
            lastAction = loginTime
            publish()
            log.success("Signed in as ${accountView()?.label ?: cleanUsername}.")

            val account = accountView()
                ?: throw ConsoleException(ErrorKind.STATE, "The account state could not be built.")
            return LoginResult(
                account = account,
                stats = statsView(),
                ceiling = snapshot.balance >= ConsoleConstants.LIMIT,
            )
        } finally {
            finish(BusyOperation.LOGIN)
        }
    }

    override suspend fun refreshBalance() {
        requireSignedIn()
        begin(BusyOperation.REFRESH)
        try {
            val snapshot = currentClient().snapshot()
            currentCoroutineContext().ensureActive()
            totals = snapshot
            lastAction = Formatters.nowTime()
            publish()
        } finally {
            finish(BusyOperation.REFRESH)
        }
    }

    override fun reviewTopUp(rawAmount: String): TopUpReview {
        if (activeOperation.get() != null) {
            throw ConsoleException(ErrorKind.BUSY, "Another operation is still running.")
        }
        val session = active
        val current = totals
        if (session == null || current == null) {
            throw ConsoleException(ErrorKind.STATE, "Not signed in. Sign in first.")
        }
        val room = Formatters.roomLeft(current.balance)
        val validation = AmountValidator.validate(rawAmount, room)
        val error = validation.error
        if (error != null) throw reviewError(error, room)
        val amount = validation.value
            ?: throw ConsoleException(ErrorKind.INPUT, "The diamond amount is invalid.")
        val review = TopUpMath.review(
            totals = current,
            amount = amount,
            accountLabel = accountLabel(session),
            accountUserId = session.data.userId,
        )
        pending = PendingTopUp(
            amount = review.amount,
            earned = review.earned,
            target = review.target,
        )
        lastTopUp = null
        publish()
        return review
    }

    override fun cancelTopUp() {
        if (activeOperation.get() != null) return
        pending = null
        publish()
    }

    override suspend fun applyTopUp(): TopUpOutcome {
        if (activeOperation.get() != null) {
            throw ConsoleException(ErrorKind.BUSY, "Another operation is still running.")
        }
        val session = active
        val current = totals
        val planned = pending
        if (session == null || current == null) {
            throw ConsoleException(ErrorKind.STATE, "Not signed in. Sign in first.")
        }
        if (planned == null) {
            throw ConsoleException(ErrorKind.STATE, "There is no top-up to apply.")
        }
        pending = null
        begin(BusyOperation.TOPUP)
        try {
            val client = makeClient(session)
            client.maxEvent(ConsoleConstants.EVENT_EARNED, planned.earned)
            val snapshot = client.snapshot()
            val report = FeatureSync(
                gateway = SessionPurchaseGateway(client),
                stamps = preferences,
                nowMillis = System::currentTimeMillis,
                sleep = { delay(it) },
            ).unlock(
                userId = session.data.userId,
                watch = ConsoleConstants.PURCHASE_WATCH_MS.take(3),
            )
            currentCoroutineContext().ensureActive()

            totals = snapshot
            features = report
            lastAction = Formatters.nowTime()
            val outcome = TopUpOutcome(
                before = current.balance,
                amount = planned.amount,
                after = snapshot.balance,
                target = planned.target,
                capped = snapshot.balance < planned.target,
                features = report,
            )
            lastTopUp = outcome
            publish()
            log.success("Top-up applied: ${Formatters.number(planned.amount)} diamonds.")
            return outcome
        } finally {
            finish(BusyOperation.TOPUP)
        }
    }

    override suspend fun readPurchases(): List<FeatureStatus> {
        requireSignedIn()
        begin(BusyOperation.PURCHASES)
        try {
            val page = currentClient().getPurchases()
            val statuses = PurchaseFeatures.from(page.items)
            currentCoroutineContext().ensureActive()
            features = FeatureReport(statuses)
            lastAction = Formatters.nowTime()
            publish()
            return statuses
        } finally {
            finish(BusyOperation.PURCHASES)
        }
    }

    override suspend fun syncFeatures(): FeatureReport {
        requireSignedIn()
        begin(BusyOperation.SYNC)
        try {
            val session = active
                ?: throw ConsoleException(ErrorKind.STATE, "Not signed in. Sign in first.")
            val report = FeatureSync(
                gateway = SessionPurchaseGateway(makeClient(session)),
                stamps = preferences,
                nowMillis = System::currentTimeMillis,
                sleep = { delay(it) },
            ).unlock(
                userId = session.data.userId,
                watch = ConsoleConstants.PURCHASE_WATCH_MS,
            )
            currentCoroutineContext().ensureActive()
            features = report
            lastTopUp = lastTopUp?.copy(features = report)
            lastAction = Formatters.nowTime()
            publish()
            return report
        } finally {
            finish(BusyOperation.SYNC)
        }
    }

    override suspend fun signOut() {
        if (activeOperation.get() != null) {
            throw ConsoleException(ErrorKind.BUSY, "Another operation is still running.")
        }
        active = null
        totals = null
        pending = null
        lastTopUp = null
        features = null
        loginTime = null
        lastAction = null
        log.info("Signed out.")
        publish()
    }

    private suspend fun persistRememberedUsername(username: String, remember: Boolean) {
        try {
            if (remember) {
                preferences.setUsername(username)
            } else {
                preferences.clearUsername()
            }
        } catch (error: Exception) {
            if (error.isCancellation()) throw error
            log.info("The username preference could not be saved: ${error.message.orEmpty()}")
        }
    }

    private fun begin(operation: BusyOperation) {
        if (!activeOperation.compareAndSet(null, operation)) {
            throw ConsoleException(ErrorKind.BUSY, "Another operation is still running.")
        }
        publish()
    }

    private fun finish(operation: BusyOperation) {
        activeOperation.compareAndSet(operation, null)
        publish()
    }

    private fun publish() {
        val session = active
        val current = totals
        _state.value = ConsoleState(
            signedIn = session != null && current != null,
            busy = activeOperation.get() != null,
            busyOperation = activeOperation.get(),
            account = accountView(),
            stats = statsView(),
            session = sessionView(),
            features = features,
            lastTopUp = lastTopUp,
            loginTime = loginTime,
            lastAction = lastAction,
        )
    }

    private fun statsView(): StatsView {
        val current = totals
        if (active == null || current == null) return StatsView.signedOut()
        return StatsView(
            signedIn = true,
            balance = current.balance,
            earned = current.earned,
            spend = current.spend,
            room = Formatters.roomLeft(current.balance),
            limit = ConsoleConstants.LIMIT,
            progress = Formatters.progress(current.balance),
        )
    }

    private fun accountView(): AccountView? {
        val session = active ?: return null
        val data = session.data
        val label = accountLabel(session)
        val rows = mutableListOf<AccountRow>()
        val seen = mutableSetOf<String>()

        fun add(field: AccountField, value: String?, mono: Boolean) {
            val text = value?.trim().orEmpty()
            if (text.isEmpty() || text == "-") return
            if (!seen.add(text.lowercase(Locale.US))) return
            rows += AccountRow(field = field, value = text, mono = mono)
        }

        add(AccountField.LOGIN, session.login, mono = true)
        add(AccountField.PLAYER_NAME, data.account.name, mono = false)
        add(AccountField.FORUM_NAME, data.account.forumName, mono = false)
        var email = data.account.email
        if (email.isBlank() || email == "-") {
            if (session.login.contains("@")) email = session.login
        }
        add(AccountField.EMAIL, email, mono = true)
        rows += AccountRow(
            field = AccountField.USER_ID,
            value = data.userId.toString(),
            mono = true,
        )
        val forumId = data.account.forumId
        if (forumId != null && forumId != 0L) {
            rows += AccountRow(
                field = AccountField.FORUM_ID,
                value = forumId.toString(),
                mono = true,
            )
        }
        return AccountView(
            label = label,
            login = session.login,
            userId = data.userId,
            name = label,
            rows = rows,
            since = loginTime ?: Formatters.nowTime(),
        )
    }

    private fun sessionView(): SessionView {
        val session = active ?: return SessionView.signedOut()
        return SessionView(
            signedIn = true,
            status = if (activeOperation.get() != null) SessionStatus.BUSY else SessionStatus.READY,
            userId = session.data.userId,
            sessionBound = true,
            keyFingerprint = session.keyFingerprint
                .ifBlank { crypto.keyFingerprint(session.data.encKey) },
            endpoint = ConsoleConstants.API_HOST,
            lastAction = lastAction,
        )
    }

    private fun accountLabel(session: ActiveSession): String =
        Formatters.accountLabel(session.data.account, session.login, session.data.userId)

    private fun currentClient(): TheoClient {
        val session = active
            ?: throw ConsoleException(ErrorKind.STATE, "Not signed in. Sign in first.")
        return makeClient(session)
    }

    private fun makeClient(session: ActiveSession): TheoClient = TheoClient(
        session = session.data,
        transport = transport,
        crypto = crypto,
        hooks = hooks,
    )

    private fun requireSignedIn() {
        if (active == null || totals == null) {
            throw ConsoleException(ErrorKind.STATE, "Not signed in. Sign in first.")
        }
    }

    private fun reviewError(error: AmountError, room: Long): ConsoleException = when (error) {
        AmountError.EMPTY -> ConsoleException(ErrorKind.INPUT, "Enter a diamond amount.")
        AmountError.NOT_INTEGER ->
            ConsoleException(ErrorKind.INPUT, "The diamond amount must be a whole number.")
        AmountError.TOO_SMALL ->
            ConsoleException(ErrorKind.INPUT, "The diamond amount must be at least 1.")
        AmountError.TOO_LARGE -> ConsoleException(
            ErrorKind.INPUT,
            "The amount exceeds the remaining room (${Formatters.number(room)}).",
        )
    }

    private data class PendingTopUp(
        val amount: Long,
        val earned: Long,
        val target: Long,
    )

    private inner class SessionPurchaseGateway(
        initialClient: TheoClient,
    ) : PurchaseGateway {
        private var client: TheoClient = initialClient

        override suspend fun getPurchases(): List<PurchaseItem> = client.getPurchases().items

        override suspend fun addPurchase(id: String, amount: Long) {
            client.addPurchase(id, amount)
        }

        override suspend fun maxPurchase(id: String, amount: Long) {
            client.maxPurchase(id, amount)
        }

        override suspend fun refreshSession() {
            val session = active
                ?: throw ConsoleException(ErrorKind.STATE, "Not signed in. Sign in first.")
            val auth = TheoClient.verifyForum(
                username = session.login,
                sealedPassword = session.sealedPassword,
                transport = transport,
                crypto = crypto,
                hooks = hooks,
            )
            val data = TheoClient.extractSession(auth)
            val refreshed = session.copy(
                data = data,
                keyFingerprint = crypto.keyFingerprint(data.encKey),
            )
            active = refreshed
            client = makeClient(refreshed)
            publish()
        }
    }
}

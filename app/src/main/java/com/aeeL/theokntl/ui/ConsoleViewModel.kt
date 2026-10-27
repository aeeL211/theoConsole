package com.aeeL.theokntl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeeL.theokntl.core.ErrorKind
import com.aeeL.theokntl.core.FailureInfo
import com.aeeL.theokntl.core.isCancellation
import com.aeeL.theokntl.core.toFailureInfo
import com.aeeL.theokntl.data.log.SessionLog
import com.aeeL.theokntl.domain.gateway.ConsoleGateway
import com.aeeL.theokntl.domain.model.ConsoleState
import com.aeeL.theokntl.domain.model.TopUpReview
import com.aeeL.theokntl.domain.usecase.ApplyTopUpUseCase
import com.aeeL.theokntl.domain.usecase.CancelTopUpUseCase
import com.aeeL.theokntl.domain.usecase.LoginUseCase
import com.aeeL.theokntl.domain.usecase.ReadPurchasesUseCase
import com.aeeL.theokntl.domain.usecase.RefreshBalanceUseCase
import com.aeeL.theokntl.domain.usecase.ReviewTopUpUseCase
import com.aeeL.theokntl.domain.usecase.SignOutUseCase
import com.aeeL.theokntl.domain.usecase.SyncFeaturesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@HiltViewModel
class ConsoleViewModel @Inject constructor(
    gateway: ConsoleGateway,
    private val loginUseCase: LoginUseCase,
    private val refreshBalanceUseCase: RefreshBalanceUseCase,
    private val reviewTopUpUseCase: ReviewTopUpUseCase,
    private val applyTopUpUseCase: ApplyTopUpUseCase,
    private val cancelTopUpUseCase: CancelTopUpUseCase,
    private val readPurchasesUseCase: ReadPurchasesUseCase,
    private val syncFeaturesUseCase: SyncFeaturesUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val sessionLog: SessionLog,
) : ViewModel() {
    val state: StateFlow<ConsoleState> = gateway.state
    val logEntries = sessionLog.entries

    private val _review = MutableStateFlow<TopUpReview?>(null)
    val review: StateFlow<TopUpReview?> = _review.asStateFlow()

    private val _failures = Channel<FailureInfo>(Channel.BUFFERED)
    val failures = _failures.receiveAsFlow()

    private var operationJob: Job? = null

    fun login(username: String, password: String, remember: Boolean) {
        start {
            _review.value = null
            loginUseCase(username, password, remember)
        }
    }

    fun refreshBalance() {
        start { refreshBalanceUseCase() }
    }

    fun reviewTopUp(rawAmount: String) {
        reviewOrReport(rawAmount)?.let { _review.value = it }
    }

    fun applyTopUp() {
        if (_review.value == null) return
        applyCurrentReview()
    }

    fun topUp(rawAmount: String, skipReview: Boolean) {
        val reviewed = reviewOrReport(rawAmount) ?: return
        _review.value = reviewed
        if (skipReview) applyCurrentReview()
    }

    fun cancelTopUp() {
        cancelTopUpUseCase()
        _review.value = null
    }

    fun readPurchases() {
        start { readPurchasesUseCase() }
    }

    fun syncFeatures() {
        start { syncFeaturesUseCase() }
    }

    fun signOut() {
        start {
            signOutUseCase()
            _review.value = null
        }
    }

    fun stopOperation() {
        operationJob?.cancel()
    }

    fun clearLog() {
        sessionLog.clear()
    }

    fun logJson(): String {
        val entries = sessionLog.entries.value
        return buildJsonArray {
            entries.forEach { entry ->
                add(
                    buildJsonObject {
                        put("time", entry.time)
                        put("level", entry.level.name.lowercase())
                        put("message", entry.message)
                        entry.detail?.let { put("detail", it) }
                    },
                )
            }
        }.toString()
    }

    private fun applyCurrentReview() {
        _review.value = null
        start { applyTopUpUseCase() }
    }

    private fun reviewOrReport(rawAmount: String): TopUpReview? = try {
        reviewTopUpUseCase(rawAmount)
    } catch (error: Throwable) {
        report(error)
        null
    }

    private fun start(block: suspend () -> Unit) {
        if (operationJob?.isActive == true) {
            _failures.trySend(
                FailureInfo(ErrorKind.BUSY, "Another operation is still running."),
            )
            return
        }
        operationJob = viewModelScope.launch {
            try {
                block()
            } catch (error: Throwable) {
                report(error)
            }
        }
    }

    private fun report(error: Throwable) {
        if (error.isCancellation()) return
        _failures.trySend(error.toFailureInfo())
    }
}

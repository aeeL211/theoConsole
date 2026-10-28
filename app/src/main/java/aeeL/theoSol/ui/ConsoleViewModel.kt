package aeeL.theoSol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aeeL.theoSol.core.ErrorKind
import aeeL.theoSol.core.FailureInfo
import aeeL.theoSol.core.isCancellation
import aeeL.theoSol.core.toFailureInfo
import aeeL.theoSol.data.log.SessionLog
import aeeL.theoSol.domain.gateway.ConsoleGateway
import aeeL.theoSol.domain.model.ConsoleState
import aeeL.theoSol.domain.model.TopUpOutcome
import aeeL.theoSol.domain.model.TopUpReview
import aeeL.theoSol.domain.usecase.ApplyTopUpUseCase
import aeeL.theoSol.domain.usecase.CancelTopUpUseCase
import aeeL.theoSol.domain.usecase.LoginUseCase
import aeeL.theoSol.domain.usecase.RefreshBalanceUseCase
import aeeL.theoSol.domain.usecase.ReviewTopUpUseCase
import aeeL.theoSol.domain.usecase.SignOutUseCase
import aeeL.theoSol.domain.usecase.SyncFeaturesUseCase
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

    private val _topUpSuccess = Channel<TopUpOutcome>(Channel.BUFFERED)
    val topUpSuccess = _topUpSuccess.receiveAsFlow()

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

    fun cancelTopUp() {
        cancelTopUpUseCase()
        _review.value = null
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
        start {
            val outcome = applyTopUpUseCase()
            _topUpSuccess.send(outcome)
        }
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

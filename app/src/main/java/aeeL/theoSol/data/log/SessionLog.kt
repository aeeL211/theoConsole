package aeeL.theoSol.data.log

import aeeL.theoSol.data.network.ActionInfo
import aeeL.theoSol.data.network.CallFailureInfo
import aeeL.theoSol.data.network.ResponseInfo
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class LogLevel {
    INFO,
    ACTION,
    RESPONSE,
    FAILURE,
    SUCCESS,
}

data class LogEntry(
    val id: Long,
    val time: String,
    val level: LogLevel,
    val message: String,
    val detail: String?,
)

@Singleton
class SessionLog @Inject constructor() {
    private val nextId = AtomicLong(0)
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)
    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())

    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    fun info(message: String) = append(LogLevel.INFO, message, null)

    fun success(message: String) = append(LogLevel.SUCCESS, message, null)

    fun action(info: ActionInfo) {
        val suffix = if (info.args.isBlank()) "" else " · ${info.args}"
        append(LogLevel.ACTION, "→ ${info.name}$suffix", null)
    }

    fun response(info: ResponseInfo) {
        val status = info.status?.toString() ?: "—"
        val elapsed = if (info.elapsedMs > 0) " · ${info.elapsedMs} ms" else ""
        append(
            LogLevel.RESPONSE,
            "HTTP $status · ${info.outcome}$elapsed",
            "${info.method} ${info.path}" +
                if (info.detail.isBlank()) "" else " · ${info.detail}",
        )
    }

    fun failure(info: CallFailureInfo) {
        val status = info.status?.toString() ?: "no response"
        append(
            LogLevel.FAILURE,
            "HTTP $status · request failed",
            "${info.method} ${info.path}" +
                if (info.technical.isBlank()) "" else " · ${info.technical.take(300)}",
        )
    }

    fun clear() {
        _entries.value = emptyList()
    }

    private fun append(level: LogLevel, message: String, detail: String?) {
        val entry = LogEntry(
            id = nextId.incrementAndGet(),
            time = LocalTime.now().format(timeFormat),
            level = level,
            message = message,
            detail = detail?.takeIf { it.isNotBlank() },
        )
        _entries.update { current -> (current + entry).takeLast(MAX_ENTRIES) }
    }

    private companion object {
        const val MAX_ENTRIES = 400
    }
}

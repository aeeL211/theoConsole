package aeeL.theoSol.core

enum class ErrorKind {
    CONFIG,
    USAGE,
    INPUT,
    STATE,
    BUSY,
    CANCELLED,
    NETWORK,
    TIMEOUT,
    AUTH,
    SESSION,
    SIGNATURE,
    PROTOCOL,
    OVERFLOW,
    HTTP,
    UNKNOWN,
}

class ConsoleException(
    val kind: ErrorKind,
    override val message: String,
    val httpStatus: Int? = null,
) : Exception(message)

data class FailureInfo(
    val kind: ErrorKind,
    val technical: String,
    val httpStatus: Int? = null,
)

fun Throwable.toFailureInfo(): FailureInfo = when (this) {
    is ConsoleException -> FailureInfo(kind, message.orEmpty(), httpStatus)
    is java.util.concurrent.CancellationException -> FailureInfo(ErrorKind.CANCELLED, message.orEmpty())
    else -> FailureInfo(ErrorKind.UNKNOWN, message.orEmpty())
}

fun Throwable.isCancellation(): Boolean =
    this is java.util.concurrent.CancellationException ||
        (this is ConsoleException && kind == ErrorKind.CANCELLED)

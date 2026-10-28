package aeeL.theoSol.data.network

import aeeL.theoSol.core.ConsoleConstants
import aeeL.theoSol.core.ConsoleException
import aeeL.theoSol.core.ErrorKind
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

data class HttpResult(
    val status: Int,
    val successful: Boolean,
    val body: ByteArray,
    val elapsedMs: Long,
    val url: String,
)

@Singleton
class HttpTransport @Inject constructor(
    private val client: OkHttpClient,
) {
    suspend fun postForm(url: String, fields: List<Pair<String, String>>): HttpResult {
        val encodedBody = fields.joinToString("&") { (name, value) ->
            "${javaUrlEncode(name)}=${javaUrlEncode(value)}"
        }
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(encodedBody.toByteArray(Charsets.UTF_8).toRequestBody(FORM_MEDIA_TYPE))
            .build()
        return execute(request)
    }

    /**
     * Java's URLEncoder leaves only letters, digits and ".-*_" untouched. A
     * space becomes "+" and every other UTF-8 byte is written as upper-case
     * %XX. OkHttp's FormBody would differ for characters such as "~".
     */
    fun javaUrlEncode(value: String): String {
        val bytes = value.toByteArray(Charsets.UTF_8)
        val builder = StringBuilder(bytes.size * 3)
        for (byte in bytes) {
            val unsigned = byte.toInt() and 0xff
            val safe = unsigned in 0x30..0x39 ||
                unsigned in 0x41..0x5a ||
                unsigned in 0x61..0x7a ||
                unsigned == 0x2a ||
                unsigned == 0x2d ||
                unsigned == 0x2e ||
                unsigned == 0x5f
            when {
                safe -> builder.append(unsigned.toChar())
                unsigned == 0x20 -> builder.append('+')
                else -> {
                    builder.append('%')
                    builder.append(HEX[unsigned ushr 4])
                    builder.append(HEX[unsigned and 0x0f])
                }
            }
        }
        return builder.toString()
    }

    private suspend fun execute(request: Request): HttpResult =
        suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)
            val started = System.nanoTime()
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, error: IOException) {
                    if (!continuation.isActive) return
                    val failure = when {
                        call.isCanceled() -> CancellationException("Request cancelled")
                        error is SocketTimeoutException || error is InterruptedIOException ->
                            ConsoleException(
                                ErrorKind.TIMEOUT,
                                "Timeout after ${ConsoleConstants.REQUEST_TIMEOUT_MS / 1000}s waiting for ${request.url}.",
                            )
                        else -> ConsoleException(
                            ErrorKind.NETWORK,
                            "Connection to ${request.url} failed: ${error.message ?: "no answer"}.",
                        )
                    }
                    continuation.resumeWith(Result.failure(failure))
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { result ->
                        val bytes = try {
                            result.body?.bytes() ?: ByteArray(0)
                        } catch (error: IOException) {
                            if (continuation.isActive) {
                                continuation.resumeWith(
                                    Result.failure(
                                        ConsoleException(
                                            ErrorKind.NETWORK,
                                            "The response body could not be read: ${error.message}.",
                                        ),
                                    ),
                                )
                            }
                            return
                        }
                        if (continuation.isActive) {
                            continuation.resumeWith(
                                Result.success(
                                    HttpResult(
                                        status = result.code,
                                        successful = result.isSuccessful,
                                        body = bytes,
                                        elapsedMs = (System.nanoTime() - started) / 1_000_000L,
                                        url = request.url.toString(),
                                    ),
                                ),
                            )
                        }
                    }
                }
            })
        }

    private companion object {
        val FORM_MEDIA_TYPE = "application/x-www-form-urlencoded".toMediaType()
        const val HEX = "0123456789ABCDEF"
    }
}

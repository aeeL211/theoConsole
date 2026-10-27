package com.aeeL.theokntl.data.network

import com.aeeL.theokntl.core.ConsoleConstants
import com.aeeL.theokntl.core.ConsoleException
import com.aeeL.theokntl.core.CryptoEngine
import com.aeeL.theokntl.core.ErrorKind
import com.aeeL.theokntl.core.Formatters
import com.aeeL.theokntl.core.isCancellation
import com.aeeL.theokntl.domain.model.AccountInfo
import com.aeeL.theokntl.domain.model.EventTotals
import com.aeeL.theokntl.domain.model.PurchaseItem
import com.aeeL.theokntl.domain.model.SessionData
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

data class EventValue(
    val amount: Long,
    val changeTime: Long,
)

data class PurchasePage(
    val items: List<PurchaseItem>,
    val rawCount: Int,
    val invalidCount: Int,
)

class TheoClient(
    private val session: SessionData,
    private val transport: HttpTransport,
    private val crypto: CryptoEngine,
    private val hooks: TheoHooks,
) {
    private val encryptKey = crypto.fromHex(ConsoleConstants.SESSION_CLIENT + session.encKey)
    private val decryptKey = crypto.fromHex(ConsoleConstants.SESSION_SERVER + session.encKey)
    private val iv = crypto.fromHex(ConsoleConstants.SESSION_IV)
    private val requestSalt = crypto.fromHex(ConsoleConstants.REQUEST_SALT_HEX)
    private val responseSalt = crypto.fromHex(ConsoleConstants.RESPONSE_SALT_HEX)

    suspend fun snapshot(): EventTotals {
        val earned = getEvent(ConsoleConstants.EVENT_EARNED).amount
        val spend = getEvent(ConsoleConstants.EVENT_SPEND).amount
        return EventTotals(earned = earned, spend = spend)
    }

    suspend fun getEvent(eventId: String): EventValue {
        val payload = secure(
            payload = buildJsonObject {
                put("cmd", "get event")
                put("id", eventId)
            },
            path = ConsoleConstants.API_EVENTS,
            action = ActionInfo("Baca event", eventId),
        )
        if (payload.stringOrNull("status") == "could not find") {
            return EventValue(0L, 0L)
        }
        requireSuccess(payload, "Baca event")
        return EventValue(
            amount = zeroIfNull(payload["amount"], "Baca event"),
            changeTime = zeroIfNull(payload["change_time"], "Baca event"),
        )
    }

    suspend fun maxEvent(eventId: String, amount: Long) {
        if (amount < 1L || amount > ConsoleConstants.INT32_MAX) {
            throw ConsoleException(ErrorKind.PROTOCOL, "Event amount is out of range.")
        }
        val payload = secure(
            payload = buildJsonObject {
                put("cmd", "max event")
                put("id", eventId)
                put("amount", amount)
            },
            path = ConsoleConstants.API_EVENTS,
            action = ActionInfo("Tulis event", "$eventId, ${Formatters.number(amount)}"),
        )
        requireSuccess(payload, "Tulis event $eventId")
    }

    suspend fun getPurchases(): PurchasePage {
        val payload = secure(
            payload = buildJsonObject { put("cmd", "get purchases") },
            path = ConsoleConstants.API_PURCHASES,
            action = ActionInfo("Baca pembelian", ""),
        )
        if (payload.stringOrNull("status") == "could not find") {
            return PurchasePage(emptyList(), 0, 0)
        }
        requireSuccess(payload, "Baca pembelian")
        val raw = payload["purchases"] as? JsonArray ?: JsonArray(emptyList())
        val items = raw.mapNotNull { entry ->
            val item = entry as? JsonObject ?: return@mapNotNull null
            val id = item["purchase_id"].textOrNull()?.trim().orEmpty()
            if (id.isEmpty()) {
                null
            } else {
                PurchaseItem(
                    id = id,
                    amount = zeroIfNull(item["amount"], "Baca pembelian"),
                )
            }
        }
        return PurchasePage(
            items = items,
            rawCount = raw.size,
            invalidCount = raw.size - items.size,
        )
    }

    suspend fun addPurchase(purchaseId: String, amount: Long) {
        val purchase = normalizePurchase(purchaseId, amount)
        val payload = secure(
            payload = buildJsonObject {
                put("cmd", "add purchase")
                put("id", purchase.first)
                put("amount", purchase.second)
            },
            path = ConsoleConstants.API_PURCHASES,
            action = ActionInfo(
                "Tambah pembelian",
                "${purchase.first}, ${Formatters.number(purchase.second)}",
            ),
        )
        requireSuccess(payload, "Tambah pembelian ${purchase.first}")
    }

    suspend fun maxPurchase(purchaseId: String, amount: Long) {
        val purchase = normalizePurchase(purchaseId, amount)
        val payload = secure(
            payload = buildJsonObject {
                put("cmd", "max purchase")
                put("id", purchase.first)
                put("amount", purchase.second)
            },
            path = ConsoleConstants.API_PURCHASES,
            action = ActionInfo(
                "Maks pembelian",
                "${purchase.first}, ${Formatters.number(purchase.second)}",
            ),
        )
        requireSuccess(payload, "Maks pembelian ${purchase.first}")
    }

    private fun normalizePurchase(purchaseId: String, amount: Long): Pair<String, Long> {
        val id = purchaseId.trim()
        if (id.isEmpty()) {
            throw ConsoleException(ErrorKind.PROTOCOL, "A purchase ID is required.")
        }
        if (amount < 1L) {
            throw ConsoleException(ErrorKind.PROTOCOL, "Invalid purchase amount for $id.")
        }
        return id to amount
    }

    private suspend fun secure(
        payload: JsonObject,
        path: String,
        action: ActionInfo,
    ): JsonObject {
        currentCoroutineContext().ensureActive()
        emit { hooks.onAction(action) }

        val plain = payload.toString().toByteArray(Charsets.UTF_8)
        val encrypted = crypto.aesCbcEncrypt(encryptKey, iv, plain)
        val signature = crypto.base64(crypto.md5(plain + requestSalt))
        val fields = listOf(
            "user" to session.userId.toString(),
            "cake" to session.cake,
            "session" to session.sessionId,
            "content" to crypto.base64(encrypted),
            "sign" to signature,
            "clientid" to ConsoleConstants.CLIENT_ID,
            "clientversion" to ConsoleConstants.CLIENT_BUILD,
            "vi" to ConsoleConstants.CLIENT_VI,
            "compress" to "1",
            "model" to ConsoleConstants.CLIENT_MODEL,
        )

        var http: HttpResult? = null
        var responded = false
        try {
            val result = transport.postForm(ConsoleConstants.DEFAULT_API + path, fields)
            http = result
            if (!result.successful) throw httpError(result)

            val outer = parseJsonBody(result.body)
            val body = if (outer.containsKey("content")) decryptResponse(outer) else outer
            val outcome = body.stringOrNull("status") ?: "success"
            emit {
                hooks.onResponse(
                    ResponseInfo(
                        method = "POST",
                        path = path,
                        status = result.status,
                        ok = result.successful,
                        outcome = outcome,
                        detail = responseDetail(action, body),
                        elapsedMs = result.elapsedMs,
                    ),
                )
            }
            responded = true
            return body
        } catch (error: Throwable) {
            if (!responded && !error.isCancellation()) {
                emit {
                    hooks.onFailure(
                        CallFailureInfo(
                            method = "POST",
                            path = path,
                            technical = error.message.orEmpty(),
                            status = http?.status,
                        ),
                    )
                }
            }
            throw error
        }
    }

    private fun decryptResponse(outer: JsonObject): JsonObject {
        val encrypted = outer.stringOrNull("content")
            ?: throw ConsoleException(ErrorKind.PROTOCOL, "The encrypted response has no content.")
        val signature = outer.stringOrNull("sign")
            ?: throw ConsoleException(ErrorKind.PROTOCOL, "The encrypted response has no signature.")
        val plain = try {
            crypto.aesCbcDecrypt(decryptKey, iv, crypto.decodeBase64(encrypted))
        } catch (error: Exception) {
            throw ConsoleException(ErrorKind.PROTOCOL, "The encrypted response is not valid.")
        }
        val received = try {
            crypto.decodeBase64(signature)
        } catch (error: Exception) {
            throw ConsoleException(ErrorKind.PROTOCOL, "The response signature is not valid Base64.")
        }
        val expected = crypto.md5(plain + responseSalt)
        if (!crypto.timingSafeEqual(received, expected)) {
            throw ConsoleException(
                ErrorKind.SIGNATURE,
                "Server response signature mismatch (wrong session key?).",
            )
        }
        return parseJsonBody(plain)
    }

    private fun requireSuccess(payload: JsonObject, label: String): JsonObject {
        if (payload.stringOrNull("status") != "success") {
            throw ConsoleException(
                ErrorKind.PROTOCOL,
                "$label failed: ${payload.toString().take(300)}",
            )
        }
        return payload
    }

    private fun responseDetail(action: ActionInfo, body: JsonObject): String = when (action.name) {
        "Baca event", "Tulis event" ->
            "amount ${body["amount"].textOrNull() ?: "—"}"
        "Baca pembelian" ->
            "purchases ${(body["purchases"] as? JsonArray)?.size ?: "—"}"
        "Tambah pembelian", "Maks pembelian" ->
            "purchase_id ${body["purchase_id"].textOrNull() ?: "—"} · amount ${body["amount"].textOrNull() ?: "—"}"
        else -> ""
    }

    private fun httpError(result: HttpResult): ConsoleException {
        val detail = String(result.body, Charsets.UTF_8).trim().take(300)
        val suffix = if (detail.isEmpty()) "" else ": $detail"
        return ConsoleException(
            ErrorKind.HTTP,
            "HTTP ${result.status} from ${result.url}$suffix",
            result.status,
        )
    }

    private fun emit(block: () -> Unit) {
        try {
            block()
        } catch (_: Throwable) {
            // Logging must never break the wire call.
        }
    }

    companion object {
        suspend fun verifyForum(
            username: String,
            sealedPassword: String,
            transport: HttpTransport,
            crypto: CryptoEngine,
            hooks: TheoHooks,
        ): JsonObject {
            val user = createVerifyUser(username, sealedPassword)
            val fields = listOf(
                "content" to user.toString(),
                "clientid" to ConsoleConstants.CLIENT_ID,
                "cake" to "",
                "clientversion" to ConsoleConstants.CLIENT_BUILD,
                "vi" to ConsoleConstants.CLIENT_VI,
                "compress" to "1",
                "model" to ConsoleConstants.CLIENT_MODEL,
            )
            val action = ActionInfo("Masuk", "$username, ••••••••")
            emitSafely { hooks.onAction(action) }

            var http: HttpResult? = null
            var responded = false
            try {
                val result = transport.postForm(ConsoleConstants.DEFAULT_API + ConsoleConstants.API_VERIFY, fields)
                http = result
                if (!result.successful) throw httpErrorFor(result)
                val response = parseJsonBody(result.body)
                val payload = response["content"] as? JsonObject ?: response
                val userId = (payload["user"] as? JsonObject)
                    ?.get("id")
                    .textOrNull()
                    ?: "—"
                val detail = "user $userId · session bound"
                val status = payload.stringOrNull("status") ?: "failed"
                if (status != "success") {
                    val hint = payload.stringOrNull("hint") ?: payload.stringOrNull("extra") ?: ""
                    emitSafely {
                        hooks.onResponse(
                            ResponseInfo(
                                method = "POST",
                                path = ConsoleConstants.API_VERIFY,
                                status = result.status,
                                ok = result.successful,
                                outcome = status,
                                detail = if (hint.isNotEmpty()) "hint $hint" else detail,
                                elapsedMs = result.elapsedMs,
                            ),
                        )
                    }
                    responded = true
                    val hintText = if (hint.isNotEmpty()) ", Hint=\"$hint\"" else ""
                    throw ConsoleException(
                        ErrorKind.AUTH,
                        "The verify_fb request failed: Status=\"$status\"$hintText",
                    )
                }
                val session = payload["session"] as? JsonObject
                val encKey = session?.stringOrNull("enc_key").orEmpty()
                if (session == null || encKey.isEmpty()) {
                    emitSafely {
                        hooks.onResponse(
                            ResponseInfo(
                                method = "POST",
                                path = ConsoleConstants.API_VERIFY,
                                status = result.status,
                                ok = result.successful,
                                outcome = "success",
                                detail = detail,
                                elapsedMs = result.elapsedMs,
                            ),
                        )
                    }
                    responded = true
                    throw ConsoleException(
                        ErrorKind.SESSION,
                        "The verify_fb response returned no session key (response keys: ${payload.keys.joinToString(", ")}).",
                    )
                }
                emitSafely {
                    hooks.onResponse(
                        ResponseInfo(
                            method = "POST",
                            path = ConsoleConstants.API_VERIFY,
                            status = result.status,
                            ok = result.successful,
                            outcome = "success",
                            detail = detail,
                            elapsedMs = result.elapsedMs,
                        ),
                    )
                }
                responded = true
                return payload
            } catch (error: Throwable) {
                if (!responded && !error.isCancellation()) {
                    emitSafely {
                        hooks.onFailure(
                            CallFailureInfo(
                                method = "POST",
                                path = ConsoleConstants.API_VERIFY,
                                technical = error.message.orEmpty(),
                                status = http?.status,
                            ),
                        )
                    }
                }
                throw error
            }
        }

        fun extractSession(auth: JsonObject): SessionData {
            val session = auth["session"] as? JsonObject
                ?: throw ConsoleException(ErrorKind.SESSION, "The login response has no session object.")
            val account = auth["user"] as? JsonObject ?: JsonObject(emptyMap())
            val userId = account["id"].strictLongOrNull() ?: 0L
            val sessionId = session["id"].textOrNull().orEmpty()
            val encKey = session["enc_key"].textOrNull().orEmpty()
            val cake = auth["cake"].textOrNull().orEmpty()
            if (userId <= 0L) {
                throw ConsoleException(ErrorKind.SESSION, "The login returned an invalid account ID.")
            }
            if (sessionId.isEmpty()) {
                throw ConsoleException(ErrorKind.SESSION, "The login returned no session ID.")
            }
            if (!Regex("^[0-9a-fA-F]{16}$").matches(encKey)) {
                throw ConsoleException(ErrorKind.SESSION, "The login returned an unusable session key.")
            }
            return SessionData(
                userId = userId,
                sessionId = sessionId,
                encKey = encKey,
                cake = cake,
                account = AccountInfo(
                    id = userId,
                    name = account["name"].textOrNull().orEmpty(),
                    email = account["email"].textOrNull().orEmpty(),
                    forumName = account["forum_name"].textOrNull().orEmpty(),
                    forumId = account["forum_id"].strictLongOrNull(),
                    forumColor = account["forum_color"].textOrNull().orEmpty(),
                ),
            )
        }

        private fun createVerifyUser(username: String, sealedPassword: String): JsonObject =
            buildJsonObject {
                put("id", 0)
                put("name", "")
                put("email", "")
                put("fb_id", "")
                put("social_type", "")
                put("last_active", 0)
                put("time_created", 0)
                put("admin", 0)
                put("permissions", 0)
                put("steam_name", "")
                put("forum_id", 0)
                put("forum_name", username)
                put("forum_pw", sealedPassword)
                put("forum_color", "")
                put("os", "android")
                put("path", "unset")
                put("model", ConsoleConstants.CLIENT_MODEL)
                put("user_ids", JsonArray(emptyList()))
            }

        private fun httpErrorFor(result: HttpResult): ConsoleException {
            val detail = String(result.body, Charsets.UTF_8).trim().take(300)
            val suffix = if (detail.isEmpty()) "" else ": $detail"
            return ConsoleException(
                ErrorKind.HTTP,
                "HTTP ${result.status} from ${result.url}$suffix",
                result.status,
            )
        }

        private fun emitSafely(block: () -> Unit) {
            try {
                block()
            } catch (_: Throwable) {
                // Logging must never break the wire call.
            }
        }
    }
}

private fun ByteArray.gunzipIfNeeded(): ByteArray {
    if (size < 2 || this[0] != 0x1f.toByte() || this[1] != 0x8b.toByte()) return this
    return try {
        GZIPInputStream(ByteArrayInputStream(this)).use { it.readBytes() }
    } catch (error: Exception) {
        throw ConsoleException(ErrorKind.PROTOCOL, "The server sent a gzip body that could not be opened.")
    }
}

private fun parseJsonBody(bytes: ByteArray): JsonObject {
    val text = bytes.gunzipIfNeeded().toString(Charsets.UTF_8).removePrefix("\uFEFF").trim()
    if (text.isEmpty()) {
        throw ConsoleException(ErrorKind.PROTOCOL, "The server returned an empty body.")
    }
    val element = try {
        Json.parseToJsonElement(text)
    } catch (error: Exception) {
        throw ConsoleException(ErrorKind.PROTOCOL, "The server response is not JSON: ${text.take(220)}")
    }
    if (element !is JsonObject) {
        val shape = when (element) {
            is JsonArray -> "array"
            is JsonNull -> "null"
            is JsonPrimitive -> "primitive"
            else -> "object"
        }
        throw ConsoleException(ErrorKind.PROTOCOL, "Unexpected response shape: $shape.")
    }
    return element
}

private fun JsonElement?.textOrNull(): String? =
    (this as? JsonPrimitive)?.contentOrNull

private fun JsonObject.stringOrNull(key: String): String? = this[key].textOrNull()

private fun JsonElement?.strictLongOrNull(): Long? {
    val number = textOrNull()?.toDoubleOrNull() ?: return null
    if (!number.isFinite() ||
        number > Long.MAX_VALUE.toDouble() ||
        number < Long.MIN_VALUE.toDouble()
    ) {
        return null
    }
    return number.toLong()
}

private fun zeroIfNull(element: JsonElement?, label: String): Long {
    if (element == null || element is JsonNull) return 0L
    return element.strictLongOrNull()
        ?: throw ConsoleException(ErrorKind.PROTOCOL, "$label returned an invalid value.")
}

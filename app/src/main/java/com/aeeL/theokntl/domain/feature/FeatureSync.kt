package com.aeeL.theokntl.domain.feature

import com.aeeL.theokntl.core.ConsoleConstants
import com.aeeL.theokntl.core.ConsoleException
import com.aeeL.theokntl.core.ErrorKind
import com.aeeL.theokntl.core.FailureInfo
import com.aeeL.theokntl.core.isCancellation
import com.aeeL.theokntl.core.toFailureInfo
import com.aeeL.theokntl.domain.model.FeatureReport
import com.aeeL.theokntl.domain.model.FeatureStatus
import com.aeeL.theokntl.domain.model.FeatureUnlock
import com.aeeL.theokntl.domain.model.FeatureUnlocks
import com.aeeL.theokntl.domain.model.PurchaseItem
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

interface PurchaseGateway {
    suspend fun getPurchases(): List<PurchaseItem>
    suspend fun addPurchase(id: String, amount: Long)
    suspend fun maxPurchase(id: String, amount: Long)
    suspend fun refreshSession()
}

interface PurchaseStampStore {
    suspend fun read(userId: Long): Map<String, Long>
    suspend fun write(userId: Long, stamps: Map<String, Long>)
}

/**
 * Port of the CLI purchase synchronisation: read first, write only missing rows,
 * keep per-account stamps, watch for the rows, refresh once, fall back to max
 * purchase, and probe when everything stays invisible.
 */
class FeatureSync(
    private val gateway: PurchaseGateway,
    private val stamps: PurchaseStampStore,
    private val nowMillis: () -> Long,
    private val sleep: suspend (Long) -> Unit,
) {
    suspend fun unlock(
        userId: Long,
        watch: List<Long> = ConsoleConstants.PURCHASE_WATCH_MS,
    ): FeatureReport {
        val unlocked = mutableMapOf<String, Boolean>()
        val written = stamps.read(userId).toMutableMap()
        var probe: Boolean? = null

        suspend fun readMissing(): List<FeatureUnlock> {
            currentCoroutineContext().ensureActive()
            val owned = gateway.getPurchases().map { it.id }.toSet()
            FeatureUnlocks.ALL.forEach { feature ->
                unlocked[feature.id] = feature.id in owned
            }
            return FeatureUnlocks.ALL.filter { unlocked[it.id] != true }
        }

        fun snapshot(): List<FeatureStatus> = FeatureUnlocks.ALL.map { feature ->
            FeatureStatus(
                id = feature.id,
                label = feature.label,
                unlocked = unlocked[feature.id] == true,
            )
        }

        suspend fun persist(error: FailureInfo? = null): FeatureReport {
            stamps.write(userId, written.toMap())
            return FeatureReport(snapshot(), error, probe)
        }

        try {
            var missing = readMissing()
            if (missing.isEmpty()) return persist()

            for (item in missing) {
                currentCoroutineContext().ensureActive()
                val stamp = written[item.id] ?: 0L
                if (stamp > 0L &&
                    nowMillis() - stamp < ConsoleConstants.PURCHASE_REWRITE_MS
                ) {
                    continue
                }
                try {
                    gateway.addPurchase(item.id, 1)
                    written[item.id] = nowMillis()
                } catch (error: Throwable) {
                    if (error.isCancellation()) throw error
                    // The request is already captured by the activity log.
                }
                sleep(ConsoleConstants.PURCHASE_SETTLE_MS)
            }
            persist()

            var index = 0
            while (index < watch.size && missing.isNotEmpty()) {
                sleep(watch[index])
                index += 1
                missing = readMissing()
            }
            if (missing.isEmpty()) return persist()

            try {
                gateway.refreshSession()
                missing = readMissing()
                var refreshIndex = 0
                while (refreshIndex < 2 && missing.isNotEmpty()) {
                    val wait = watch.getOrNull(refreshIndex)
                        ?: ConsoleConstants.PURCHASE_WATCH_MS.first()
                    sleep(wait)
                    refreshIndex += 1
                    missing = readMissing()
                }
                if (missing.isEmpty()) return persist()
            } catch (error: Throwable) {
                if (error.isCancellation()) throw error
                // A failed refresh is not fatal; the fallback below still runs.
            }

            for (item in missing) {
                currentCoroutineContext().ensureActive()
                try {
                    gateway.maxPurchase(item.id, 1)
                } catch (error: Throwable) {
                    if (error.isCancellation()) throw error
                    // The request is already captured by the activity log.
                }
                sleep(ConsoleConstants.PURCHASE_SETTLE_MS)
            }

            sleep(ConsoleConstants.PURCHASE_SETTLE_LONG_MS)
            missing = readMissing()
            if (missing.isNotEmpty()) probe = probeWrite()
            if (missing.isNotEmpty()) {
                persist()
                throw ConsoleException(
                    ErrorKind.PROTOCOL,
                    missingMessage(missing, probe),
                )
            }
            return persist()
        } catch (error: Throwable) {
            if (error.isCancellation()) throw error
            return FeatureReport(snapshot(), error.toFailureInfo(), probe)
        }
    }

    private suspend fun probeWrite(): Boolean? {
        val id = "console_probe_" + nowMillis().toString(36)
        return try {
            gateway.addPurchase(id, 1)
            sleep(ConsoleConstants.PURCHASE_SETTLE_LONG_MS)
            gateway.getPurchases().any { it.id == id }
        } catch (error: Throwable) {
            if (error.isCancellation()) throw error
            null
        }
    }

    private fun missingMessage(missing: List<FeatureUnlock>, probe: Boolean?): String {
        val labels = missing.joinToString(", ") { it.label }
        val hint = when (probe) {
            true -> " The probe row came back, so the server filters these two purchase IDs."
            false -> " The probe row did not come back either; rows can appear minutes later. Run sync again before the next top-up."
            null -> ""
        }
        return "Purchases are still missing after add + max: $labels.$hint"
    }
}

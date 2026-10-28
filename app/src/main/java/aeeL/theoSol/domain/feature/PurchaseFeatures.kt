package aeeL.theoSol.domain.feature

import aeeL.theoSol.domain.model.FeatureReport
import aeeL.theoSol.domain.model.FeatureStatus
import aeeL.theoSol.domain.model.FeatureUnlocks
import aeeL.theoSol.domain.model.PurchaseItem

object PurchaseFeatures {
    fun from(items: List<PurchaseItem>): List<FeatureStatus> {
        val owned = items.map { it.id }.toSet()
        return FeatureUnlocks.ALL.map { feature ->
            FeatureStatus(
                id = feature.id,
                label = feature.label,
                unlocked = feature.id in owned,
            )
        }
    }

    fun verified(report: FeatureReport?): Boolean = report?.verified == true
}

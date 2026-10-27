package com.aeeL.theokntl.domain.feature

import com.aeeL.theokntl.domain.model.FeatureReport
import com.aeeL.theokntl.domain.model.FeatureStatus
import com.aeeL.theokntl.domain.model.FeatureUnlocks
import com.aeeL.theokntl.domain.model.PurchaseItem

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

package com.aeeL.theokntl.domain.feature

import com.aeeL.theokntl.domain.model.FeatureUnlocks
import com.aeeL.theokntl.domain.model.PurchaseItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseFeaturesTest {
    @Test
    fun emptyPurchaseListLeavesBothFeaturesLocked() {
        val result = PurchaseFeatures.from(emptyList())
        assertTrue(result.all { !it.unlocked })
    }

    @Test
    fun bothPurchaseRowsUnlockBothFeatures() {
        val items = FeatureUnlocks.ALL.map { PurchaseItem(it.id, 1) }
        val result = PurchaseFeatures.from(items)
        assertTrue(result.all { it.unlocked })
        assertTrue(PurchaseFeatures.verified(com.aeeL.theokntl.domain.model.FeatureReport(result)))
    }

    @Test
    fun aReportWithAnErrorIsNotVerified() {
        val items = FeatureUnlocks.ALL.map { PurchaseItem(it.id, 1) }
        val report = com.aeeL.theokntl.domain.model.FeatureReport(
            items = PurchaseFeatures.from(items),
            error = com.aeeL.theokntl.core.FailureInfo(
                kind = com.aeeL.theokntl.core.ErrorKind.PROTOCOL,
                technical = "test",
            ),
        )
        assertFalse(PurchaseFeatures.verified(report))
    }
}

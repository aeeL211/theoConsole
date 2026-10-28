package aeeL.theoSol.domain.feature

import aeeL.theoSol.domain.model.FeatureUnlocks
import aeeL.theoSol.domain.model.PurchaseItem
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
        assertTrue(PurchaseFeatures.verified(aeeL.theoSol.domain.model.FeatureReport(result)))
    }

    @Test
    fun aReportWithAnErrorIsNotVerified() {
        val items = FeatureUnlocks.ALL.map { PurchaseItem(it.id, 1) }
        val report = aeeL.theoSol.domain.model.FeatureReport(
            items = PurchaseFeatures.from(items),
            error = aeeL.theoSol.core.FailureInfo(
                kind = aeeL.theoSol.core.ErrorKind.PROTOCOL,
                technical = "test",
            ),
        )
        assertFalse(PurchaseFeatures.verified(report))
    }
}

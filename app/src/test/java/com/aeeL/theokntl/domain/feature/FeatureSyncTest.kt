package com.aeeL.theokntl.domain.feature

import com.aeeL.theokntl.core.ErrorKind
import com.aeeL.theokntl.domain.model.FeatureUnlocks
import com.aeeL.theokntl.domain.model.PurchaseItem
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureSyncTest {
    @Test
    fun existingRowsAreReportedWithoutWriting() = runTest {
        val gateway = mockk<PurchaseGateway>()
        val stamps = mockk<PurchaseStampStore>()
        coEvery { gateway.getPurchases() } returns FeatureUnlocks.ALL.map {
            PurchaseItem(it.id, 1)
        }
        coEvery { stamps.read(7) } returns emptyMap()
        coEvery { stamps.write(7, any()) } just Runs

        val report = FeatureSync(gateway, stamps, { 0L }, { }).unlock(7, emptyList())

        assertTrue(report.verified)
        assertTrue(report.error == null)
        coVerify(exactly = 0) { gateway.addPurchase(any(), any()) }
    }

    @Test
    fun missingRowsAreWrittenOnceAndVerifiedByTheWatchRead() = runTest {
        val owned = mutableSetOf<String>()
        val gateway = mockk<PurchaseGateway>()
        val stamps = mockk<PurchaseStampStore>()
        coEvery { gateway.getPurchases() } answers {
            owned.map { PurchaseItem(it, 1) }
        }
        coEvery { gateway.addPurchase(any(), any()) } answers {
            owned += firstArg<String>()
            Unit
        }
        coEvery { stamps.read(9) } returns emptyMap()
        coEvery { stamps.write(9, any()) } just Runs

        val report = FeatureSync(gateway, stamps, { 1000L }, { }).unlock(9, listOf(1L))

        assertTrue(report.verified)
        assertEquals(2, owned.size)
        coVerify(exactly = 2) { gateway.addPurchase(any(), 1L) }
    }

    @Test
    fun fallbackUsesRefreshMaxAndProbeWhenRowsStayHidden() = runTest {
        val gateway = mockk<PurchaseGateway>()
        val stamps = mockk<PurchaseStampStore>()
        coEvery { gateway.getPurchases() } returns emptyList()
        coEvery { gateway.addPurchase(any(), any()) } just Runs
        coEvery { gateway.maxPurchase(any(), any()) } just Runs
        coEvery { gateway.refreshSession() } just Runs
        coEvery { stamps.read(11) } returns emptyMap()
        coEvery { stamps.write(11, any()) } just Runs

        val report = FeatureSync(gateway, stamps, { 0L }, { }).unlock(11, emptyList())

        assertFalse(report.verified)
        assertEquals(ErrorKind.PROTOCOL, report.error?.kind)
        assertEquals(false, report.probe)
        coVerify(exactly = 1) { gateway.refreshSession() }
        coVerify(exactly = 2) { gateway.maxPurchase(any(), 1L) }
    }
}

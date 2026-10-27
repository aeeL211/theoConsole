package com.aeeL.theokntl.domain.usecase

import com.aeeL.theokntl.core.ConsoleConstants
import com.aeeL.theokntl.core.ConsoleException
import com.aeeL.theokntl.core.ErrorKind
import com.aeeL.theokntl.domain.model.EventTotals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TopUpMathTest {
    @Test
    fun roomLeftNeverGoesBelowZero() {
        assertEquals(0L, TopUpMath.roomLeft(ConsoleConstants.LIMIT))
        assertEquals(0L, TopUpMath.roomLeft(ConsoleConstants.LIMIT + 50))
        assertEquals(1000L, TopUpMath.roomLeft(ConsoleConstants.LIMIT - 1000))
    }

    @Test
    fun targetNormalizesWhenSpendIsHigherThanEarned() {
        val review = TopUpMath.review(
            totals = EventTotals(earned = 1000, spend = 5000),
            amount = 2500,
            accountLabel = "AeeL",
            accountUserId = 42,
        )
        assertEquals(-4000L, review.before)
        assertEquals(7500L, review.earned)
        assertEquals(2500L, review.target)
        assertEquals(2500L, review.after)
        assertEquals("AeeL", review.accountLabel)
        assertEquals(42L, review.accountUserId)
    }

    @Test
    fun amountAboveRoomIsRejected() {
        val error = assertThrows(ConsoleException::class.java) {
            TopUpMath.review(
                totals = EventTotals(earned = 5000, spend = 0),
                amount = ConsoleConstants.LIMIT,
                accountLabel = "AeeL",
                accountUserId = 42,
            )
        }
        assertEquals(ErrorKind.INPUT, error.kind)
    }

    @Test
    fun overflowAboveInt32IsRejected() {
        val earned = ConsoleConstants.INT32_MAX
        val error = assertThrows(ConsoleException::class.java) {
            TopUpMath.review(
                totals = EventTotals(earned = earned, spend = earned - 50_000),
                amount = 1000,
                accountLabel = "AeeL",
                accountUserId = 42,
            )
        }
        assertEquals(ErrorKind.OVERFLOW, error.kind)
    }
}

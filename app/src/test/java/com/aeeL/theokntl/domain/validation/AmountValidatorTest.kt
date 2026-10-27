package com.aeeL.theokntl.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountValidatorTest {
    @Test
    fun emptyInputIsRejected() {
        val result = AmountValidator.validate("   ", 1000)
        assertEquals(AmountError.EMPTY, result.error)
        assertNull(result.value)
    }

    @Test
    fun lettersAreRejected() {
        val result = AmountValidator.validate("12k", 1000)
        assertEquals(AmountError.NOT_INTEGER, result.error)
    }

    @Test
    fun separatorsAreRemoved() {
        val result = AmountValidator.validate("12,000", 20000)
        assertEquals(12000L, result.value)
        assertNull(result.error)
    }

    @Test
    fun zeroIsTooSmall() {
        val result = AmountValidator.validate("0", 1000)
        assertEquals(AmountError.TOO_SMALL, result.error)
    }

    @Test
    fun amountAboveRoomIsRejected() {
        val result = AmountValidator.validate("1001", 1000)
        assertEquals(AmountError.TOO_LARGE, result.error)
    }

    @Test
    fun validAmountIsAccepted() {
        val result = AmountValidator.validate("1000", 1000)
        assertEquals(1000L, result.value)
        assertNull(result.error)
    }
}

package aeeL.theoSol.domain.time

import org.junit.Assert.assertEquals
import org.junit.Test

class DayPeriodTest {
    @Test
    fun `morning covers early through late morning`() {
        assertEquals(DayPeriod.MORNING, DayPeriod.fromHour(4))
        assertEquals(DayPeriod.MORNING, DayPeriod.fromHour(10))
    }

    @Test
    fun `afternoon covers midday`() {
        assertEquals(DayPeriod.AFTERNOON, DayPeriod.fromHour(11))
        assertEquals(DayPeriod.AFTERNOON, DayPeriod.fromHour(14))
    }

    @Test
    fun `evening covers late afternoon`() {
        assertEquals(DayPeriod.EVENING, DayPeriod.fromHour(15))
        assertEquals(DayPeriod.EVENING, DayPeriod.fromHour(17))
    }

    @Test
    fun `night covers late evening and early hours`() {
        assertEquals(DayPeriod.NIGHT, DayPeriod.fromHour(18))
        assertEquals(DayPeriod.NIGHT, DayPeriod.fromHour(23))
        assertEquals(DayPeriod.NIGHT, DayPeriod.fromHour(0))
        assertEquals(DayPeriod.NIGHT, DayPeriod.fromHour(3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `hour must be in range`() {
        DayPeriod.fromHour(24)
    }
}

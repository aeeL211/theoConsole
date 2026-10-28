package aeeL.theoSol.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import aeeL.theoSol.R
import aeeL.theoSol.domain.time.DayPeriod
import java.time.LocalTime
import kotlinx.coroutines.delay

@Composable
fun rememberDayPeriod(): DayPeriod {
    val periodic = produceState(
        initialValue = DayPeriod.fromHour(LocalTime.now().hour),
    ) {
        while (true) {
            value = DayPeriod.fromHour(LocalTime.now().hour)
            delay(30_000L)
        }
    }
    return periodic.value
}

@StringRes
fun dayPeriodLabel(period: DayPeriod): Int = when (period) {
    DayPeriod.MORNING -> R.string.greeting_morning
    DayPeriod.AFTERNOON -> R.string.greeting_afternoon
    DayPeriod.EVENING -> R.string.greeting_evening
    DayPeriod.NIGHT -> R.string.greeting_night
}

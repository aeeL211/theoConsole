package aeeL.theoSol.domain.time

enum class DayPeriod {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
    ;

    companion object {
        fun fromHour(hour: Int): DayPeriod {
            require(hour in 0..23) { "hour must be between 0 and 23" }
            return when (hour) {
                in 4..10 -> MORNING
                in 11..14 -> AFTERNOON
                in 15..17 -> EVENING
                else -> NIGHT
            }
        }
    }
}

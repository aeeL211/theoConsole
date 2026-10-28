package aeeL.theoSol.core

import aeeL.theoSol.domain.model.AccountInfo
import aeeL.theoSol.domain.model.EventTotals
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val TIME_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)

    fun number(value: Long): String = String.format(Locale.US, "%,d", value)

    fun signedNumber(value: Long): String {
        val prefix = if (value >= 0) "+" else ""
        return prefix + number(value)
    }

    fun nowTime(): String = LocalTime.now().format(TIME_FORMAT)

    fun roomLeft(balance: Long): Long =
        maxOf(0L, ConsoleConstants.LIMIT - maxOf(balance, 0L))

    fun progress(balance: Long): Float =
        (maxOf(balance, 0L).toDouble() / ConsoleConstants.LIMIT.toDouble())
            .coerceIn(0.0, 1.0)
            .toFloat()

    fun accountLabel(account: AccountInfo?, login: String, userId: Long): String {
        if (account != null) {
            if (account.name.isNotBlank()) return account.name.trim()
            if (account.forumName.isNotBlank()) return account.forumName.trim()
        }
        if (login.isNotBlank()) return login.trim()
        return userId.toString()
    }

    fun roomLeft(stats: EventTotals): Long = roomLeft(stats.balance)
}

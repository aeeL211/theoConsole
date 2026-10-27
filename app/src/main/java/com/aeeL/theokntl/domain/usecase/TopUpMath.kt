package com.aeeL.theokntl.domain.usecase

import com.aeeL.theokntl.core.ConsoleConstants
import com.aeeL.theokntl.core.ConsoleException
import com.aeeL.theokntl.core.ErrorKind
import com.aeeL.theokntl.core.Formatters
import com.aeeL.theokntl.domain.model.EventTotals
import com.aeeL.theokntl.domain.model.FeatureUnlocks
import com.aeeL.theokntl.domain.model.TopUpReview

object TopUpMath {
    fun roomLeft(balance: Long): Long = Formatters.roomLeft(balance)

    fun review(
        totals: EventTotals,
        amount: Long,
        accountLabel: String,
        accountUserId: Long,
    ): TopUpReview {
        if (amount < 1L) {
            throw ConsoleException(ErrorKind.INPUT, "The diamond amount must be at least 1.")
        }
        val room = roomLeft(totals.balance)
        if (amount > room) {
            throw ConsoleException(
                ErrorKind.INPUT,
                "The amount exceeds the remaining room (${Formatters.number(room)}).",
            )
        }
        val newEarned = maxOf(totals.earned, totals.spend) + amount
        if (newEarned > ConsoleConstants.INT32_MAX) {
            throw ConsoleException(
                ErrorKind.OVERFLOW,
                "The top-up would exceed the game counter limit.",
            )
        }
        val target = newEarned - totals.spend
        return TopUpReview(
            before = totals.balance,
            amount = amount,
            earned = newEarned,
            target = target,
            after = target,
            unlocks = FeatureUnlocks.ALL.map { it.label },
            accountLabel = accountLabel,
            accountUserId = accountUserId,
        )
    }
}

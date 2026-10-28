package aeeL.theoSol.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import aeeL.theoSol.R
import aeeL.theoSol.domain.model.ConsoleState
import aeeL.theoSol.domain.model.FeatureUnlocks
import aeeL.theoSol.domain.model.TopUpReview
import aeeL.theoSol.domain.validation.AmountError
import aeeL.theoSol.domain.validation.AmountValidator
import aeeL.theoSol.ui.components.AppCard
import aeeL.theoSol.ui.components.BusyPanel
import aeeL.theoSol.ui.components.EmptyPanel
import aeeL.theoSol.ui.components.HeroCard
import aeeL.theoSol.ui.components.LabeledValue
import aeeL.theoSol.ui.components.ScreenColumn
import aeeL.theoSol.ui.components.SectionHeader
import aeeL.theoSol.ui.components.StatTile
import aeeL.theoSol.ui.components.SwipeToConfirm
import aeeL.theoSol.ui.components.featureLabel
import aeeL.theoSol.ui.components.localizedNumber
import aeeL.theoSol.ui.icons.DiamondFilled
import aeeL.theoSol.ui.theme.Spacing

@Composable
fun TopUpScreen(
    state: ConsoleState,
    review: TopUpReview?,
    onLogin: () -> Unit,
    onReview: (String) -> Unit,
    onTopUp: (String, Boolean) -> Unit,
    onApply: () -> Unit,
    onCancelReview: () -> Unit,
    onStop: () -> Unit,
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var skipReview by rememberSaveable { mutableStateOf(false) }
    val room = state.stats.room ?: 0L
    val validation = AmountValidator.validate(amount, room)
    val busy = state.busy

    if (!state.signedIn) {
        ScreenColumn {
            EmptyPanel(
                title = stringResource(R.string.topup_signed_out),
                body = stringResource(R.string.signed_out_body),
                actionLabel = stringResource(R.string.action_login),
                onAction = onLogin,
            )
        }
        return
    }

    val amountReady = amount.isNotBlank() && validation.error == null
    val swipeEnabled = if (review != null) !busy else amountReady && !busy
    val swipeLabel = when {
        busy -> stringResource(R.string.topup_swipe_busy)
        review != null -> stringResource(R.string.topup_swipe_confirm)
        skipReview -> stringResource(R.string.topup_swipe_apply)
        else -> stringResource(R.string.topup_swipe_review)
    }
    val swipeHint = if (!swipeEnabled && !busy) {
        stringResource(R.string.topup_swipe_invalid)
    } else {
        stringResource(R.string.topup_swipe_hint)
    }
    val onSwipe: () -> Unit = {
        if (review != null) {
            onApply()
        } else if (skipReview) {
            onTopUp(amount, true)
        } else {
            onReview(amount)
        }
    }
    val onAmountChange: (String) -> Unit = { value ->
        amount = value
        if (review != null) onCancelReview()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= 720.dp
        ScreenColumn {
            BusyPanel(operation = state.busyOperation, onStop = onStop)
            if (wide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                    ) {
                        ApplyHero(
                            amount = amount,
                            onAmountChange = onAmountChange,
                            room = room,
                            balance = state.stats.balance ?: 0L,
                            error = validation.error,
                            skipReview = skipReview,
                            onSkipReviewChange = {
                                skipReview = it
                                if (it && review != null) onCancelReview()
                            },
                            busy = busy,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                    ) {
                        ReviewSection(review = review)
                        ConfirmSection(
                            hint = swipeHint,
                            label = swipeLabel,
                            enabled = swipeEnabled,
                            onConfirmed = onSwipe,
                        )
                    }
                }
            } else {
                ApplyHero(
                    amount = amount,
                    onAmountChange = onAmountChange,
                    room = room,
                    balance = state.stats.balance ?: 0L,
                    error = validation.error,
                    skipReview = skipReview,
                    onSkipReviewChange = {
                        skipReview = it
                        if (it && review != null) onCancelReview()
                    },
                    busy = busy,
                )
                ReviewSection(review = review)
                ConfirmSection(
                    hint = swipeHint,
                    label = swipeLabel,
                    enabled = swipeEnabled,
                    onConfirmed = onSwipe,
                )
            }
        }
    }
}

@Composable
private fun ApplyHero(
    amount: String,
    onAmountChange: (String) -> Unit,
    room: Long,
    balance: Long,
    error: AmountError?,
    skipReview: Boolean,
    onSkipReviewChange: (Boolean) -> Unit,
    busy: Boolean,
) {
    HeroCard(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Icon(
                imageVector = DiamondFilled,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.topup_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(R.string.topup_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            HeroMetric(
                label = stringResource(R.string.home_balance),
                value = localizedNumber(balance),
                modifier = Modifier.weight(1f),
            )
            HeroMetric(
                label = stringResource(R.string.topup_room),
                value = localizedNumber(room),
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.topup_amount_label)) },
            singleLine = true,
            enabled = !busy,
            isError = error != null && amount.isNotBlank(),
            supportingText = {
                if (error != null && amount.isNotBlank()) {
                    Text(amountErrorText(error, room))
                } else {
                    Text(stringResource(R.string.topup_amount_hint))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        QuickAmounts(
            amount = amount,
            room = room,
            onPick = onAmountChange,
            enabled = !busy,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.topup_skip_review),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.topup_skip_review_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = skipReview,
                onCheckedChange = onSkipReviewChange,
                enabled = !busy,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    disabledCheckedThumbColor =
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    disabledCheckedTrackColor =
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledUncheckedThumbColor =
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    disabledUncheckedTrackColor =
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                ),
            )
        }
        Text(
            text = stringResource(R.string.topup_write_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun HeroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun QuickAmounts(
    amount: String,
    room: Long,
    onPick: (String) -> Unit,
    enabled: Boolean,
) {
    if (room <= 0L) return
    val maxLabel = stringResource(R.string.topup_quick_max)
    val picks = remember(room) {
        (listOf(1_000L, 5_000L, 10_000L, 25_000L, 50_000L)
            .filter { it <= room } + room).distinct()
    }
    val selected = amount.toLongOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = stringResource(R.string.topup_quick_pick),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        picks.chunked(3).forEach { rowPicks ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                rowPicks.forEach { value ->
                    FilterChip(
                        selected = selected == value,
                        onClick = { onPick(value.toString()) },
                        enabled = enabled,
                        label = {
                            Text(
                                text = if (value == room) maxLabel else localizedNumber(value),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                    )
                }
                repeat(3 - rowPicks.size) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ReviewSection(
    review: TopUpReview?,
) {
    var shownReview by remember { mutableStateOf(review) }
    LaunchedEffect(review) {
        if (review != null) shownReview = review
    }

    AnimatedVisibility(
        visible = review != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        val current = review ?: shownReview ?: return@AnimatedVisibility
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SectionHeader(title = stringResource(R.string.topup_review_title))
            AppCard {
                LabeledValue(
                    label = stringResource(R.string.topup_account),
                    value = "${current.accountLabel} · ${current.accountUserId}",
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    StatTile(
                        label = stringResource(R.string.home_before),
                        value = localizedNumber(current.before),
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = stringResource(R.string.home_amount),
                        value = localizedNumber(current.amount),
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = stringResource(R.string.home_after),
                        value = localizedNumber(current.after),
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    text = stringResource(R.string.topup_unlocks),
                    style = MaterialTheme.typography.labelMedium,
                )
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    FeatureUnlocks.ALL.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            Icon(
                                imageVector = DiamondFilled,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = featureLabel(feature.id),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.topup_review_swipe_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ConfirmSection(
    hint: String,
    label: String,
    enabled: Boolean,
    onConfirmed: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionHeader(title = stringResource(R.string.topup_confirm_title))
        AppCard {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SwipeToConfirm(
                text = label,
                enabled = enabled,
                onConfirmed = onConfirmed,
            )
        }
    }
}

@Composable
private fun amountErrorText(error: AmountError, room: Long): String = when (error) {
    AmountError.EMPTY -> stringResource(R.string.topup_validation_empty)
    AmountError.NOT_INTEGER -> stringResource(R.string.topup_validation_not_integer)
    AmountError.TOO_SMALL -> stringResource(R.string.topup_validation_too_small)
    AmountError.TOO_LARGE -> stringResource(
        R.string.topup_validation_too_large,
        localizedNumber(room),
    )
}

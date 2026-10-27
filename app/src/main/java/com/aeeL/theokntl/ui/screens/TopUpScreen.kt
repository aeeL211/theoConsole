package com.aeeL.theokntl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aeeL.theokntl.R
import com.aeeL.theokntl.core.Formatters
import com.aeeL.theokntl.domain.model.ConsoleState
import com.aeeL.theokntl.domain.model.FeatureUnlocks
import com.aeeL.theokntl.domain.model.TopUpOutcome
import com.aeeL.theokntl.domain.model.TopUpReview
import com.aeeL.theokntl.domain.validation.AmountError
import com.aeeL.theokntl.domain.validation.AmountValidator
import com.aeeL.theokntl.ui.components.BusyPanel
import com.aeeL.theokntl.ui.components.ConfirmDialog
import com.aeeL.theokntl.ui.components.EmptyPanel
import com.aeeL.theokntl.ui.components.FeatureReportContent
import com.aeeL.theokntl.ui.components.LabeledValue
import com.aeeL.theokntl.ui.components.ScreenColumn
import com.aeeL.theokntl.ui.components.SectionCard
import com.aeeL.theokntl.ui.components.StatTile
import com.aeeL.theokntl.ui.components.StatusPill
import com.aeeL.theokntl.ui.components.featureLabel

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
    var confirmApply by rememberSaveable { mutableStateOf(false) }
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

    if (confirmApply) {
        ConfirmDialog(
            title = stringResource(R.string.topup_confirm_title),
            body = stringResource(
                R.string.topup_confirm_body,
                Formatters.number(review?.amount ?: 0L),
            ),
            confirmLabel = stringResource(R.string.action_apply),
            onConfirm = {
                confirmApply = false
                onApply()
            },
            onDismiss = { confirmApply = false },
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= 720.dp
        ScreenColumn {
            if (busy) BusyPanel(operation = state.busyOperation, onStop = onStop)
            if (wide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        FormSection(
                            amount = amount,
                            onAmountChange = {
                                amount = it
                                if (review != null) onCancelReview()
                            },
                            room = room,
                            error = validation.error,
                            skipReview = skipReview,
                            onSkipReviewChange = {
                                skipReview = it
                                if (it && review != null) onCancelReview()
                            },
                            busy = busy,
                            hasReview = review != null,
                            onReview = { onReview(amount) },
                            onTopUp = { onTopUp(amount, true) },
                        )
                        LastOutcomeSection(outcome = state.lastTopUp)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ReviewSection(
                            review = review,
                            busy = busy,
                            onApply = { confirmApply = true },
                            onCancel = onCancelReview,
                        )
                    }
                }
            } else {
                FormSection(
                    amount = amount,
                    onAmountChange = {
                        amount = it
                        if (review != null) onCancelReview()
                    },
                    room = room,
                    error = validation.error,
                    skipReview = skipReview,
                    onSkipReviewChange = {
                        skipReview = it
                        if (it && review != null) onCancelReview()
                    },
                    busy = busy,
                    hasReview = review != null,
                    onReview = { onReview(amount) },
                    onTopUp = { onTopUp(amount, true) },
                )
                ReviewSection(
                    review = review,
                    busy = busy,
                    onApply = { confirmApply = true },
                    onCancel = onCancelReview,
                )
                LastOutcomeSection(outcome = state.lastTopUp)
            }
        }
    }
}

@Composable
private fun FormSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    room: Long,
    error: AmountError?,
    skipReview: Boolean,
    onSkipReviewChange: (Boolean) -> Unit,
    busy: Boolean,
    hasReview: Boolean,
    onReview: () -> Unit,
    onTopUp: () -> Unit,
) {
    SectionCard(title = stringResource(R.string.topup_title)) {
        Text(
            text = stringResource(R.string.topup_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LabeledValue(
            label = stringResource(R.string.topup_room),
            value = Formatters.number(room),
            mono = true,
        )
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            )
        }
        Button(
            onClick = if (skipReview) onTopUp else onReview,
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy && amount.isNotBlank() && error == null && !hasReview,
        ) {
            Text(
                stringResource(
                    if (skipReview) R.string.action_apply_now else R.string.action_review_topup,
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
private fun ReviewSection(
    review: TopUpReview?,
    busy: Boolean,
    onApply: () -> Unit,
    onCancel: () -> Unit,
) {
    SectionCard(title = stringResource(R.string.topup_review_title)) {
        if (review == null) {
            Text(
                text = stringResource(R.string.topup_review_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@SectionCard
        }
        LabeledValue(
            label = stringResource(R.string.topup_account),
            value = "${review.accountLabel} · ${review.accountUserId}",
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatTile(
                label = stringResource(R.string.home_before),
                value = Formatters.number(review.before),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.home_amount),
                value = Formatters.number(review.amount),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.home_after),
                value = Formatters.number(review.after),
                modifier = Modifier.weight(1f),
            )
        }
        LabeledValue(
            label = stringResource(R.string.home_target),
            value = Formatters.number(review.target),
            mono = true,
        )
        Text(
            text = stringResource(R.string.topup_unlocks),
            style = MaterialTheme.typography.labelMedium,
        )
        FeatureUnlocks.ALL.forEach { feature ->
            Text(
                text = "• ${featureLabel(feature.id)}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f),
                enabled = !busy,
            ) {
                Text(stringResource(R.string.action_apply))
            }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !busy,
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    }
}

@Composable
private fun LastOutcomeSection(outcome: TopUpOutcome?) {
    if (outcome == null) return
    SectionCard(title = stringResource(R.string.topup_outcome_title)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatTile(
                label = stringResource(R.string.home_before),
                value = Formatters.number(outcome.before),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.home_after),
                value = Formatters.number(outcome.after),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.home_target),
                value = Formatters.number(outcome.target),
                modifier = Modifier.weight(1f),
            )
        }
        if (outcome.capped) {
            StatusPill(
                text = stringResource(R.string.topup_capped),
                positive = false,
            )
            Text(
                text = stringResource(R.string.topup_capped_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(R.string.topup_features_title),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
        FeatureReportContent(report = outcome.features)
    }
}

@Composable
private fun amountErrorText(error: AmountError, room: Long): String = when (error) {
    AmountError.EMPTY -> stringResource(R.string.topup_validation_empty)
    AmountError.NOT_INTEGER -> stringResource(R.string.topup_validation_not_integer)
    AmountError.TOO_SMALL -> stringResource(R.string.topup_validation_too_small)
    AmountError.TOO_LARGE -> stringResource(
        R.string.topup_validation_too_large,
        Formatters.number(room),
    )
}

package aeeL.theoSol.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import aeeL.theoSol.R
import aeeL.theoSol.domain.model.ConsoleState
import aeeL.theoSol.ui.components.AppCard
import aeeL.theoSol.ui.components.ConfirmDialog
import aeeL.theoSol.ui.components.EmptyPanel
import aeeL.theoSol.ui.components.HeroCard
import aeeL.theoSol.ui.components.LabeledValue
import aeeL.theoSol.ui.components.ScreenColumn
import aeeL.theoSol.ui.components.SecondaryButton
import aeeL.theoSol.ui.components.SectionHeader
import aeeL.theoSol.ui.components.StatusChip
import aeeL.theoSol.ui.components.StatusTone
import aeeL.theoSol.ui.components.accountFieldLabel
import aeeL.theoSol.ui.theme.Dimens
import aeeL.theoSol.ui.theme.PillShape
import aeeL.theoSol.ui.theme.Spacing

@Composable
fun AccountScreen(
    state: ConsoleState,
    onLogin: () -> Unit,
    onOpenSession: () -> Unit,
    onSignOut: () -> Unit,
    onStop: () -> Unit,
) {
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    val account = state.account

    if (!state.signedIn || account == null) {
        ScreenColumn {
            EmptyPanel(
                title = stringResource(R.string.account_signed_out),
                body = stringResource(R.string.signed_out_body),
                actionLabel = stringResource(R.string.action_login),
                onAction = onLogin,
            )
        }
        return
    }

    if (confirmSignOut) {
        ConfirmDialog(
            title = stringResource(R.string.account_signout_confirm_title),
            body = stringResource(R.string.account_signout_confirm_body),
            confirmLabel = stringResource(R.string.action_sign_out),
            onConfirm = {
                confirmSignOut = false
                onSignOut()
            },
            onDismiss = { confirmSignOut = false },
        )
    }

    ScreenColumn {
        HeroCard(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            val initial = account.label.trim().firstOrNull()?.uppercase()
                ?: stringResource(R.string.home_account_fallback).take(1)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                Surface(
                    modifier = Modifier.size(Dimens.avatar),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Text(
                        text = account.label,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    StatusChip(
                        text = stringResource(R.string.home_signed_in),
                        tone = StatusTone.POSITIVE,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                LabeledValue(
                    label = stringResource(R.string.field_user_id),
                    value = account.userId.toString(),
                    modifier = Modifier.weight(1f),
                )
                LabeledValue(
                    label = stringResource(R.string.account_since),
                    value = account.since,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        SectionHeader(title = stringResource(R.string.account_identity))
        AppCard {
            account.rows.forEach { row ->
                LabeledValue(
                    label = stringResource(accountFieldLabel(row.field)),
                    value = row.value,
                )
            }
        }
        SectionHeader(title = stringResource(R.string.account_session_details))
        AppCard {
            val session = state.session
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                LabeledValue(
                    label = stringResource(R.string.session_bound),
                    value = stringResource(
                        if (session.sessionBound) R.string.common_yes else R.string.common_no,
                    ),
                    modifier = Modifier.weight(1f),
                )
                LabeledValue(
                    label = stringResource(R.string.session_endpoint),
                    value = session.endpoint ?: stringResource(R.string.common_not_available),
                    modifier = Modifier.weight(1f),
                    monospace = true,
                )
            }
            LabeledValue(
                label = stringResource(R.string.session_key_fingerprint),
                value = session.keyFingerprint ?: stringResource(R.string.common_not_available),
                monospace = true,
            )
            SecondaryButton(
                text = stringResource(R.string.account_open_session),
                onClick = onOpenSession,
                enabled = !state.busy,
            )
        }
        if (state.busy) {
            SecondaryButton(
                text = stringResource(R.string.action_stop),
                onClick = onStop,
            )
        }
        OutlinedButton(
            onClick = { confirmSignOut = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.buttonHeight),
            enabled = !state.busy,
            shape = PillShape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text(stringResource(R.string.action_sign_out))
        }
    }
}

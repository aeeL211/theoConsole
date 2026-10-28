package aeeL.theoSol.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import aeeL.theoSol.R
import aeeL.theoSol.domain.model.ConsoleState
import aeeL.theoSol.domain.model.SessionStatus
import aeeL.theoSol.ui.components.AppCard
import aeeL.theoSol.ui.components.BusyPanel
import aeeL.theoSol.ui.components.EmptyPanel
import aeeL.theoSol.ui.components.HeroCard
import aeeL.theoSol.ui.components.LabeledValue
import aeeL.theoSol.ui.components.ScreenColumn
import aeeL.theoSol.ui.components.SectionHeader
import aeeL.theoSol.ui.components.StatusChip
import aeeL.theoSol.ui.components.StatusTone
import aeeL.theoSol.ui.components.sessionStatusLabel
import aeeL.theoSol.ui.theme.Spacing

@Composable
fun SessionScreen(
    state: ConsoleState,
    onLogin: () -> Unit,
    onStop: () -> Unit,
) {
    val session = state.session
    if (!state.signedIn || !session.signedIn) {
        ScreenColumn {
            EmptyPanel(
                title = stringResource(R.string.session_signed_out_title),
                body = stringResource(R.string.session_signed_out_body),
                actionLabel = stringResource(R.string.action_login),
                onAction = onLogin,
            )
        }
        return
    }

    ScreenColumn {
        BusyPanel(operation = state.busyOperation, onStop = onStop)
        HeroCard(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Text(
                text = stringResource(R.string.session_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            StatusChip(
                text = stringResource(sessionStatusLabel(session.status)),
                tone = when (session.status) {
                    SessionStatus.READY -> StatusTone.POSITIVE
                    SessionStatus.BUSY -> StatusTone.NEUTRAL
                    SessionStatus.SIGNED_OUT -> StatusTone.NEGATIVE
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                LabeledValue(
                    label = stringResource(R.string.session_user_id),
                    value = session.userId?.toString()
                        ?: stringResource(R.string.common_not_available),
                    modifier = Modifier.weight(1f),
                )
                LabeledValue(
                    label = stringResource(R.string.session_bound),
                    value = stringResource(
                        if (session.sessionBound) R.string.common_yes else R.string.common_no,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        SectionHeader(title = stringResource(R.string.account_session_details))
        AppCard {
            LabeledValue(
                label = stringResource(R.string.session_key_fingerprint),
                value = session.keyFingerprint ?: stringResource(R.string.common_not_available),
                monospace = true,
            )
            LabeledValue(
                label = stringResource(R.string.session_endpoint),
                value = session.endpoint ?: stringResource(R.string.common_not_available),
                monospace = true,
            )
            LabeledValue(
                label = stringResource(R.string.session_last_action),
                value = session.lastAction ?: stringResource(R.string.common_not_available),
            )
        }
        SectionHeader(title = stringResource(R.string.session_security_title))
        AppCard {
            Text(
                text = stringResource(R.string.session_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

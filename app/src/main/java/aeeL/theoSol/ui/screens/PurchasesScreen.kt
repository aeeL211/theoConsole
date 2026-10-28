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
import aeeL.theoSol.ui.components.AppCard
import aeeL.theoSol.ui.components.BusyPanel
import aeeL.theoSol.ui.components.EmptyPanel
import aeeL.theoSol.ui.components.FeatureReportContent
import aeeL.theoSol.ui.components.PrimaryButton
import aeeL.theoSol.ui.components.ScreenColumn
import aeeL.theoSol.ui.components.SecondaryButton
import aeeL.theoSol.ui.components.SectionHeader
import aeeL.theoSol.ui.components.StatusChip
import aeeL.theoSol.ui.components.StatusTone
import aeeL.theoSol.ui.theme.Spacing

@Composable
fun PurchasesScreen(
    state: ConsoleState,
    onLogin: () -> Unit,
    onRead: () -> Unit,
    onSync: () -> Unit,
    onStop: () -> Unit,
) {
    if (!state.signedIn) {
        ScreenColumn {
            EmptyPanel(
                title = stringResource(R.string.purchases_signed_out),
                body = stringResource(R.string.signed_out_body),
                actionLabel = stringResource(R.string.action_login),
                onAction = onLogin,
            )
        }
        return
    }

    ScreenColumn {
        Text(
            text = stringResource(R.string.purchases_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BusyPanel(operation = state.busyOperation, onStop = onStop)
        SectionHeader(title = stringResource(R.string.purchases_status_title))
        AppCard {
            val report = state.features
            if (report == null) {
                Text(
                    text = stringResource(R.string.purchases_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                StatusChip(
                    text = stringResource(
                        if (report.verified) {
                            R.string.purchases_verified
                        } else {
                            R.string.purchases_not_verified
                        },
                    ),
                    tone = if (report.verified) StatusTone.POSITIVE else StatusTone.NEGATIVE,
                )
                FeatureReportContent(report = report)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                PrimaryButton(
                    text = stringResource(R.string.action_read),
                    onClick = onRead,
                    enabled = !state.busy,
                    modifier = Modifier.weight(1f),
                )
                SecondaryButton(
                    text = stringResource(R.string.action_sync),
                    onClick = onSync,
                    enabled = !state.busy,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        SectionHeader(title = stringResource(R.string.purchases_how_title))
        AppCard {
            Text(
                text = stringResource(R.string.purchases_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

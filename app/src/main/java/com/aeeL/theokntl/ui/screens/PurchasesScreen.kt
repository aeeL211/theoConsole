package com.aeeL.theokntl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aeeL.theokntl.R
import com.aeeL.theokntl.domain.model.ConsoleState
import com.aeeL.theokntl.ui.components.BusyPanel
import com.aeeL.theokntl.ui.components.EmptyPanel
import com.aeeL.theokntl.ui.components.FeatureReportContent
import com.aeeL.theokntl.ui.components.ScreenColumn
import com.aeeL.theokntl.ui.components.SectionCard
import com.aeeL.theokntl.ui.components.StatusPill

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
        SectionCard(title = stringResource(R.string.purchases_status_title)) {
            val report = state.features
            if (report == null) {
                Text(
                    text = stringResource(R.string.purchases_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                StatusPill(
                    text = stringResource(
                        if (report.verified) R.string.purchases_verified else R.string.purchases_not_verified,
                    ),
                    positive = report.verified,
                )
                FeatureReportContent(report = report)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onRead,
                    modifier = Modifier.weight(1f),
                    enabled = !state.busy,
                ) {
                    Text(stringResource(R.string.action_read))
                }
                OutlinedButton(
                    onClick = onSync,
                    modifier = Modifier.weight(1f),
                    enabled = !state.busy,
                ) {
                    Text(stringResource(R.string.action_sync))
                }
            }
        }
        SectionCard(title = stringResource(R.string.purchases_how_title)) {
            Text(
                text = stringResource(R.string.purchases_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

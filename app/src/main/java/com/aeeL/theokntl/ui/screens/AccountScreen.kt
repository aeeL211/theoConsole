package com.aeeL.theokntl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aeeL.theokntl.R
import com.aeeL.theokntl.domain.model.ConsoleState
import com.aeeL.theokntl.ui.components.ConfirmDialog
import com.aeeL.theokntl.ui.components.EmptyPanel
import com.aeeL.theokntl.ui.components.LabeledValue
import com.aeeL.theokntl.ui.components.ScreenColumn
import com.aeeL.theokntl.ui.components.SectionCard
import com.aeeL.theokntl.ui.components.accountFieldLabel

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
        SectionCard(title = stringResource(R.string.account_details)) {
            Text(
                text = account.label,
                style = MaterialTheme.typography.headlineSmall,
            )
            LabeledValue(
                label = stringResource(R.string.account_since),
                value = account.since,
            )
            LabeledValue(
                label = stringResource(R.string.field_user_id),
                value = account.userId.toString(),
                mono = true,
            )
        }
        SectionCard(title = stringResource(R.string.account_identity)) {
            account.rows.forEach { row ->
                LabeledValue(
                    label = stringResource(accountFieldLabel(row.field)),
                    value = row.value,
                    mono = row.mono,
                )
            }
        }
        SectionCard(title = stringResource(R.string.account_session_details)) {
            val session = state.session
            LabeledValue(
                label = stringResource(R.string.session_bound),
                value = stringResource(
                    if (session.sessionBound) R.string.common_yes else R.string.common_no,
                ),
            )
            LabeledValue(
                label = stringResource(R.string.session_key_fingerprint),
                value = session.keyFingerprint ?: stringResource(R.string.common_not_available),
                mono = true,
            )
            LabeledValue(
                label = stringResource(R.string.session_endpoint),
                value = session.endpoint ?: stringResource(R.string.common_not_available),
                mono = true,
            )
            OutlinedButton(
                onClick = onOpenSession,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.busy,
            ) {
                Text(stringResource(R.string.account_open_session))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { confirmSignOut = true },
                modifier = Modifier.weight(1f),
                enabled = !state.busy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(stringResource(R.string.action_sign_out))
            }
            if (state.busy) {
                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.action_stop))
                }
            }
        }
    }
}

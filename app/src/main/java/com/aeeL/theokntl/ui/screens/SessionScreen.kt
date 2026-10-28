package com.aeeL.theokntl.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aeeL.theokntl.R
import com.aeeL.theokntl.domain.model.ConsoleState
import com.aeeL.theokntl.ui.components.BusyPanel
import com.aeeL.theokntl.ui.components.EmptyPanel
import com.aeeL.theokntl.ui.components.LabeledValue
import com.aeeL.theokntl.ui.components.ScreenColumn
import com.aeeL.theokntl.ui.components.SectionCard
import com.aeeL.theokntl.ui.components.sessionStatusLabel

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
        SectionCard(title = stringResource(R.string.session_title)) {
            LabeledValue(
                label = stringResource(R.string.session_status),
                value = stringResource(sessionStatusLabel(session.status)),
            )
            LabeledValue(
                label = stringResource(R.string.session_user_id),
                value = session.userId?.toString() ?: stringResource(R.string.common_not_available),
            )
            LabeledValue(
                label = stringResource(R.string.session_bound),
                value = stringResource(
                    if (session.sessionBound) R.string.common_yes else R.string.common_no,
                ),
            )
            LabeledValue(
                label = stringResource(R.string.session_key_fingerprint),
                value = session.keyFingerprint ?: stringResource(R.string.common_not_available),
            )
            LabeledValue(
                label = stringResource(R.string.session_endpoint),
                value = session.endpoint ?: stringResource(R.string.common_not_available),
            )
            LabeledValue(
                label = stringResource(R.string.session_last_action),
                value = session.lastAction ?: stringResource(R.string.common_not_available),
            )
        }
        SectionCard(title = stringResource(R.string.session_security_title)) {
            Text(
                text = stringResource(R.string.session_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

package com.aeeL.theokntl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aeeL.theokntl.R
import com.aeeL.theokntl.core.Formatters
import com.aeeL.theokntl.domain.model.ConsoleState
import com.aeeL.theokntl.domain.model.FeatureReport
import com.aeeL.theokntl.domain.model.TopUpOutcome
import com.aeeL.theokntl.ui.components.BusyPanel
import com.aeeL.theokntl.ui.components.EmptyPanel
import com.aeeL.theokntl.ui.components.FeatureReportContent
import com.aeeL.theokntl.ui.components.LabeledValue
import com.aeeL.theokntl.ui.components.ScreenColumn
import com.aeeL.theokntl.ui.components.SectionCard
import com.aeeL.theokntl.ui.components.StatTile
import com.aeeL.theokntl.ui.components.StatusPill
import com.aeeL.theokntl.ui.components.ToolRow
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    state: ConsoleState,
    onLogin: () -> Unit,
    onRefresh: () -> Unit,
    onSync: () -> Unit,
    onStop: () -> Unit,
    onTopUp: () -> Unit,
    onPurchases: () -> Unit,
    onSession: () -> Unit,
    onProtocol: () -> Unit,
    onDeveloper: () -> Unit,
    onHelp: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= 720.dp
        if (!state.signedIn) {
            ScreenColumn {
                EmptyPanel(
                    title = stringResource(R.string.home_signed_out_title),
                    body = stringResource(R.string.home_signed_out_body),
                    actionLabel = stringResource(R.string.action_login),
                    onAction = onLogin,
                )
                ToolsSection(
                    onSession = onSession,
                    onProtocol = onProtocol,
                    onDeveloper = onDeveloper,
                    onHelp = onHelp,
                )
            }
            return@BoxWithConstraints
        }

        ScreenColumn {
            Text(
                text = state.account?.label ?: stringResource(R.string.home_account_fallback),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = state.lastAction?.let {
                    stringResource(R.string.home_last_action, it)
                } ?: stringResource(R.string.home_signed_in),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.busy) {
                BusyPanel(operation = state.busyOperation, onStop = onStop)
            }
            if ((state.stats.balance ?: 0L) >= state.stats.limit) {
                SectionCard(title = stringResource(R.string.home_ceiling_title)) {
                    Text(
                        text = stringResource(R.string.home_ceiling_warning),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

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
                        BalanceSection(state = state)
                        ActionsSection(
                            busy = state.busy,
                            onRefresh = onRefresh,
                            onSync = onSync,
                            onTopUp = onTopUp,
                        )
                        LastTopUpSection(outcome = state.lastTopUp)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        FeaturesSection(
                            report = state.features,
                            busy = state.busy,
                            onSync = onSync,
                            onPurchases = onPurchases,
                        )
                        ToolsSection(
                            onSession = onSession,
                            onProtocol = onProtocol,
                            onDeveloper = onDeveloper,
                            onHelp = onHelp,
                        )
                    }
                }
            } else {
                BalanceSection(state = state)
                ActionsSection(
                    busy = state.busy,
                    onRefresh = onRefresh,
                    onSync = onSync,
                    onTopUp = onTopUp,
                )
                LastTopUpSection(outcome = state.lastTopUp)
                FeaturesSection(
                    report = state.features,
                    busy = state.busy,
                    onSync = onSync,
                    onPurchases = onPurchases,
                )
                ToolsSection(
                    onSession = onSession,
                    onProtocol = onProtocol,
                    onDeveloper = onDeveloper,
                    onHelp = onHelp,
                )
            }
        }
    }
}

@Composable
private fun BalanceSection(state: ConsoleState) {
    val stats = state.stats
    val balance = stats.balance ?: 0L
    SectionCard(title = stringResource(R.string.home_balance)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = Formatters.number(balance),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(
                    R.string.home_balance_of_limit,
                    Formatters.number(stats.limit),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(
            progress = { stats.progress },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(
                R.string.home_progress,
                (stats.progress * 100f).roundToInt(),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatTile(
                label = stringResource(R.string.home_earned),
                value = Formatters.number(stats.earned ?: 0L),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.home_spend),
                value = Formatters.number(stats.spend ?: 0L),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.home_room),
                value = Formatters.number(stats.room ?: 0L),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ActionsSection(
    busy: Boolean,
    onRefresh: () -> Unit,
    onSync: () -> Unit,
    onTopUp: () -> Unit,
) {
    SectionCard(title = stringResource(R.string.home_actions)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onTopUp,
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy,
            ) {
                Text(stringResource(R.string.action_top_up))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.weight(1f),
                    enabled = !busy,
                ) {
                    Text(stringResource(R.string.action_refresh))
                }
                OutlinedButton(
                    onClick = onSync,
                    modifier = Modifier.weight(1f),
                    enabled = !busy,
                ) {
                    Text(stringResource(R.string.action_sync))
                }
            }
        }
    }
}

@Composable
private fun LastTopUpSection(outcome: TopUpOutcome?) {
    if (outcome == null) return
    SectionCard(title = stringResource(R.string.home_last_topup)) {
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
                label = stringResource(R.string.home_amount),
                value = Formatters.number(outcome.amount),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.home_after),
                value = Formatters.number(outcome.after),
                modifier = Modifier.weight(1f),
            )
        }
        LabeledValue(
            label = stringResource(R.string.home_target),
            value = Formatters.number(outcome.target),
        )
        if (outcome.capped) {
            StatusPill(
                text = stringResource(R.string.topup_capped),
                positive = false,
            )
        }
    }
}

@Composable
private fun FeaturesSection(
    report: FeatureReport?,
    busy: Boolean,
    onSync: () -> Unit,
    onPurchases: () -> Unit,
) {
    SectionCard(title = stringResource(R.string.home_features)) {
        if (report == null) {
            Text(
                text = stringResource(R.string.home_no_features),
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
            OutlinedButton(
                onClick = onSync,
                modifier = Modifier.weight(1f),
                enabled = !busy,
            ) {
                Text(stringResource(R.string.action_sync))
            }
            TextButton(
                onClick = onPurchases,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.action_open_purchases))
            }
        }
    }
}

@Composable
private fun ToolsSection(
    onSession: () -> Unit,
    onProtocol: () -> Unit,
    onDeveloper: () -> Unit,
    onHelp: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.home_tools),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 3.dp),
        )
        ToolRow(
            title = stringResource(R.string.title_session),
            body = stringResource(R.string.home_session_summary),
            icon = Icons.Default.Info,
            onClick = onSession,
        )
        ToolRow(
            title = stringResource(R.string.title_protocol),
            body = stringResource(R.string.home_protocol_summary),
            icon = Icons.Default.Build,
            onClick = onProtocol,
        )
        ToolRow(
            title = stringResource(R.string.title_developer),
            body = stringResource(R.string.home_developer_summary),
            icon = Icons.Default.Person,
            onClick = onDeveloper,
        )
        ToolRow(
            title = stringResource(R.string.title_help),
            body = stringResource(R.string.home_help_summary),
            icon = Icons.Default.Menu,
            onClick = onHelp,
        )
        ToolRow(
            title = stringResource(R.string.action_top_up),
            body = stringResource(R.string.home_topup_summary),
            icon = Icons.Default.AddCircle,
            onClick = onTopUp,
        )
        ToolRow(
            title = stringResource(R.string.title_purchases),
            body = stringResource(R.string.home_purchases_summary),
            icon = Icons.Default.ShoppingCart,
            onClick = onPurchases,
        )
    }
}

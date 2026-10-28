package com.aeeL.theokntl.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aeeL.theokntl.R
import com.aeeL.theokntl.core.Formatters
import com.aeeL.theokntl.domain.model.ConsoleState
import com.aeeL.theokntl.domain.model.FeatureReport
import com.aeeL.theokntl.domain.model.TopUpOutcome
import com.aeeL.theokntl.domain.time.DayPeriod
import com.aeeL.theokntl.ui.components.BusyPanel
import com.aeeL.theokntl.ui.components.EmptyPanel
import com.aeeL.theokntl.ui.components.FeatureReportContent
import com.aeeL.theokntl.ui.components.ScreenColumn
import com.aeeL.theokntl.ui.components.SectionCard
import com.aeeL.theokntl.ui.components.StatTile
import com.aeeL.theokntl.ui.components.StatusPill
import com.aeeL.theokntl.ui.components.ToolRow
import com.aeeL.theokntl.ui.components.dayPeriodLabel
import com.aeeL.theokntl.ui.components.rememberDayPeriod
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
    val period = rememberDayPeriod()
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= 720.dp
        if (!state.signedIn) {
            ScreenColumn {
                GreetingHeader(state = state, period = period)
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
            GreetingHeader(state = state, period = period)
            BusyPanel(operation = state.busyOperation, onStop = onStop)
            AnimatedVisibility(
                visible = (state.stats.balance ?: 0L) >= state.stats.limit,
                enter = fadeIn(animationSpec = tween(180)) +
                    expandVertically(
                        animationSpec = tween(260),
                        expandFrom = Alignment.Top,
                    ),
                exit = fadeOut(animationSpec = tween(140)) +
                    shrinkVertically(
                        animationSpec = tween(220),
                        shrinkTowards = Alignment.Top,
                    ),
            ) {
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
private fun GreetingHeader(
    state: ConsoleState,
    period: DayPeriod,
) {
    val greeting = stringResource(dayPeriodLabel(period))
    val accountName = state.account?.label?.takeIf { it.isNotBlank() }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = if (state.signedIn && accountName != null) {
                stringResource(R.string.home_greeting_name, greeting, accountName)
            } else {
                greeting
            },
            style = MaterialTheme.typography.displaySmall,
        )
        if (state.signedIn) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatusPill(
                    text = stringResource(R.string.home_signed_in),
                    positive = true,
                )
                state.lastAction?.let { lastAction ->
                    Text(
                        text = stringResource(R.string.home_last_action, lastAction),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy,
            ) {
                Text(stringResource(R.string.action_refresh))
            }
        }
    }
}

@Composable
private fun LastTopUpSection(outcome: TopUpOutcome?) {
    var shownOutcome by remember { mutableStateOf(outcome) }
    LaunchedEffect(outcome) {
        if (outcome != null) shownOutcome = outcome
    }

    AnimatedVisibility(
        visible = outcome != null,
        enter = fadeIn(animationSpec = tween(180)) +
            expandVertically(
                animationSpec = tween(260),
                expandFrom = Alignment.Top,
            ),
        exit = fadeOut(animationSpec = tween(140)) +
            shrinkVertically(
                animationSpec = tween(220),
                shrinkTowards = Alignment.Top,
            ),
    ) {
        val lastOutcome = outcome ?: shownOutcome ?: return@AnimatedVisibility
        SectionCard(title = stringResource(R.string.home_last_topup)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatTile(
                    label = stringResource(R.string.home_before),
                    value = Formatters.number(lastOutcome.before),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.home_amount),
                    value = Formatters.number(lastOutcome.amount),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.home_after),
                    value = Formatters.number(lastOutcome.after),
                    modifier = Modifier.weight(1f),
                )
            }
            if (lastOutcome.capped) {
                StatusPill(
                    text = stringResource(R.string.topup_capped),
                    positive = false,
                )
            }
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
    }
}

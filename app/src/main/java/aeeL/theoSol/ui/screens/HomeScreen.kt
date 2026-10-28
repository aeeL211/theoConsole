package aeeL.theoSol.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import aeeL.theoSol.R
import aeeL.theoSol.domain.model.ConsoleState
import aeeL.theoSol.domain.model.FeatureReport
import aeeL.theoSol.domain.model.TopUpOutcome
import aeeL.theoSol.domain.time.DayPeriod
import aeeL.theoSol.ui.components.AppCard
import aeeL.theoSol.ui.components.AppListItem
import aeeL.theoSol.ui.components.BusyPanel
import aeeL.theoSol.ui.components.EmptyPanel
import aeeL.theoSol.ui.components.FeatureReportContent
import aeeL.theoSol.ui.components.HeroCard
import aeeL.theoSol.ui.components.ListDivider
import aeeL.theoSol.ui.components.PrimaryButton
import aeeL.theoSol.ui.components.ScreenColumn
import aeeL.theoSol.ui.components.SecondaryButton
import aeeL.theoSol.ui.components.SectionHeader
import aeeL.theoSol.ui.components.StatTile
import aeeL.theoSol.ui.components.StatusChip
import aeeL.theoSol.ui.components.StatusTone
import aeeL.theoSol.ui.components.dayPeriodLabel
import aeeL.theoSol.ui.components.localizedNumber
import aeeL.theoSol.ui.components.rememberDayPeriod
import aeeL.theoSol.ui.icons.DiamondFilled
import aeeL.theoSol.ui.theme.Dimens
import aeeL.theoSol.ui.theme.Spacing
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    state: ConsoleState,
    onLogin: () -> Unit,
    onRefresh: () -> Unit,
    onSync: () -> Unit,
    onStop: () -> Unit,
    onTopUp: () -> Unit,
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
                        animationSpec = tween(240),
                        expandFrom = Alignment.Top,
                    ),
                exit = fadeOut(animationSpec = tween(140)) +
                    shrinkVertically(
                        animationSpec = tween(200),
                        shrinkTowards = Alignment.Top,
                    ),
            ) {
                AppCard {
                    Text(
                        text = stringResource(R.string.home_ceiling_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(R.string.home_ceiling_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

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
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                    ) {
                        FeaturesSection(
                            report = state.features,
                            busy = state.busy,
                            onSync = onSync,
                        )
                        ToolsSection(
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
                )
                ToolsSection(
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
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
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
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                StatusChip(
                    text = stringResource(R.string.home_signed_in),
                    tone = StatusTone.POSITIVE,
                )
                state.lastAction?.let { lastAction ->
                    Text(
                        text = stringResource(R.string.home_last_action, lastAction),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
    val progress = stats.progress.coerceIn(0f, 1f)
    val secondaryContent = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
    HeroCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.home_balance),
                    style = MaterialTheme.typography.labelLarge,
                    color = secondaryContent,
                )
                Text(
                    text = localizedNumber(balance),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFeatureSettings = "tnum",
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = DiamondFilled,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(Spacing.md)
                        .size(24.dp),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.home_balance_of_limit,
                        localizedNumber(stats.limit),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryContent,
                )
                Text(
                    text = stringResource(
                        R.string.home_progress,
                        (progress * 100f).roundToInt(),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryContent,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.progressHeight)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f),
            )
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.14f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BalanceMetric(
                label = stringResource(R.string.home_earned),
                value = localizedNumber(stats.earned ?: 0L),
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                modifier = Modifier.height(28.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f),
            )
            BalanceMetric(
                label = stringResource(R.string.home_spend),
                value = localizedNumber(stats.spend ?: 0L),
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                modifier = Modifier.height(28.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f),
            )
            BalanceMetric(
                label = stringResource(R.string.home_room),
                value = localizedNumber(stats.room ?: 0L),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BalanceMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontFeatureSettings = "tnum",
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ActionsSection(
    busy: Boolean,
    onRefresh: () -> Unit,
    onTopUp: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionHeader(title = stringResource(R.string.home_actions))
        AppCard {
            PrimaryButton(
                text = stringResource(R.string.action_top_up),
                onClick = onTopUp,
                enabled = !busy,
            )
            SecondaryButton(
                text = stringResource(R.string.action_refresh),
                onClick = onRefresh,
                enabled = !busy,
            )
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
                animationSpec = tween(240),
                expandFrom = Alignment.Top,
            ),
        exit = fadeOut(animationSpec = tween(140)) +
            shrinkVertically(
                animationSpec = tween(200),
                shrinkTowards = Alignment.Top,
            ),
    ) {
        val lastOutcome = outcome ?: shownOutcome ?: return@AnimatedVisibility
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SectionHeader(title = stringResource(R.string.home_last_topup))
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    StatTile(
                        label = stringResource(R.string.home_before),
                        value = localizedNumber(lastOutcome.before),
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = stringResource(R.string.home_amount),
                        value = localizedNumber(lastOutcome.amount),
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = stringResource(R.string.home_after),
                        value = localizedNumber(lastOutcome.after),
                        modifier = Modifier.weight(1f),
                    )
                }
                if (lastOutcome.capped) {
                    StatusChip(
                        text = stringResource(R.string.topup_capped),
                        tone = StatusTone.NEGATIVE,
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturesSection(
    report: FeatureReport?,
    busy: Boolean,
    onSync: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionHeader(title = stringResource(R.string.home_features))
        AppCard {
            if (report == null) {
                Text(
                    text = stringResource(R.string.home_no_features),
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
            SecondaryButton(
                text = stringResource(R.string.action_sync),
                onClick = onSync,
                enabled = !busy,
            )
        }
    }
}

@Composable
private fun ToolsSection(
    onProtocol: () -> Unit,
    onDeveloper: () -> Unit,
    onHelp: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionHeader(title = stringResource(R.string.home_tools))
        AppCard(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            AppListItem(
                title = stringResource(R.string.title_protocol),
                subtitle = stringResource(R.string.home_protocol_summary),
                icon = Icons.Default.List,
                onClick = onProtocol,
            )
            ListDivider()
            AppListItem(
                title = stringResource(R.string.title_developer),
                subtitle = stringResource(R.string.home_developer_summary),
                icon = Icons.Default.Person,
                onClick = onDeveloper,
            )
            ListDivider()
            AppListItem(
                title = stringResource(R.string.title_help),
                subtitle = stringResource(R.string.home_help_summary),
                icon = Icons.Default.Info,
                onClick = onHelp,
            )
        }
    }
}

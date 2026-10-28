package aeeL.theoSol.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import aeeL.theoSol.R
import aeeL.theoSol.domain.model.TopUpOutcome
import aeeL.theoSol.ui.components.AppCard
import aeeL.theoSol.ui.components.FeatureReportContent
import aeeL.theoSol.ui.components.HeroCard
import aeeL.theoSol.ui.components.PrimaryButton
import aeeL.theoSol.ui.components.SectionHeader
import aeeL.theoSol.ui.components.StatTile
import aeeL.theoSol.ui.components.StatusChip
import aeeL.theoSol.ui.components.StatusTone
import aeeL.theoSol.ui.components.localizedNumber
import aeeL.theoSol.ui.theme.Spacing

@Composable
fun TopUpSuccessScreen(
    outcome: TopUpOutcome?,
    onDone: () -> Unit,
) {
    if (outcome == null) {
        LaunchedEffect(Unit) { onDone() }
        return
    }

    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    val badgeScale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.7f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "success badge scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Bottom,
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier
                    .size(112.dp)
                    .scale(badgeScale),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                    )
                }
            }
            Text(
                text = stringResource(R.string.topup_success_title),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.lg),
            )
            Text(
                text = stringResource(R.string.topup_success_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .padding(top = Spacing.sm),
            )
            AnimatedVisibility(
                visible = revealed,
                enter = fadeIn(animationSpec = tween(220)) +
                    slideInVertically(
                        animationSpec = tween(260),
                        initialOffsetY = { it / 12 },
                    ),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                            .padding(top = Spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        HeroCard {
                            Text(
                                text = stringResource(
                                    R.string.topup_success_amount,
                                    localizedNumber(outcome.amount),
                                ),
                                style = MaterialTheme.typography.displaySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            ) {
                                StatTile(
                                    label = stringResource(R.string.home_before),
                                    value = localizedNumber(outcome.before),
                                    modifier = Modifier.weight(1f),
                                )
                                StatTile(
                                    label = stringResource(R.string.home_after),
                                    value = localizedNumber(outcome.after),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (outcome.capped) {
                                StatusChip(
                                    text = stringResource(R.string.topup_capped),
                                    tone = StatusTone.NEGATIVE,
                                )
                            }
                        }
                        SectionHeader(
                            title = stringResource(R.string.topup_features_title),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        AppCard {
                            FeatureReportContent(report = outcome.features)
                        }
                        PrimaryButton(
                            text = stringResource(R.string.topup_success_done),
                            onClick = onDone,
                        )
                    }
                }
            }
        }
    }
}

package aeeL.theoSol.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
            SuccessBadge(revealed = revealed)
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

@Composable
private fun SuccessBadge(revealed: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val scale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "success badge scale",
    )
    val transition = rememberInfiniteTransition(label = "success ring")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "success ring rotation",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "success ring pulse",
    )

    Box(
        modifier = Modifier.size(156.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = primary.copy(alpha = 0.12f),
                radius = size.minDimension * 0.46f * pulse,
            )
            drawCircle(
                color = tertiary.copy(alpha = 0.14f),
                radius = size.minDimension * 0.38f,
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation },
        ) {
            val inset = size.minDimension * 0.08f
            drawArc(
                color = primary.copy(alpha = 0.5f),
                startAngle = 18f,
                sweepAngle = 108f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2f, size.height - inset * 2f),
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                ),
            )
        }
        Surface(
            modifier = Modifier
                .size(112.dp)
                .scale(scale),
            shape = CircleShape,
            color = primary,
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
    }
}

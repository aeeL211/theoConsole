package com.aeeL.theokntl.ui.screens

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aeeL.theokntl.R
import com.aeeL.theokntl.core.Formatters
import com.aeeL.theokntl.domain.model.TopUpOutcome
import com.aeeL.theokntl.ui.components.FeatureReportContent
import com.aeeL.theokntl.ui.components.SectionCard
import com.aeeL.theokntl.ui.components.StatTile
import com.aeeL.theokntl.ui.components.StatusPill
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

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

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val background = MaterialTheme.colorScheme.background
    val confettiColors = listOf(
        primary,
        tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primaryContainer,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        background,
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                    ),
                ),
            ),
    ) {
        Confetti(colors = confettiColors)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Bottom,
                    ),
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            SuccessOrb(revealed = revealed, primary = primary, tertiary = tertiary)
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.topup_success_eyebrow),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp,
            )
            Text(
                text = stringResource(R.string.topup_success_title),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = stringResource(R.string.topup_success_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .padding(top = 8.dp),
            )
            AnimatedVisibility(
                visible = revealed,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 420, delayMillis = 160),
                ) + slideInVertically(
                    animationSpec = tween(durationMillis = 520, delayMillis = 160),
                    initialOffsetY = { it / 4 },
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp)
                        .padding(top = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(
                            R.string.topup_success_amount,
                            Formatters.number(outcome.amount),
                        ),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        StatTile(
                            label = stringResource(R.string.home_before),
                            value = Formatters.number(outcome.before),
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            label = stringResource(R.string.home_after),
                            value = Formatters.number(outcome.after),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (outcome.capped) {
                        StatusPill(
                            text = stringResource(R.string.topup_capped),
                            positive = false,
                        )
                    }
                    SectionCard(title = stringResource(R.string.topup_features_title)) {
                        FeatureReportContent(report = outcome.features)
                    }
                    Button(
                        onClick = onDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                    ) {
                        Text(stringResource(R.string.topup_success_done))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SuccessOrb(
    revealed: Boolean,
    primary: Color,
    tertiary: Color,
) {
    val scale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.55f,
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
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "success ring rotation",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "success ring pulse",
    )

    Box(
        modifier = Modifier.size(164.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = primary.copy(alpha = 0.12f),
                radius = size.minDimension * 0.47f * pulse,
            )
            drawCircle(
                color = tertiary.copy(alpha = 0.18f),
                radius = size.minDimension * 0.35f,
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation },
        ) {
            drawArc(
                color = primary.copy(alpha = 0.55f),
                startAngle = 20f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(size.width * 0.08f, size.height * 0.08f),
                size = androidx.compose.ui.geometry.Size(
                    size.width * 0.84f,
                    size.height * 0.84f,
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx()),
            )
        }
        Surface(
            modifier = Modifier
                .size(112.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            shape = CircleShape,
            color = primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shadowElevation = 12.dp,
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

private data class ConfettiParticle(
    val x: Float,
    val start: Float,
    val speed: Float,
    val sway: Float,
    val radius: Float,
    val colorIndex: Int,
)

@Composable
private fun Confetti(colors: List<Color>) {
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "confetti progress",
    )
    val particles = remember {
        val random = Random(2026)
        List(28) {
            ConfettiParticle(
                x = random.nextFloat(),
                start = random.nextFloat(),
                speed = 0.55f + random.nextFloat() * 0.45f,
                sway = 0.6f + random.nextFloat() * 1.8f,
                radius = 3f + random.nextFloat() * 5f,
                colorIndex = random.nextInt(4),
            )
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val y = ((particle.start + progress * particle.speed) % 1.1f) * size.height
            val drift = sin(progress * 2.0 * PI * particle.sway).toFloat() * 0.045f
            val x = (particle.x + drift).coerceIn(0f, 1f) * size.width
            drawCircle(
                color = colors[particle.colorIndex % colors.size].copy(alpha = 0.3f),
                radius = particle.radius,
                center = Offset(x, y),
            )
        }
    }
}

package aeeL.theoSol.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import aeeL.theoSol.ui.feedback.HapticEffect
import aeeL.theoSol.ui.feedback.rememberHaptics
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun SwipeToConfirm(
    text: String,
    enabled: Boolean,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val haptics = rememberHaptics()
    val scope = rememberCoroutineScope()
    val confirmAction by rememberUpdatedState(onConfirmed)
    var widthPx by remember { mutableIntStateOf(0) }
    var offsetPx by remember { mutableFloatStateOf(0f) }
    var animationJob by remember { mutableStateOf<Job?>(null) }
    var thresholdCrossed by remember { mutableStateOf(false) }

    val thumbSize = 56.dp
    val thumbInset = 8.dp
    val maxOffset = with(density) {
        (widthPx - thumbSize.toPx() - thumbInset.toPx() * 2f).coerceAtLeast(1f)
    }
    val progress = (offsetPx / maxOffset).coerceIn(0f, 1f)
    val trigger = 0.86f
    val errorColor = MaterialTheme.colorScheme.error
    val successColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF81C784)
    } else {
        Color(0xFF2E7D32)
    }
    val activeColor = lerp(errorColor, successColor, progress)
    val activeContent = readableContentColor(activeColor)

    fun settle(
        target: Float,
        animationSpec: AnimationSpec<Float>,
        onFinished: (() -> Unit)? = null,
    ) {
        animationJob?.cancel()
        val start = offsetPx
        animationJob = scope.launch {
            animate(
                initialValue = start,
                targetValue = target,
                animationSpec = animationSpec,
            ) { value, _ ->
                offsetPx = value
            }
            onFinished?.invoke()
        }
    }

    LaunchedEffect(text, enabled, maxOffset) {
        animationJob?.cancel()
        animationJob = null
        offsetPx = 0f
        thresholdCrossed = false
    }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .onSizeChanged { widthPx = it.width }
            .clip(RoundedCornerShape(50))
            .background(
                if (enabled) {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                },
            )
            .pointerInput(enabled, maxOffset) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = {
                        animationJob?.cancel()
                        animationJob = null
                        thresholdCrossed = offsetPx >= maxOffset * trigger
                    },
                    onDragCancel = {
                        settle(
                            target = 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        )
                    },
                    onDragEnd = {
                        if (offsetPx >= maxOffset * trigger) {
                            haptics.perform(HapticEffect.CONFIRM)
                            settle(
                                target = maxOffset,
                                animationSpec = tween(durationMillis = 140),
                            ) {
                                confirmAction()
                            }
                        } else {
                            settle(
                                target = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                ),
                            )
                        }
                    },
                ) { change, dragAmount ->
                    change.consume()
                    val next = (offsetPx + dragAmount).coerceIn(0f, maxOffset)
                    val crossed = next >= maxOffset * trigger
                    if (crossed != thresholdCrossed) {
                        thresholdCrossed = crossed
                        if (crossed) {
                            haptics.perform(HapticEffect.SELECT)
                        }
                    }
                    offsetPx = next
                }
            }
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = text
                if (enabled) {
                    onClick(label = text) {
                        haptics.perform(HapticEffect.CONFIRM)
                        confirmAction()
                        true
                    }
                } else {
                    disabled()
                }
            },
    ) {
        SwipeLabel(
            text = text,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 72.dp),
        )
        if (enabled && progress > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clearAndSetSemantics { }
                    .drawWithContent {
                        clipRect(right = size.width * progress) {
                            this@drawWithContent.drawContent()
                        }
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(50))
                        .background(activeColor),
                )
                SwipeLabel(
                    text = text,
                    color = activeContent,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 72.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetPx.roundToInt(), 0) }
                .padding(thumbInset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(
                    if (enabled) activeColor else MaterialTheme.colorScheme.outline,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = if (enabled) {
                    activeContent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

private val DarkContent = Color(0xFF0B1F10)

@Composable
private fun SwipeLabel(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.SemiBold,
        ),
        color = color,
        maxLines = 2,
        textAlign = TextAlign.Center,
    )
}

private fun readableContentColor(background: Color): Color {
    val backgroundLuminance = background.luminance()
    val darkContrast = (backgroundLuminance + 0.05f) / (DarkContent.luminance() + 0.05f)
    val lightContrast = 1.05f / (backgroundLuminance + 0.05f)
    return if (darkContrast >= lightContrast) DarkContent else Color.White
}

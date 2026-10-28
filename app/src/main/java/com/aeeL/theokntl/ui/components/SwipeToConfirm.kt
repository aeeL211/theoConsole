package com.aeeL.theokntl.ui.components

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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val confirmAction by rememberUpdatedState(onConfirmed)
    var widthPx by remember { mutableIntStateOf(0) }
    var offsetPx by remember { mutableFloatStateOf(0f) }
    var animationJob by remember { mutableStateOf<Job?>(null) }

    val thumbSize = 56.dp
    val thumbInset = 8.dp
    val maxOffset = with(density) {
        (widthPx - thumbSize.toPx() - thumbInset.toPx() * 2f).coerceAtLeast(1f)
    }
    val progress = (offsetPx / maxOffset).coerceIn(0f, 1f)
    val trigger = 0.86f

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
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
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
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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
                    offsetPx = (offsetPx + dragAmount).coerceIn(0f, maxOffset)
                }
            }
            .semantics {
                role = Role.Button
                contentDescription = text
                if (enabled) {
                    onClick(label = text) {
                        confirmAction()
                        true
                    }
                } else {
                    disabled()
                }
            },
    ) {
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                            ),
                        ),
                    ),
            )
        }
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 72.dp),
            style = MaterialTheme.typography.labelLarge,
            color = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                progress > 0.52f -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 2,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetPx.roundToInt(), 0) }
                .padding(thumbInset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(
                    if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

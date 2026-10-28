package com.aeeL.theokntl.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
    val offset = remember { Animatable(0f) }
    var widthPx by remember { mutableIntStateOf(0) }
    var dragTarget by remember { mutableFloatStateOf(0f) }

    val thumbSize = 56.dp
    val thumbInset = 8.dp
    val maxOffset = with(density) {
        (widthPx - thumbSize.toPx() - thumbInset.toPx() * 2f).coerceAtLeast(1f)
    }
    val progress = (offset.value / maxOffset).coerceIn(0f, 1f)
    val trigger = 0.86f

    LaunchedEffect(text, enabled, maxOffset) {
        dragTarget = 0f
        offset.snapTo(0f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .onSizeChanged { widthPx = it.width }
            .clip(RoundedCornerShape(50))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = if (enabled) 0.85f else 0.4f,
                ),
            )
            .pointerInput(enabled, maxOffset) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { dragTarget = offset.value },
                    onDragCancel = {
                        dragTarget = 0f
                        scope.launch {
                            offset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                ),
                            )
                        }
                    },
                    onDragEnd = {
                        if (dragTarget >= maxOffset * trigger) {
                            dragTarget = maxOffset
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                offset.animateTo(maxOffset, tween(durationMillis = 140))
                                onConfirmed()
                            }
                        } else {
                            dragTarget = 0f
                            scope.launch {
                                offset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow,
                                    ),
                                )
                            }
                        }
                    },
                ) { change, dragAmount ->
                    change.consume()
                    dragTarget = (dragTarget + dragAmount).coerceIn(0f, maxOffset)
                    scope.launch {
                        offset.snapTo(dragTarget)
                    }
                }
            }
            .semantics {
                role = Role.Button
                contentDescription = text
                if (enabled) {
                    onClick(label = text) {
                        onConfirmed()
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
                .offset { IntOffset(offset.value.roundToInt(), 0) }
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

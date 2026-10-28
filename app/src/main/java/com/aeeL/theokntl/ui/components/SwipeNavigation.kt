package com.aeeL.theokntl.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

fun Modifier.swipeToNavigate(
    enabled: Boolean,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    thresholdDp: Float = 72f,
): Modifier {
    if (!enabled) return this
    return pointerInput(enabled, thresholdDp) {
        var total = 0f
        detectHorizontalDragGestures(
            onDragStart = { total = 0f },
            onDragCancel = { total = 0f },
            onDragEnd = {
                val threshold = thresholdDp.dp.toPx()
                when {
                    total <= -threshold -> onSwipeLeft()
                    total >= threshold -> onSwipeRight()
                }
                total = 0f
            },
        ) { change, dragAmount ->
            change.consume()
            total += dragAmount
        }
    }
}

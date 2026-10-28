package com.aeeL.theokntl.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Material Symbols "diamond", re-scaled from the 960-unit grid to a 24-unit viewport.
val DiamondFilled: ImageVector = ImageVector.Builder(
    name = "DiamondFilled",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(9.2f, 8.25f)
        lineTo(11.85f, 3f)
        lineTo(12.15f, 3f)
        lineTo(14.8f, 8.25f)
        close()
        moveTo(11.25f, 20.1f)
        lineTo(2.625f, 9.75f)
        lineTo(11.25f, 9.75f)
        close()
        moveTo(12.75f, 20.1f)
        lineTo(12.75f, 9.75f)
        lineTo(21.375f, 9.75f)
        close()
        moveTo(16.45f, 8.25f)
        lineTo(13.85f, 3f)
        lineTo(19f, 3f)
        lineTo(21.625f, 8.25f)
        close()
        moveTo(2.375f, 8.25f)
        lineTo(5f, 3f)
        lineTo(10.15f, 3f)
        lineTo(7.55f, 8.25f)
        close()
    }
}.build()

val DiamondOutlined: ImageVector = ImageVector.Builder(
    name = "DiamondOutlined",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(12f, 21f)
        lineTo(2f, 9f)
        lineTo(5f, 3f)
        lineTo(19f, 3f)
        lineTo(22f, 9f)
        close()
        moveTo(9.625f, 8f)
        lineTo(14.375f, 8f)
        lineTo(12.875f, 5f)
        lineTo(11.125f, 5f)
        close()
        moveTo(11f, 16.675f)
        lineTo(11f, 10f)
        lineTo(5.45f, 10f)
        close()
        moveTo(13f, 16.675f)
        lineTo(18.55f, 10f)
        lineTo(13f, 10f)
        close()
        moveTo(16.6f, 8f)
        lineTo(19.25f, 8f)
        lineTo(17.75f, 5f)
        lineTo(15.1f, 5f)
        close()
        moveTo(4.75f, 8f)
        lineTo(7.4f, 8f)
        lineTo(8.9f, 5f)
        lineTo(6.25f, 5f)
        close()
    }
}.build()

package aeeL.theoSol.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import aeeL.theoSol.domain.model.ThemePalette

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object Dimens {
    val topBarHeight = 60.dp
    val buttonHeight = 52.dp
    val listItemMinHeight = 56.dp
    val avatar = 56.dp
    val progressHeight = 8.dp
    val paletteSwatch = 44.dp
    val selectedBorder = 2.dp
    val defaultBorder = 1.dp
}

val PillShape = CircleShape

private val PaletteAccents = mapOf(
    ThemePalette.TEAL to Color(0xFF00695C),
    ThemePalette.TANGERINE to Color(0xFFB33A00),
    ThemePalette.GRAPE to Color(0xFF6B4E9B),
    ThemePalette.OCEAN to Color(0xFF00639B),
    ThemePalette.ROSE to Color(0xFFA23F5E),
)

fun paletteAccent(palette: ThemePalette): Color =
    PaletteAccents.getValue(palette)

fun paletteOnAccent(palette: ThemePalette): Color =
    if (paletteAccent(palette).luminance() > 0.4f) Color(0xFF101413) else Color.White

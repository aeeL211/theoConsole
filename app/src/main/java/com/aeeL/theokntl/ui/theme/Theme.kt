package com.aeeL.theokntl.ui.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.aeeL.theokntl.R
import com.aeeL.theokntl.domain.model.ThemeMode
import com.aeeL.theokntl.domain.model.ThemePalette

private data class PaletteColors(
    val light: ColorScheme,
    val dark: ColorScheme,
)

private val TealPalette = PaletteColors(
    light = lightColorScheme(
        primary = Color(0xFF00695C),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFA5F2E4),
        onPrimaryContainer = Color(0xFF00201B),
        secondary = Color(0xFF4A635F),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCCE8E2),
        onSecondaryContainer = Color(0xFF06201C),
        tertiary = Color(0xFF7A5900),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDF9C),
        onTertiaryContainer = Color(0xFF261A00),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFAFDFB),
        onBackground = Color(0xFF191C1B),
        surface = Color(0xFFFAFDFB),
        onSurface = Color(0xFF191C1B),
        surfaceVariant = Color(0xFFDAE5E1),
        onSurfaceVariant = Color(0xFF3F4946),
        outline = Color(0xFF6F7976),
    ),
    dark = darkColorScheme(
        primary = Color(0xFF89D6C8),
        onPrimary = Color(0xFF003730),
        primaryContainer = Color(0xFF005046),
        onPrimaryContainer = Color(0xFFA5F2E4),
        secondary = Color(0xFFB1CCC6),
        onSecondary = Color(0xFF1C3531),
        secondaryContainer = Color(0xFF324B47),
        onSecondaryContainer = Color(0xFFCCE8E2),
        tertiary = Color(0xFFF0C048),
        onTertiary = Color(0xFF402D00),
        tertiaryContainer = Color(0xFF5C4300),
        onTertiaryContainer = Color(0xFFFFDF9C),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF101413),
        onBackground = Color(0xFFE0E3E1),
        surface = Color(0xFF101413),
        onSurface = Color(0xFFE0E3E1),
        surfaceVariant = Color(0xFF3F4946),
        onSurfaceVariant = Color(0xFFBEC9C5),
        outline = Color(0xFF89938F),
    ),
)

private val TangerinePalette = PaletteColors(
    light = lightColorScheme(
        primary = Color(0xFFB33A00),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDBCF),
        onPrimaryContainer = Color(0xFF3B0900),
        secondary = Color(0xFF77574C),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDBD0),
        onSecondaryContainer = Color(0xFF2C160E),
        tertiary = Color(0xFF6D5D00),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF9E287),
        onTertiaryContainer = Color(0xFF211B00),
        background = Color(0xFFFFF8F6),
        onBackground = Color(0xFF221A17),
        surface = Color(0xFFFFF8F6),
        onSurface = Color(0xFF221A17),
        surfaceVariant = Color(0xFFF5DED6),
        onSurfaceVariant = Color(0xFF53433E),
        outline = Color(0xFF85736D),
    ),
    dark = darkColorScheme(
        primary = Color(0xFFFFB599),
        onPrimary = Color(0xFF5F1600),
        primaryContainer = Color(0xFF862200),
        onPrimaryContainer = Color(0xFFFFDBCF),
        secondary = Color(0xFFE7BEB1),
        onSecondary = Color(0xFF442A21),
        secondaryContainer = Color(0xFF5D4036),
        onSecondaryContainer = Color(0xFFFFDBD0),
        tertiary = Color(0xFFDCC66D),
        onTertiary = Color(0xFF393000),
        tertiaryContainer = Color(0xFF524600),
        onTertiaryContainer = Color(0xFFF9E287),
        background = Color(0xFF1A110E),
        onBackground = Color(0xFFF1DFDA),
        surface = Color(0xFF1A110E),
        onSurface = Color(0xFFF1DFDA),
        surfaceVariant = Color(0xFF53433E),
        onSurfaceVariant = Color(0xFFD8C2BB),
        outline = Color(0xFFA08C86),
    ),
)

private val GrapePalette = PaletteColors(
    light = lightColorScheme(
        primary = Color(0xFF6B4E9B),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF625B71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        tertiary = Color(0xFF7D5260),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD8E4),
        onTertiaryContainer = Color(0xFF31111D),
        background = Color(0xFFFEF7FF),
        onBackground = Color(0xFF1D1B20),
        surface = Color(0xFFFEF7FF),
        onSurface = Color(0xFF1D1B20),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF79747E),
    ),
    dark = darkColorScheme(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFCCC2DC),
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        tertiary = Color(0xFFEFB8C8),
        onTertiary = Color(0xFF492532),
        tertiaryContainer = Color(0xFF633B48),
        onTertiaryContainer = Color(0xFFFFD8E4),
        background = Color(0xFF141218),
        onBackground = Color(0xFFE6E0E9),
        surface = Color(0xFF141218),
        onSurface = Color(0xFFE6E0E9),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF938F99),
    ),
)

private val OceanPalette = PaletteColors(
    light = lightColorScheme(
        primary = Color(0xFF00639B),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFCEE5FF),
        onPrimaryContainer = Color(0xFF001D33),
        secondary = Color(0xFF50606E),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD3E5F5),
        onSecondaryContainer = Color(0xFF0C1D29),
        tertiary = Color(0xFF5F5C7D),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE5DFFF),
        onTertiaryContainer = Color(0xFF1B1735),
        background = Color(0xFFF8F9FF),
        onBackground = Color(0xFF181C20),
        surface = Color(0xFFF8F9FF),
        onSurface = Color(0xFF181C20),
        surfaceVariant = Color(0xFFDDE3EA),
        onSurfaceVariant = Color(0xFF41474D),
        outline = Color(0xFF71787E),
    ),
    dark = darkColorScheme(
        primary = Color(0xFF96CCFF),
        onPrimary = Color(0xFF003353),
        primaryContainer = Color(0xFF004A76),
        onPrimaryContainer = Color(0xFFCEE5FF),
        secondary = Color(0xFFB7C9D9),
        onSecondary = Color(0xFF22323F),
        secondaryContainer = Color(0xFF384956),
        onSecondaryContainer = Color(0xFFD3E5F5),
        tertiary = Color(0xFFC9C3EA),
        onTertiary = Color(0xFF302D4A),
        tertiaryContainer = Color(0xFF47445F),
        onTertiaryContainer = Color(0xFFE5DFFF),
        background = Color(0xFF0E1418),
        onBackground = Color(0xFFDEE3E8),
        surface = Color(0xFF0E1418),
        onSurface = Color(0xFFDEE3E8),
        surfaceVariant = Color(0xFF41474D),
        onSurfaceVariant = Color(0xFFC1C7CE),
        outline = Color(0xFF8B9198),
    ),
)

private val RosePalette = PaletteColors(
    light = lightColorScheme(
        primary = Color(0xFFA23F5E),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD9E2),
        onPrimaryContainer = Color(0xFF3F001F),
        secondary = Color(0xFF74565E),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFD9E2),
        onSecondaryContainer = Color(0xFF2B151B),
        tertiary = Color(0xFF7C5635),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDCC1),
        onTertiaryContainer = Color(0xFF2D1600),
        background = Color(0xFFFFF8F8),
        onBackground = Color(0xFF22191C),
        surface = Color(0xFFFFF8F8),
        onSurface = Color(0xFF22191C),
        surfaceVariant = Color(0xFFF2DDE1),
        onSurfaceVariant = Color(0xFF514347),
        outline = Color(0xFF837378),
    ),
    dark = darkColorScheme(
        primary = Color(0xFFFFB1C6),
        onPrimary = Color(0xFF5F112F),
        primaryContainer = Color(0xFF7F2945),
        onPrimaryContainer = Color(0xFFFFD9E2),
        secondary = Color(0xFFE2BDC5),
        onSecondary = Color(0xFF422A31),
        secondaryContainer = Color(0xFF5A3F46),
        onSecondaryContainer = Color(0xFFFFD9E2),
        tertiary = Color(0xFFEFBD94),
        onTertiary = Color(0xFF48290B),
        tertiaryContainer = Color(0xFF613F21),
        onTertiaryContainer = Color(0xFFFFDCC1),
        background = Color(0xFF191113),
        onBackground = Color(0xFFF0DEE1),
        surface = Color(0xFF191113),
        onSurface = Color(0xFFF0DEE1),
        surfaceVariant = Color(0xFF514347),
        onSurfaceVariant = Color(0xFFD5C2C6),
        outline = Color(0xFF9E8C90),
    ),
)

private val RobotoFlex = FontFamily(
    Font(R.font.roboto_flex, FontWeight.Thin),
    Font(R.font.roboto_flex, FontWeight.ExtraLight),
    Font(R.font.roboto_flex, FontWeight.Light),
    Font(R.font.roboto_flex, FontWeight.Normal),
    Font(R.font.roboto_flex, FontWeight.Medium),
    Font(R.font.roboto_flex, FontWeight.SemiBold),
    Font(R.font.roboto_flex, FontWeight.Bold),
    Font(R.font.roboto_flex, FontWeight.ExtraBold),
    Font(R.font.roboto_flex, FontWeight.Black),
)

private fun Typography.withRobotoFlex(): Typography = Typography(
    displayLarge = displayLarge.copy(fontFamily = RobotoFlex),
    displayMedium = displayMedium.copy(fontFamily = RobotoFlex),
    displaySmall = displaySmall.copy(fontFamily = RobotoFlex),
    headlineLarge = headlineLarge.copy(fontFamily = RobotoFlex),
    headlineMedium = headlineMedium.copy(fontFamily = RobotoFlex),
    headlineSmall = headlineSmall.copy(fontFamily = RobotoFlex),
    titleLarge = titleLarge.copy(fontFamily = RobotoFlex),
    titleMedium = titleMedium.copy(fontFamily = RobotoFlex),
    titleSmall = titleSmall.copy(fontFamily = RobotoFlex),
    bodyLarge = bodyLarge.copy(fontFamily = RobotoFlex),
    bodyMedium = bodyMedium.copy(fontFamily = RobotoFlex),
    bodySmall = bodySmall.copy(fontFamily = RobotoFlex),
    labelLarge = labelLarge.copy(fontFamily = RobotoFlex),
    labelMedium = labelMedium.copy(fontFamily = RobotoFlex),
    labelSmall = labelSmall.copy(fontFamily = RobotoFlex),
)

private val TheoTypography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(
            fontWeight = FontWeight.Black,
            fontSize = 56.sp,
            lineHeight = 60.sp,
            letterSpacing = (-1.4).sp,
        ),
        displayMedium = base.displayMedium.copy(
            fontWeight = FontWeight.Black,
            fontSize = 44.sp,
            lineHeight = 48.sp,
            letterSpacing = (-1.0).sp,
        ),
        displaySmall = base.displaySmall.copy(
            fontWeight = FontWeight.Black,
            fontSize = 36.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.7).sp,
        ),
        headlineLarge = base.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.4).sp,
        ),
        headlineMedium = base.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.2).sp,
        ),
        headlineSmall = base.headlineSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 30.sp,
        ),
        titleLarge = base.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        titleMedium = base.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        ),
        titleSmall = base.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
        ),
        bodyLarge = base.bodyLarge.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.1.sp,
        ),
        bodyMedium = base.bodyMedium.copy(
            fontSize = 14.sp,
            lineHeight = 21.sp,
            letterSpacing = 0.15.sp,
        ),
        bodySmall = base.bodySmall.copy(
            fontSize = 12.sp,
            lineHeight = 17.sp,
            letterSpacing = 0.2.sp,
        ),
        labelLarge = base.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 0.2.sp,
        ),
        labelMedium = base.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 0.7.sp,
        ),
        labelSmall = base.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.0.sp,
        ),
    )
}.withRobotoFlex()

private val TheoShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

private fun ColorScheme.withExpressiveSurfaces(): ColorScheme = copy(
    surfaceTint = primary,
    inverseSurface = onSurface,
    inverseOnSurface = surface,
    inversePrimary = primaryContainer,
    surfaceDim = lerp(surface, Color.Black, 0.08f),
    surfaceBright = lerp(surface, Color.White, 0.08f),
    surfaceContainerLowest = surface,
    surfaceContainerLow = lerp(surface, surfaceVariant, 0.18f),
    surfaceContainer = lerp(surface, surfaceVariant, 0.32f),
    surfaceContainerHigh = lerp(surface, surfaceVariant, 0.48f),
    surfaceContainerHighest = lerp(surface, surfaceVariant, 0.64f),
    outlineVariant = lerp(outline, surfaceVariant, 0.5f),
    scrim = Color.Black,
)

private fun PaletteColors.withExpressiveSurfaces(): PaletteColors = PaletteColors(
    light = light.withExpressiveSurfaces(),
    dark = dark.withExpressiveSurfaces(),
)

private fun paletteColors(palette: ThemePalette): PaletteColors = when (palette) {
    ThemePalette.TEAL -> TealPalette
    ThemePalette.TANGERINE -> TangerinePalette
    ThemePalette.GRAPE -> GrapePalette
    ThemePalette.OCEAN -> OceanPalette
    ThemePalette.ROSE -> RosePalette
}.withExpressiveSurfaces()

@Composable
fun TheoConsoleTheme(
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    palette: ThemePalette = ThemePalette.TEAL,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    val paletteScheme = paletteColors(palette)
    val colors = when {
        dynamicColor && supportsDynamic && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && supportsDynamic -> dynamicLightColorScheme(context)
        darkTheme -> paletteScheme.dark
        else -> paletteScheme.light
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            activity.window.setBackgroundDrawable(ColorDrawable(colors.background.toArgb()))
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = TheoTypography,
        shapes = TheoShapes,
        content = content,
    )
}

package aeeL.theoSol.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/**
 * Formats a quantity using the locale that is currently applied to the UI.
 * Protocol logs and machine values keep their stable formatting.
 */
@Composable
fun localizedNumber(value: Long): String {
    val locales = LocalConfiguration.current.locales
    val locale = if (locales.size() > 0) locales.get(0) else Locale.ROOT
    return remember(value, locale) { formatNumber(value, locale) }
}

private fun formatNumber(value: Long, locale: Locale): String =
    String.format(locale, "%,d", value)

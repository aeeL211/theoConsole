package com.aeeL.theokntl.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aeeL.theokntl.BuildConfig
import com.aeeL.theokntl.R
import com.aeeL.theokntl.domain.model.AppLanguage
import com.aeeL.theokntl.domain.model.AppSettings
import com.aeeL.theokntl.domain.model.ThemeMode
import com.aeeL.theokntl.domain.model.ThemePalette
import com.aeeL.theokntl.ui.components.LabeledValue
import com.aeeL.theokntl.ui.components.ScreenColumn
import com.aeeL.theokntl.ui.components.SectionCard

@Composable
fun SettingsScreen(
    settings: AppSettings,
    language: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onPalette: (ThemePalette) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    onDebugDetails: (Boolean) -> Unit,
    onHelp: () -> Unit,
    onProtocol: () -> Unit,
    onDeveloper: () -> Unit,
) {
    val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    ScreenColumn {
        SectionCard(title = stringResource(R.string.settings_language)) {
            Text(
                text = stringResource(R.string.settings_language_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppLanguage.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguage(option) }
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RadioButton(
                        selected = language == option,
                        onClick = { onLanguage(option) },
                    )
                    Text(
                        text = stringResource(languageLabel(option)),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        SectionCard(title = stringResource(R.string.settings_theme)) {
            ThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeMode(mode) }
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RadioButton(
                        selected = settings.themeMode == mode,
                        onClick = { onThemeMode(mode) },
                    )
                    Text(
                        text = stringResource(themeModeLabel(mode)),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        SectionCard(title = stringResource(R.string.settings_palette)) {
            Text(
                text = stringResource(R.string.settings_palette_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ThemePalette.entries.forEach { option ->
                    val selected = settings.palette == option
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPalette(option) }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(palettePreview(option), CircleShape)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                )
                            }
                        }
                        Text(
                            text = stringResource(paletteLabel(option)),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_dynamic_color),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(
                            if (dynamicSupported) {
                                R.string.settings_dynamic_color_hint
                            } else {
                                R.string.settings_dynamic_color_unsupported
                            },
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.dynamicColor && dynamicSupported,
                    onCheckedChange = onDynamicColor,
                    enabled = dynamicSupported,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_debug),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.settings_debug_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.debugDetails,
                    onCheckedChange = onDebugDetails,
                )
            }
        }
        SectionCard(title = stringResource(R.string.settings_about)) {
            OutlinedButton(
                onClick = onHelp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.title_help))
            }
            OutlinedButton(
                onClick = onProtocol,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.title_protocol))
            }
            OutlinedButton(
                onClick = onDeveloper,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.title_developer))
            }
            LabeledValue(
                label = stringResource(R.string.settings_version),
                value = stringResource(
                    R.string.version_line,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                ),
                mono = true,
            )
            Text(
                text = stringResource(R.string.settings_build_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun themeModeLabel(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}

private fun languageLabel(language: AppLanguage): Int = when (language) {
    AppLanguage.SYSTEM -> R.string.settings_language_system
    AppLanguage.ENGLISH -> R.string.settings_language_english
    AppLanguage.INDONESIAN -> R.string.settings_language_indonesian
}

private fun paletteLabel(palette: ThemePalette): Int = when (palette) {
    ThemePalette.TEAL -> R.string.settings_palette_teal
    ThemePalette.TANGERINE -> R.string.settings_palette_tangerine
    ThemePalette.GRAPE -> R.string.settings_palette_grape
    ThemePalette.OCEAN -> R.string.settings_palette_ocean
    ThemePalette.ROSE -> R.string.settings_palette_rose
}

private fun palettePreview(palette: ThemePalette): Color = when (palette) {
    ThemePalette.TEAL -> Color(0xFF00695C)
    ThemePalette.TANGERINE -> Color(0xFFB33A00)
    ThemePalette.GRAPE -> Color(0xFF6B4E9B)
    ThemePalette.OCEAN -> Color(0xFF00639B)
    ThemePalette.ROSE -> Color(0xFFA23F5E)
}

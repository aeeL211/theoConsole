package aeeL.theoSol.ui.screens

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import aeeL.theoSol.BuildConfig
import aeeL.theoSol.R
import aeeL.theoSol.domain.model.AppLanguage
import aeeL.theoSol.domain.model.AppSettings
import aeeL.theoSol.domain.model.ThemeMode
import aeeL.theoSol.domain.model.ThemePalette
import aeeL.theoSol.ui.components.AppCard
import aeeL.theoSol.ui.components.AppListItem
import aeeL.theoSol.ui.components.LabeledValue
import aeeL.theoSol.ui.components.ListDivider
import aeeL.theoSol.ui.components.ScreenColumn
import aeeL.theoSol.ui.components.SectionHeader
import aeeL.theoSol.ui.components.SettingsSwitchRow
import aeeL.theoSol.ui.feedback.HapticEffect
import aeeL.theoSol.ui.feedback.rememberHaptics
import aeeL.theoSol.ui.theme.Dimens
import aeeL.theoSol.ui.theme.Spacing
import aeeL.theoSol.ui.theme.paletteAccent
import aeeL.theoSol.ui.theme.paletteOnAccent

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
    val haptics = rememberHaptics()
    val languageOptions = listOf(
        AppLanguage.SYSTEM to stringResource(R.string.settings_language_system),
        AppLanguage.ENGLISH to stringResource(R.string.settings_language_english),
        AppLanguage.INDONESIAN to stringResource(R.string.settings_language_indonesian),
    )
    val themeOptions = listOf(
        ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
        ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
        ThemeMode.DARK to stringResource(R.string.settings_theme_dark),
    )

    ScreenColumn {
        SectionHeader(
            title = stringResource(R.string.settings_language),
            supporting = stringResource(R.string.settings_language_hint),
        )
        AppCard(contentPadding = PaddingValues(0.dp)) {
            ChoiceRow(
                title = stringResource(R.string.settings_language),
                value = stringResource(languageLabel(language)),
                options = languageOptions,
                onSelected = onLanguage,
            )
        }
        SectionHeader(title = stringResource(R.string.settings_theme))
        AppCard(contentPadding = PaddingValues(0.dp)) {
            ChoiceRow(
                title = stringResource(R.string.settings_theme),
                value = stringResource(themeModeLabel(settings.themeMode)),
                options = themeOptions,
                onSelected = onThemeMode,
            )
        }
        SectionHeader(
            title = stringResource(R.string.settings_palette),
            supporting = stringResource(R.string.settings_palette_hint),
        )
        AppCard(contentPadding = PaddingValues(vertical = Spacing.lg)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                ThemePalette.entries.forEach { option ->
                    val selected = settings.palette == option
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = {
                                    haptics.perform(HapticEffect.SELECT)
                                    onPalette(option)
                                },
                            )
                            .padding(vertical = Spacing.xs),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Dimens.paletteSwatch)
                                .background(paletteAccent(option), CircleShape)
                                .border(
                                    width = if (selected) {
                                        Dimens.selectedBorder
                                    } else {
                                        Dimens.defaultBorder
                                    },
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
                                    tint = paletteOnAccent(option),
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
            if (dynamicSupported) {
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_dynamic_color),
                    subtitle = stringResource(R.string.settings_dynamic_color_hint),
                    checked = settings.dynamicColor,
                    onCheckedChange = onDynamicColor,
                )
            }
        }
        SectionHeader(title = stringResource(R.string.settings_appearance))
        AppCard(contentPadding = PaddingValues(0.dp)) {
            SettingsSwitchRow(
                title = stringResource(R.string.settings_debug),
                subtitle = stringResource(R.string.settings_debug_hint),
                checked = settings.debugDetails,
                onCheckedChange = onDebugDetails,
            )
        }
        SectionHeader(title = stringResource(R.string.settings_about))
        AppCard(
            contentPadding = PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            AppListItem(
                title = stringResource(R.string.title_help),
                subtitle = stringResource(R.string.home_help_summary),
                icon = Icons.Default.Info,
                onClick = onHelp,
            )
            ListDivider()
            AppListItem(
                title = stringResource(R.string.title_protocol),
                subtitle = stringResource(R.string.home_protocol_summary),
                icon = Icons.Default.List,
                onClick = onProtocol,
            )
            ListDivider()
            AppListItem(
                title = stringResource(R.string.title_developer),
                subtitle = stringResource(R.string.home_developer_summary),
                icon = Icons.Default.Person,
                onClick = onDeveloper,
            )
            ListDivider()
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                LabeledValue(
                    label = stringResource(R.string.settings_version),
                    value = stringResource(
                        R.string.version_line,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE,
                    ),
                )
                Text(
                    text = stringResource(R.string.settings_build_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun <T> ChoiceRow(
    title: String,
    value: String,
    options: List<Pair<T, String>>,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val haptics = rememberHaptics()
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.listItemMinHeight)
                .clickable(role = Role.Button) {
                    haptics.perform(HapticEffect.TAP)
                    expanded = true
                }
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (option, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        haptics.perform(HapticEffect.SELECT)
                        onSelected(option)
                    },
                )
            }
        }
    }
}

@StringRes
private fun themeModeLabel(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}

@StringRes
private fun languageLabel(language: AppLanguage): Int = when (language) {
    AppLanguage.SYSTEM -> R.string.settings_language_system
    AppLanguage.ENGLISH -> R.string.settings_language_english
    AppLanguage.INDONESIAN -> R.string.settings_language_indonesian
}

@StringRes
private fun paletteLabel(palette: ThemePalette): Int = when (palette) {
    ThemePalette.TEAL -> R.string.settings_palette_teal
    ThemePalette.TANGERINE -> R.string.settings_palette_tangerine
    ThemePalette.GRAPE -> R.string.settings_palette_grape
    ThemePalette.OCEAN -> R.string.settings_palette_ocean
    ThemePalette.ROSE -> R.string.settings_palette_rose
}

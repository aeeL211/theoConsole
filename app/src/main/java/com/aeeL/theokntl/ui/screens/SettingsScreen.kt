package com.aeeL.theokntl.ui.screens

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aeeL.theokntl.BuildConfig
import com.aeeL.theokntl.R
import com.aeeL.theokntl.domain.model.AppSettings
import com.aeeL.theokntl.domain.model.ThemeMode
import com.aeeL.theokntl.ui.components.LabeledValue
import com.aeeL.theokntl.ui.components.ScreenColumn
import com.aeeL.theokntl.ui.components.SectionCard

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeMode: (ThemeMode) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    onDebugDetails: (Boolean) -> Unit,
    onHelp: () -> Unit,
    onProtocol: () -> Unit,
    onDeveloper: () -> Unit,
) {
    val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    ScreenColumn {
        SectionCard(title = stringResource(R.string.settings_theme)) {
            ThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeMode(mode) }
                        .padding(vertical = 4.dp),
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
        SectionCard(title = stringResource(R.string.settings_appearance)) {
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

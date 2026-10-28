package com.aeeL.theokntl.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aeeL.theokntl.BuildConfig
import com.aeeL.theokntl.R
import com.aeeL.theokntl.ui.components.LabeledValue
import com.aeeL.theokntl.ui.components.ScreenColumn
import com.aeeL.theokntl.ui.components.SectionCard

@Composable
fun HelpScreen() {
    ScreenColumn {
        Text(
            text = stringResource(R.string.help_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HelpSection(
            title = stringResource(R.string.help_section_start),
            body = stringResource(R.string.help_start_body),
        )
        HelpSection(
            title = stringResource(R.string.help_section_topup),
            body = stringResource(R.string.help_topup_body),
        )
        HelpSection(
            title = stringResource(R.string.help_section_features),
            body = stringResource(R.string.help_features_body),
        )
        HelpSection(
            title = stringResource(R.string.help_section_security),
            body = stringResource(R.string.help_security_body),
        )
        HelpSection(
            title = stringResource(R.string.help_section_build),
            body = stringResource(R.string.help_build_body),
        )
        SectionCard(title = stringResource(R.string.settings_version)) {
            LabeledValue(
                label = stringResource(R.string.settings_version),
                value = stringResource(
                    R.string.version_line,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                ),
            )
        }
    }
}

@Composable
private fun HelpSection(title: String, body: String) {
    SectionCard(title = title) {
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

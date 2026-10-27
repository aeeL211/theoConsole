package com.aeeL.theokntl.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.aeeL.theokntl.BuildConfig
import com.aeeL.theokntl.R
import com.aeeL.theokntl.core.Formatters
import com.aeeL.theokntl.domain.model.developerView
import com.aeeL.theokntl.ui.components.LabeledValue
import com.aeeL.theokntl.ui.components.ScreenColumn
import com.aeeL.theokntl.ui.components.SectionCard

@Composable
fun DeveloperScreen() {
    val developer = remember { developerView() }
    val uriHandler = LocalUriHandler.current
    ScreenColumn {
        SectionCard(title = stringResource(R.string.developer_title)) {
            LabeledValue(
                label = stringResource(R.string.developer_name),
                value = developer.name,
            )
            LabeledValue(
                label = stringResource(R.string.developer_telegram),
                value = developer.telegramLabel,
                mono = true,
            )
            Button(
                onClick = { runCatching { uriHandler.openUri(developer.telegramUrl) } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_open_telegram))
            }
            Text(
                text = stringResource(R.string.developer_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SectionCard(title = stringResource(R.string.developer_build)) {
            LabeledValue(
                label = stringResource(R.string.settings_version),
                value = stringResource(
                    R.string.version_line,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                ),
                mono = true,
            )
            LabeledValue(
                label = stringResource(R.string.developer_topup_limit),
                value = Formatters.number(com.aeeL.theokntl.core.ConsoleConstants.LIMIT),
                mono = true,
            )
        }
    }
}

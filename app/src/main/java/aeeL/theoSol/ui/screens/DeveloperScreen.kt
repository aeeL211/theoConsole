package aeeL.theoSol.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import aeeL.theoSol.BuildConfig
import aeeL.theoSol.R
import aeeL.theoSol.core.ConsoleConstants
import aeeL.theoSol.domain.model.developerView
import aeeL.theoSol.ui.components.AppCard
import aeeL.theoSol.ui.components.HeroCard
import aeeL.theoSol.ui.components.PrimaryButton
import aeeL.theoSol.ui.components.ScreenColumn
import aeeL.theoSol.ui.components.SectionHeader
import aeeL.theoSol.ui.components.StatTile
import aeeL.theoSol.ui.components.localizedNumber
import aeeL.theoSol.ui.theme.Dimens
import aeeL.theoSol.ui.theme.Spacing

@Composable
fun DeveloperScreen() {
    val developer = remember { developerView() }
    val uriHandler = LocalUriHandler.current
    ScreenColumn {
        HeroCard(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                Surface(
                    modifier = Modifier.size(Dimens.avatar),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Text(
                        text = stringResource(R.string.title_developer),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = developer.name,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = developer.telegramLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = stringResource(R.string.developer_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PrimaryButton(
                text = stringResource(R.string.action_open_telegram),
                onClick = {
                    runCatching { uriHandler.openUri(developer.telegramUrl) }
                },
            )
        }
        SectionHeader(title = stringResource(R.string.developer_build))
        AppCard {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                StatTile(
                    label = stringResource(R.string.settings_version),
                    value = stringResource(
                        R.string.version_line,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE,
                    ),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.developer_topup_limit),
                    value = localizedNumber(ConsoleConstants.LIMIT),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

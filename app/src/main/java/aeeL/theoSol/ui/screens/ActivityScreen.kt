package aeeL.theoSol.ui.screens

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import aeeL.theoSol.R
import aeeL.theoSol.data.log.LogEntry
import aeeL.theoSol.data.log.LogLevel
import aeeL.theoSol.ui.components.AppCard
import aeeL.theoSol.ui.components.ConfirmDialog
import aeeL.theoSol.ui.components.EmptyPanel
import aeeL.theoSol.ui.components.PrimaryButton
import aeeL.theoSol.ui.components.SecondaryButton
import aeeL.theoSol.ui.components.StatusChip
import aeeL.theoSol.ui.components.StatusTone
import aeeL.theoSol.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun ActivityScreen(
    entries: List<LogEntry>,
    onLogJson: () -> String,
    onClear: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val copiedMessage = stringResource(R.string.activity_copied)
    val clipLabel = stringResource(R.string.app_name)
    val shareTitle = stringResource(R.string.activity_share_title)

    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) {
            listState.scrollToItem(entries.lastIndex + 1)
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            title = stringResource(R.string.activity_clear_confirm_title),
            body = stringResource(R.string.activity_clear_confirm_body),
            confirmLabel = stringResource(R.string.action_clear),
            onConfirm = {
                confirmClear = false
                onClear()
            },
            onDismiss = { confirmClear = false },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item(key = "header") {
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.padding(Spacing.md),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.activity_size, entries.size),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.activity_empty_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.action_copy_json),
                        onClick = {
                            val json = onLogJson()
                            scope.launch {
                                clipboard.setClipEntry(
                                    ClipEntry(ClipData.newPlainText(clipLabel, json)),
                                )
                                Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = entries.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                    )
                    SecondaryButton(
                        text = stringResource(R.string.action_share),
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_TEXT, onLogJson())
                            }
                            context.startActivity(
                                Intent.createChooser(intent, shareTitle),
                            )
                        },
                        enabled = entries.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                    )
                }
                SecondaryButton(
                    text = stringResource(R.string.action_clear),
                    onClick = { confirmClear = true },
                    enabled = entries.isNotEmpty(),
                )
            }
        }
        if (entries.isEmpty()) {
            item(key = "empty") {
                EmptyPanel(
                    title = stringResource(R.string.activity_empty),
                    body = stringResource(R.string.activity_empty_body),
                )
            }
        } else {
            items(items = entries, key = { it.id }) { entry ->
                LogRow(entry = entry)
            }
        }
    }
}

@Composable
private fun LogRow(entry: LogEntry) {
    val tone = when (entry.level) {
        LogLevel.SUCCESS -> StatusTone.POSITIVE
        LogLevel.FAILURE -> StatusTone.NEGATIVE
        LogLevel.ACTION, LogLevel.RESPONSE, LogLevel.INFO -> StatusTone.NEUTRAL
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Text(
                    text = entry.time,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatusChip(text = entry.level.name, tone = tone)
            }
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = entry.message,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    entry.detail?.let { detail ->
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

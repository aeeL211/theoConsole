package com.aeeL.theokntl.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.aeeL.theokntl.R
import com.aeeL.theokntl.data.log.LogEntry
import com.aeeL.theokntl.data.log.LogLevel
import com.aeeL.theokntl.ui.components.ConfirmDialog
import com.aeeL.theokntl.ui.components.EmptyPanel
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.activity_size, entries.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            val json = onLogJson()
                            scope.launch {
                                clipboard.setClipEntry(
                                    ClipEntry(ClipData.newPlainText(clipLabel, json)),
                                )
                                Toast.makeText(
                                    context,
                                    copiedMessage,
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                        enabled = entries.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.action_copy_json))
                    }
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_TEXT, onLogJson())
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    shareTitle,
                                ),
                            )
                        },
                        enabled = entries.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.action_share))
                    }
                    TextButton(
                        onClick = { confirmClear = true },
                        enabled = entries.isNotEmpty(),
                    ) {
                        Text(stringResource(R.string.action_clear))
                    }
                }
                HorizontalDivider()
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
    val levelColor = when (entry.level) {
        LogLevel.SUCCESS -> MaterialTheme.colorScheme.primary
        LogLevel.FAILURE -> MaterialTheme.colorScheme.error
        LogLevel.ACTION -> MaterialTheme.colorScheme.tertiary
        LogLevel.RESPONSE -> MaterialTheme.colorScheme.secondary
        LogLevel.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = entry.time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = entry.level.name,
                style = MaterialTheme.typography.labelSmall,
                color = levelColor,
            )
        }
        SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = entry.message,
                    style = MaterialTheme.typography.bodySmall,
                )
                entry.detail?.let { detail ->
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

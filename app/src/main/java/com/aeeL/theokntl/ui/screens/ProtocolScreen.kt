package com.aeeL.theokntl.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.aeeL.theokntl.R
import com.aeeL.theokntl.domain.model.protocolInfo
import com.aeeL.theokntl.ui.components.InlineRow
import com.aeeL.theokntl.ui.components.ScreenColumn
import com.aeeL.theokntl.ui.components.SectionCard
import com.aeeL.theokntl.ui.components.protocolLabel

@Composable
fun ProtocolScreen() {
    val info = remember { protocolInfo() }
    ScreenColumn {
        Text(
            text = stringResource(R.string.protocol_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionCard(title = stringResource(R.string.protocol_values)) {
            info.rows.forEach { row ->
                InlineRow(
                    label = stringResource(protocolLabel(row.labelKey)),
                    value = row.value,
                )
            }
            InlineRow(
                label = stringResource(R.string.protocol_endpoint),
                value = info.endpointHost,
            )
        }
        SectionCard(title = stringResource(R.string.protocol_raw)) {
            info.raw.forEach { row ->
                InlineRow(
                    label = stringResource(protocolLabel(row.labelKey)),
                    value = row.value,
                )
            }
        }
        Text(
            text = stringResource(R.string.protocol_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

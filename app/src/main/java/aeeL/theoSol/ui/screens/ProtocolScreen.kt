package aeeL.theoSol.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import aeeL.theoSol.R
import aeeL.theoSol.domain.model.protocolInfo
import aeeL.theoSol.ui.components.AppCard
import aeeL.theoSol.ui.components.InlineRow
import aeeL.theoSol.ui.components.ScreenColumn
import aeeL.theoSol.ui.components.SectionHeader
import aeeL.theoSol.ui.components.protocolLabel

@Composable
fun ProtocolScreen() {
    val info = remember { protocolInfo() }
    ScreenColumn {
        Text(
            text = stringResource(R.string.protocol_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionHeader(title = stringResource(R.string.protocol_values))
        AppCard {
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
        SectionHeader(title = stringResource(R.string.protocol_raw))
        AppCard {
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

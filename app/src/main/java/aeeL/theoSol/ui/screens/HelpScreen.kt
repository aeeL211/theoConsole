package aeeL.theoSol.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import aeeL.theoSol.BuildConfig
import aeeL.theoSol.R
import aeeL.theoSol.ui.components.AppCard
import aeeL.theoSol.ui.components.ListDivider
import aeeL.theoSol.ui.components.ScreenColumn
import aeeL.theoSol.ui.components.SectionHeader
import aeeL.theoSol.ui.feedback.HapticEffect
import aeeL.theoSol.ui.feedback.rememberHaptics
import aeeL.theoSol.ui.theme.Spacing

@Composable
fun HelpScreen() {
    val sections = listOf(
        stringResource(R.string.help_section_start) to stringResource(R.string.help_start_body),
        stringResource(R.string.help_section_topup) to stringResource(R.string.help_topup_body),
        stringResource(R.string.help_section_features) to stringResource(R.string.help_features_body),
        stringResource(R.string.help_section_security) to stringResource(R.string.help_security_body),
        stringResource(R.string.help_section_build) to stringResource(R.string.help_build_body),
    )
    ScreenColumn {
        Text(
            text = stringResource(R.string.help_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppCard(
            contentPadding = PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            sections.forEachIndexed { index, (title, body) ->
                HelpItem(title = title, body = body)
                if (index != sections.lastIndex) {
                    ListDivider()
                }
            }
        }
        SectionHeader(title = stringResource(R.string.settings_version))
        AppCard {
            Text(
                text = stringResource(
                    R.string.version_line,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun HelpItem(
    title: String,
    body: String,
) {
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    val haptics = rememberHaptics()
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(180),
        label = "help arrow",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable {
                haptics.perform(HapticEffect.TAP)
                expanded = !expanded
            }
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.rotate(arrowRotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(150)) + expandVertically(tween(200)),
            exit = fadeOut(tween(120)) + shrinkVertically(tween(160)),
        ) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
    }
}

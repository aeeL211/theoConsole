package aeeL.theoSol.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import aeeL.theoSol.R
import aeeL.theoSol.domain.model.BusyOperation
import aeeL.theoSol.domain.model.ConsoleState
import aeeL.theoSol.ui.components.AppCard
import aeeL.theoSol.ui.components.BusyPanel
import aeeL.theoSol.ui.components.EmptyPanel
import aeeL.theoSol.ui.components.HeroCard
import aeeL.theoSol.ui.components.PrimaryButton
import aeeL.theoSol.ui.components.ScreenColumn
import aeeL.theoSol.ui.feedback.HapticEffect
import aeeL.theoSol.ui.feedback.rememberHaptics
import aeeL.theoSol.ui.icons.DiamondFilled
import aeeL.theoSol.ui.theme.Spacing

@Composable
fun LoginScreen(
    initialUsername: String,
    console: ConsoleState,
    onLogin: (String, String, Boolean) -> Unit,
    onBackHome: () -> Unit,
    onStop: () -> Unit,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberUsername by rememberSaveable { mutableStateOf(true) }
    var usernameMissing by rememberSaveable { mutableStateOf(false) }
    var passwordMissing by rememberSaveable { mutableStateOf(false) }
    val haptics = rememberHaptics()

    LaunchedEffect(initialUsername) {
        if (username.isBlank()) username = initialUsername
    }

    val busy = console.busyOperation == BusyOperation.LOGIN
    val submit = {
        usernameMissing = username.isBlank()
        passwordMissing = password.isEmpty()
        if (!usernameMissing && !passwordMissing) {
            onLogin(username.trim(), password, rememberUsername)
        }
    }

    if (console.signedIn) {
        ScreenColumn {
            EmptyPanel(
                title = stringResource(R.string.login_signed_in_title),
                body = stringResource(R.string.login_signed_in_body),
                actionLabel = stringResource(R.string.login_back_home),
                onAction = onBackHome,
            )
        }
        return
    }

    ScreenColumn {
        BusyPanel(
            operation = console.busyOperation.takeIf { busy },
            onStop = onStop,
        )
        HeroCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Icon(
                    imageVector = DiamondFilled,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.login_title),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = stringResource(R.string.login_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        AppCard {
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameMissing = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.login_username)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                enabled = !busy,
                isError = usernameMissing,
                supportingText = if (usernameMissing) {
                    { Text(stringResource(R.string.login_validation_username)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordMissing = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.login_password)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                singleLine = true,
                enabled = !busy,
                isError = passwordMissing,
                visualTransformation = PasswordVisualTransformation(),
                supportingText = if (passwordMissing) {
                    { Text(stringResource(R.string.login_validation_password)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.login_remember),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(R.string.login_remember_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = rememberUsername,
                    onCheckedChange = {
                        haptics.perform(HapticEffect.SELECT)
                        rememberUsername = it
                    },
                    enabled = !busy,
                )
            }
            PrimaryButton(
                text = stringResource(R.string.action_login),
                onClick = submit,
                enabled = !busy,
                loading = busy,
            )
            Text(
                text = stringResource(R.string.login_security_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(
            onClick = onBackHome,
            modifier = Modifier.padding(start = Spacing.xs),
        ) {
            Text(stringResource(R.string.action_back_home))
        }
    }
}

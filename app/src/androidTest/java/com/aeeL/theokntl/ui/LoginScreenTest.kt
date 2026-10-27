package com.aeeL.theokntl.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aeeL.theokntl.domain.model.ConsoleState
import com.aeeL.theokntl.domain.model.ThemeMode
import com.aeeL.theokntl.ui.screens.LoginScreen
import com.aeeL.theokntl.ui.theme.TheoConsoleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loginFormShowsBothCredentialFields() {
        composeRule.setContent {
            TheoConsoleTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                LoginScreen(
                    initialUsername = "aeeL",
                    console = ConsoleState(),
                    onLogin = { _, _, _ -> },
                    onBackHome = {},
                    onStop = {},
                )
            }
        }

        composeRule.onNodeWithText("Forum username").assertIsDisplayed()
        composeRule.onNodeWithText("Forum password").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
    }
}

package aeeL.theoSol.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import aeeL.theoSol.R
import aeeL.theoSol.domain.model.ConsoleState
import aeeL.theoSol.domain.model.ThemeMode
import aeeL.theoSol.ui.screens.LoginScreen
import aeeL.theoSol.ui.theme.TheoConsoleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loginFormShowsBothCredentialFields() {
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        val usernameLabel = resources.getString(R.string.login_username)
        val passwordLabel = resources.getString(R.string.login_password)
        val submitLabel = resources.getString(R.string.action_login)

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

        composeRule.onNodeWithText(usernameLabel).assertIsDisplayed()
        composeRule.onNodeWithText(passwordLabel).assertIsDisplayed()
        composeRule.onNodeWithText(submitLabel).assertIsDisplayed()
    }
}

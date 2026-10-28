package aeeL.theoSol.ui

import android.content.res.Configuration

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import aeeL.theoSol.domain.model.AccountField
import aeeL.theoSol.domain.model.AccountRow
import aeeL.theoSol.domain.model.AccountView
import aeeL.theoSol.domain.model.AppLanguage
import aeeL.theoSol.domain.model.AppSettings
import aeeL.theoSol.domain.model.ConsoleState
import aeeL.theoSol.domain.model.FeatureReport
import aeeL.theoSol.domain.model.FeatureStatus
import aeeL.theoSol.domain.model.FeatureUnlocks
import aeeL.theoSol.domain.model.SessionStatus
import aeeL.theoSol.domain.model.SessionView
import aeeL.theoSol.domain.model.StatsView
import aeeL.theoSol.domain.model.ThemeMode
import aeeL.theoSol.domain.model.TopUpOutcome
import aeeL.theoSol.domain.model.TopUpReview
import aeeL.theoSol.ui.screens.AccountScreen
import aeeL.theoSol.ui.screens.ActivityScreen
import aeeL.theoSol.ui.screens.DeveloperScreen
import aeeL.theoSol.ui.screens.HelpScreen
import aeeL.theoSol.ui.screens.HomeScreen
import aeeL.theoSol.ui.screens.LoginScreen
import aeeL.theoSol.ui.screens.PurchasesScreen
import aeeL.theoSol.ui.screens.SessionScreen
import aeeL.theoSol.ui.screens.SettingsScreen
import aeeL.theoSol.ui.screens.TopUpScreen
import aeeL.theoSol.ui.screens.TopUpSuccessScreen
import aeeL.theoSol.ui.theme.TheoConsoleTheme

private val previewFeatures = FeatureReport(
    items = listOf(
        FeatureStatus(FeatureUnlocks.ALL[0].id, "Free fast speed", true),
        FeatureStatus(FeatureUnlocks.ALL[1].id, "Free tunnels", false),
    ),
)

private val previewState = ConsoleState(
    signedIn = true,
    busy = false,
    account = AccountView(
        label = "AeeL",
        login = "aeeL",
        userId = 12095,
        name = "AeeL",
        rows = listOf(
            AccountRow(AccountField.LOGIN, "aeeL"),
            AccountRow(AccountField.USER_ID, "12095"),
        ),
        since = "12:00:00",
    ),
    stats = StatsView(
        signedIn = true,
        balance = 12500,
        earned = 15000,
        spend = 2500,
        room = 87500,
        limit = 100000,
        progress = 0.125f,
    ),
    session = SessionView(
        signedIn = true,
        status = SessionStatus.READY,
        userId = 12095,
        sessionBound = true,
        keyFingerprint = "ab12cd34",
        endpoint = "api.theotown.com",
        lastAction = "12:34:56",
    ),
    features = previewFeatures,
    lastTopUp = TopUpOutcome(
        before = 10000,
        amount = 2500,
        after = 12500,
        target = 12500,
        capped = false,
        features = previewFeatures,
    ),
    loginTime = "12:00:00",
    lastAction = "12:34:56",
)

private val previewReview = TopUpReview(
    before = 12500,
    amount = 2500,
    earned = 15000,
    target = 15000,
    after = 15000,
    unlocks = FeatureUnlocks.ALL.map { it.label },
    accountLabel = "AeeL",
    accountUserId = 12095,
)

@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    TheoConsoleTheme(themeMode = ThemeMode.SYSTEM, dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

@Preview(name = "Home", widthDp = 420, heightDp = 900, showBackground = true)
@Preview(name = "Home dark", widthDp = 420, heightDp = 900, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomePreview() {
    PreviewSurface {
        HomeScreen(
            state = previewState,
            onLogin = {},
            onRefresh = {},
            onSync = {},
            onStop = {},
            onTopUp = {},
            onPurchases = {},
            onSession = {},
            onProtocol = {},
            onDeveloper = {},
            onHelp = {},
        )
    }
}

@Preview(name = "Login", widthDp = 420, heightDp = 900, showBackground = true)
@Preview(name = "Login dark", widthDp = 420, heightDp = 900, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginPreview() {
    PreviewSurface {
        LoginScreen(
            initialUsername = "aeeL",
            console = ConsoleState(),
            onLogin = { _, _, _ -> },
            onBackHome = {},
            onStop = {},
        )
    }
}

@Preview(name = "Top up", widthDp = 420, heightDp = 900, showBackground = true)
@Preview(name = "Top up dark", widthDp = 420, heightDp = 900, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopUpPreview() {
    PreviewSurface {
        TopUpScreen(
            state = previewState,
            review = previewReview,
            onLogin = {},
            onReview = {},
            onTopUp = { _, _ -> },
            onApply = {},
            onCancelReview = {},
            onStop = {},
        )
    }
}

@Preview(name = "Purchases", widthDp = 420, heightDp = 800, showBackground = true)
@Preview(name = "Purchases dark", widthDp = 420, heightDp = 800, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PurchasesPreview() {
    PreviewSurface {
        PurchasesScreen(
            state = previewState,
            onLogin = {},
            onRead = {},
            onSync = {},
            onStop = {},
        )
    }
}

@Preview(name = "Account", widthDp = 420, heightDp = 800, showBackground = true)
@Preview(name = "Account dark", widthDp = 420, heightDp = 800, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AccountPreview() {
    PreviewSurface {
        AccountScreen(
            state = previewState,
            onLogin = {},
            onOpenSession = {},
            onSignOut = {},
            onStop = {},
        )
    }
}

@Preview(name = "Settings", widthDp = 420, heightDp = 800, showBackground = true)
@Preview(name = "Settings dark", widthDp = 420, heightDp = 800, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsPreview() {
    PreviewSurface {
        SettingsScreen(
            settings = AppSettings(username = "aeeL", themeMode = ThemeMode.SYSTEM),
            language = AppLanguage.SYSTEM,
            onLanguage = {},
            onThemeMode = {},
            onPalette = {},
            onDynamicColor = {},
            onDebugDetails = {},
            onHelp = {},
            onProtocol = {},
            onDeveloper = {},
        )
    }
}

@Preview(name = "Top up success", widthDp = 420, heightDp = 900, showBackground = true)
@Preview(name = "Top up success dark", widthDp = 420, heightDp = 900, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopUpSuccessPreview() {
    PreviewSurface {
        TopUpSuccessScreen(
            outcome = previewState.lastTopUp,
            onDone = {},
        )
    }
}

@Preview(name = "Session", widthDp = 420, heightDp = 800, showBackground = true)
@Preview(name = "Session dark", widthDp = 420, heightDp = 800, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SessionPreview() {
    PreviewSurface {
        SessionScreen(
            state = previewState,
            onLogin = {},
            onStop = {},
        )
    }
}

@Preview(name = "Developer", widthDp = 420, heightDp = 800, showBackground = true)
@Preview(name = "Developer dark", widthDp = 420, heightDp = 800, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeveloperPreview() {
    PreviewSurface {
        DeveloperScreen()
    }
}

@Preview(name = "Help", widthDp = 420, heightDp = 900, showBackground = true)
@Preview(name = "Help dark", widthDp = 420, heightDp = 900, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HelpPreview() {
    PreviewSurface {
        HelpScreen()
    }
}

@Preview(name = "Activity", widthDp = 420, heightDp = 800, showBackground = true)
@Preview(name = "Activity dark", widthDp = 420, heightDp = 800, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ActivityPreview() {
    PreviewSurface {
        ActivityScreen(
            entries = emptyList(),
            onLogJson = { "" },
            onClear = {},
        )
    }
}

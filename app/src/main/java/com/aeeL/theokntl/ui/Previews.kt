package com.aeeL.theokntl.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.aeeL.theokntl.domain.model.AccountField
import com.aeeL.theokntl.domain.model.AccountRow
import com.aeeL.theokntl.domain.model.AccountView
import com.aeeL.theokntl.domain.model.AppSettings
import com.aeeL.theokntl.domain.model.ConsoleState
import com.aeeL.theokntl.domain.model.FeatureReport
import com.aeeL.theokntl.domain.model.FeatureStatus
import com.aeeL.theokntl.domain.model.FeatureUnlocks
import com.aeeL.theokntl.domain.model.SessionStatus
import com.aeeL.theokntl.domain.model.SessionView
import com.aeeL.theokntl.domain.model.StatsView
import com.aeeL.theokntl.domain.model.ThemeMode
import com.aeeL.theokntl.domain.model.TopUpOutcome
import com.aeeL.theokntl.domain.model.TopUpReview
import com.aeeL.theokntl.ui.screens.AccountScreen
import com.aeeL.theokntl.ui.screens.HomeScreen
import com.aeeL.theokntl.ui.screens.LoginScreen
import com.aeeL.theokntl.ui.screens.PurchasesScreen
import com.aeeL.theokntl.ui.screens.SettingsScreen
import com.aeeL.theokntl.ui.screens.TopUpScreen
import com.aeeL.theokntl.ui.theme.TheoConsoleTheme

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
            AccountRow(AccountField.LOGIN, "aeeL", true),
            AccountRow(AccountField.USER_ID, "12095", true),
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
@Composable
private fun SettingsPreview() {
    PreviewSurface {
        SettingsScreen(
            settings = AppSettings(username = "aeeL", themeMode = ThemeMode.SYSTEM),
            onThemeMode = {},
            onDynamicColor = {},
            onDebugDetails = {},
            onHelp = {},
            onProtocol = {},
            onDeveloper = {},
        )
    }
}

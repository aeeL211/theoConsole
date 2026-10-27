package com.aeeL.theokntl.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aeeL.theokntl.R
import com.aeeL.theokntl.domain.model.AppSettings
import com.aeeL.theokntl.ui.screens.AccountScreen
import com.aeeL.theokntl.ui.screens.ActivityScreen
import com.aeeL.theokntl.ui.screens.DeveloperScreen
import com.aeeL.theokntl.ui.screens.HelpScreen
import com.aeeL.theokntl.ui.screens.HomeScreen
import com.aeeL.theokntl.ui.screens.LoginScreen
import com.aeeL.theokntl.ui.screens.ProtocolScreen
import com.aeeL.theokntl.ui.screens.PurchasesScreen
import com.aeeL.theokntl.ui.screens.SessionScreen
import com.aeeL.theokntl.ui.screens.SettingsScreen
import com.aeeL.theokntl.ui.screens.TopUpScreen
import com.aeeL.theokntl.ui.theme.TheoConsoleTheme

object Routes {
    const val HOME = "home"
    const val LOGIN = "login"
    const val TOP_UP = "top_up"
    const val PURCHASES = "purchases"
    const val ACCOUNT = "account"
    const val SESSION = "session"
    const val PROTOCOL = "protocol"
    const val DEVELOPER = "developer"
    const val HELP = "help"
    const val SETTINGS = "settings"
    const val ACTIVITY = "activity"
}

private data class BottomDestination(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.HOME, R.string.title_home, Icons.Default.Home),
    BottomDestination(Routes.TOP_UP, R.string.title_topup, Icons.Default.AddCircle),
    BottomDestination(Routes.PURCHASES, R.string.title_purchases, Icons.Default.ShoppingCart),
    BottomDestination(Routes.ACCOUNT, R.string.title_account, Icons.Default.AccountCircle),
)

private val detailRoutes = setOf(
    Routes.LOGIN,
    Routes.SESSION,
    Routes.PROTOCOL,
    Routes.DEVELOPER,
    Routes.HELP,
    Routes.SETTINGS,
    Routes.ACTIVITY,
)

@Composable
fun TheoConsoleRoot(
    appViewModel: AppViewModel = hiltViewModel(),
    consoleViewModel: ConsoleViewModel = hiltViewModel(),
) {
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    TheoConsoleTheme(
        themeMode = settings.themeMode,
        dynamicColor = settings.dynamicColor,
    ) {
        CompositionLocalProvider(LocalDebugDetails provides settings.debugDetails) {
            TheoConsoleApp(
                settings = settings,
                appViewModel = appViewModel,
                consoleViewModel = consoleViewModel,
            )
        }
    }
}

@Composable
private fun TheoConsoleApp(
    settings: AppSettings,
    appViewModel: AppViewModel,
    consoleViewModel: ConsoleViewModel,
) {
    val console by consoleViewModel.state.collectAsStateWithLifecycle()
    val review by consoleViewModel.review.collectAsStateWithLifecycle()
    val logEntries by consoleViewModel.logEntries.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: Routes.HOME
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val debugDetails = settings.debugDetails

    LaunchedEffect(debugDetails) {
        consoleViewModel.failures.collect { failure ->
            snackbarHostState.showSnackbar(
                message = failureText(context, failure, debugDetails),
                duration = SnackbarDuration.Long,
            )
        }
    }

    Scaffold(
        topBar = {
            ConsoleTopBar(
                route = route,
                canGoBack = route in detailRoutes,
                onBack = { navController.popBackStack() },
                onActivity = {
                    if (route != Routes.ACTIVITY) {
                        navController.navigate(Routes.ACTIVITY) { launchSingleTop = true }
                    }
                },
                onSettings = {
                    if (route != Routes.SETTINGS) {
                        navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                    }
                },
            )
        },
        bottomBar = {
            if (route != Routes.LOGIN) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = route == destination.route,
                            onClick = { navController.navigateTopLevel(destination.route) },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = stringResource(destination.label),
                                )
                            },
                            label = { Text(stringResource(destination.label)) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    state = console,
                    onLogin = { navController.navigate(Routes.LOGIN) { launchSingleTop = true } },
                    onRefresh = consoleViewModel::refreshBalance,
                    onSync = consoleViewModel::syncFeatures,
                    onStop = consoleViewModel::stopOperation,
                    onTopUp = { navController.navigateTopLevel(Routes.TOP_UP) },
                    onPurchases = { navController.navigateTopLevel(Routes.PURCHASES) },
                    onSession = { navController.navigate(Routes.SESSION) { launchSingleTop = true } },
                    onProtocol = { navController.navigate(Routes.PROTOCOL) { launchSingleTop = true } },
                    onDeveloper = { navController.navigate(Routes.DEVELOPER) { launchSingleTop = true } },
                    onHelp = { navController.navigate(Routes.HELP) { launchSingleTop = true } },
                )
            }
            composable(Routes.LOGIN) {
                LaunchedEffect(console.signedIn) {
                    if (console.signedIn) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                }
                LoginScreen(
                    initialUsername = settings.username,
                    console = console,
                    onLogin = consoleViewModel::login,
                    onBackHome = {
                        navController.navigate(Routes.HOME) { launchSingleTop = true }
                    },
                    onStop = consoleViewModel::stopOperation,
                )
            }
            composable(Routes.TOP_UP) {
                TopUpScreen(
                    state = console,
                    review = review,
                    onLogin = { navController.navigate(Routes.LOGIN) { launchSingleTop = true } },
                    onReview = consoleViewModel::reviewTopUp,
                    onTopUp = consoleViewModel::topUp,
                    onApply = consoleViewModel::applyTopUp,
                    onCancelReview = consoleViewModel::cancelTopUp,
                    onStop = consoleViewModel::stopOperation,
                )
            }
            composable(Routes.PURCHASES) {
                PurchasesScreen(
                    state = console,
                    onLogin = { navController.navigate(Routes.LOGIN) { launchSingleTop = true } },
                    onRead = consoleViewModel::readPurchases,
                    onSync = consoleViewModel::syncFeatures,
                    onStop = consoleViewModel::stopOperation,
                )
            }
            composable(Routes.ACCOUNT) {
                AccountScreen(
                    state = console,
                    onLogin = { navController.navigate(Routes.LOGIN) { launchSingleTop = true } },
                    onOpenSession = { navController.navigate(Routes.SESSION) { launchSingleTop = true } },
                    onSignOut = {
                        consoleViewModel.signOut()
                        navController.navigateTopLevel(Routes.HOME)
                    },
                    onStop = consoleViewModel::stopOperation,
                )
            }
            composable(Routes.SESSION) {
                SessionScreen(
                    state = console,
                    onLogin = { navController.navigate(Routes.LOGIN) { launchSingleTop = true } },
                    onStop = consoleViewModel::stopOperation,
                )
            }
            composable(Routes.PROTOCOL) {
                ProtocolScreen()
            }
            composable(Routes.DEVELOPER) {
                DeveloperScreen()
            }
            composable(Routes.HELP) {
                HelpScreen()
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    settings = settings,
                    onThemeMode = appViewModel::setThemeMode,
                    onDynamicColor = appViewModel::setDynamicColor,
                    onDebugDetails = appViewModel::setDebugDetails,
                    onHelp = { navController.navigate(Routes.HELP) { launchSingleTop = true } },
                    onProtocol = { navController.navigate(Routes.PROTOCOL) { launchSingleTop = true } },
                    onDeveloper = { navController.navigate(Routes.DEVELOPER) { launchSingleTop = true } },
                )
            }
            composable(Routes.ACTIVITY) {
                ActivityScreen(
                    entries = logEntries,
                    onLogJson = consoleViewModel::logJson,
                    onClear = consoleViewModel::clearLog,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConsoleTopBar(
    route: String,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onActivity: () -> Unit,
    onSettings: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(routeTitle(route))) },
        navigationIcon = {
            if (canGoBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onActivity) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = stringResource(R.string.action_activity),
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.action_settings),
                )
            }
        },
    )
}

@StringRes
private fun routeTitle(route: String): Int = when (route) {
    Routes.HOME -> R.string.title_home
    Routes.LOGIN -> R.string.title_login
    Routes.TOP_UP -> R.string.title_topup
    Routes.PURCHASES -> R.string.title_purchases
    Routes.ACCOUNT -> R.string.title_account
    Routes.SESSION -> R.string.title_session
    Routes.PROTOCOL -> R.string.title_protocol
    Routes.DEVELOPER -> R.string.title_developer
    Routes.HELP -> R.string.title_help
    Routes.SETTINGS -> R.string.title_settings
    Routes.ACTIVITY -> R.string.title_activity
    else -> R.string.app_name
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

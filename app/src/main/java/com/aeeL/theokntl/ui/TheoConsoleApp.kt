package com.aeeL.theokntl.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aeeL.theokntl.R
import com.aeeL.theokntl.domain.model.AppLanguage
import com.aeeL.theokntl.domain.model.AppSettings
import com.aeeL.theokntl.domain.model.TopUpOutcome
import com.aeeL.theokntl.ui.components.swipeToNavigate
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
import com.aeeL.theokntl.ui.screens.TopUpSuccessScreen
import com.aeeL.theokntl.ui.theme.TheoConsoleTheme

object Routes {
    const val HOME = "home"
    const val LOGIN = "login"
    const val TOP_UP = "top_up"
    const val TOP_UP_SUCCESS = "top_up_success"
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
    val language = currentAppLanguage()
    TheoConsoleTheme(
        themeMode = settings.themeMode,
        dynamicColor = settings.dynamicColor,
        palette = settings.palette,
    ) {
        CompositionLocalProvider(LocalDebugDetails provides settings.debugDetails) {
            TheoConsoleApp(
                settings = settings,
                language = language,
                onLanguage = ::setAppLanguage,
                appViewModel = appViewModel,
                consoleViewModel = consoleViewModel,
            )
        }
    }
}

@Composable
private fun TheoConsoleApp(
    settings: AppSettings,
    language: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
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
    val immersive = route == Routes.TOP_UP_SUCCESS
    var successOutcome by remember { mutableStateOf<TopUpOutcome?>(null) }

    LaunchedEffect(debugDetails) {
        consoleViewModel.failures.collect { failure ->
            snackbarHostState.showSnackbar(
                message = failureText(context, failure, debugDetails),
                duration = SnackbarDuration.Long,
            )
        }
    }

    LaunchedEffect(navController) {
        consoleViewModel.topUpSuccess.collect { outcome ->
            successOutcome = outcome
            if (navController.currentDestination?.route != Routes.TOP_UP_SUCCESS) {
                navController.navigate(Routes.TOP_UP_SUCCESS) {
                    popUpTo(Routes.TOP_UP) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!immersive) {
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
            }
        },
        bottomBar = {
            if (!immersive && route != Routes.LOGIN) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp,
                ) {
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
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(360, easing = FastOutSlowInEasing),
                    initialOffsetX = { it },
                ) + fadeIn(animationSpec = tween(220))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(360, easing = FastOutSlowInEasing),
                    targetOffsetX = { -it / 4 },
                ) + fadeOut(animationSpec = tween(180))
            },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = tween(360, easing = FastOutSlowInEasing),
                    initialOffsetX = { -it / 4 },
                ) + fadeIn(animationSpec = tween(220))
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(360, easing = FastOutSlowInEasing),
                    targetOffsetX = { it },
                ) + fadeOut(animationSpec = tween(180))
            },
        ) {
            composable(Routes.HOME) {
                SwipePage(route = Routes.HOME, navController = navController) {
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
                SwipePage(route = Routes.TOP_UP, navController = navController) {
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
            }
            composable(Routes.TOP_UP_SUCCESS) {
                val done = {
                    successOutcome = null
                    navController.navigateTopLevel(Routes.HOME)
                }
                BackHandler(onBack = done)
                TopUpSuccessScreen(
                    outcome = successOutcome ?: console.lastTopUp,
                    onDone = done,
                )
            }
            composable(Routes.PURCHASES) {
                SwipePage(route = Routes.PURCHASES, navController = navController) {
                    PurchasesScreen(
                        state = console,
                        onLogin = { navController.navigate(Routes.LOGIN) { launchSingleTop = true } },
                        onRead = consoleViewModel::readPurchases,
                        onSync = consoleViewModel::syncFeatures,
                        onStop = consoleViewModel::stopOperation,
                    )
                }
            }
            composable(Routes.ACCOUNT) {
                SwipePage(route = Routes.ACCOUNT, navController = navController) {
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
                    language = language,
                    onLanguage = onLanguage,
                    onThemeMode = appViewModel::setThemeMode,
                    onPalette = appViewModel::setPalette,
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

@Composable
private fun SwipePage(
    route: String,
    navController: NavHostController,
    content: @Composable () -> Unit,
) {
    val index = bottomDestinations.indexOfFirst { it.route == route }
    val previous = bottomDestinations.getOrNull(index - 1)?.route
    val next = bottomDestinations.getOrNull(index + 1)?.route
    val enabled = index >= 0 && route != Routes.TOP_UP
    Box(
        modifier = Modifier
            .fillMaxSize()
            .swipeToNavigate(
                enabled = enabled,
                onSwipeLeft = {
                    if (next != null) navController.navigateTopLevel(next)
                },
                onSwipeRight = {
                    if (previous != null) navController.navigateTopLevel(previous)
                },
            ),
    ) {
        content()
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
        title = {
            Text(
                text = stringResource(routeTitle(route)),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
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
    Routes.HOME -> R.string.app_name
    Routes.LOGIN -> R.string.title_login
    Routes.TOP_UP -> R.string.title_topup
    Routes.TOP_UP_SUCCESS -> R.string.topup_success_title
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

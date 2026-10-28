package com.aeeL.theokntl.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aeeL.theokntl.R
import com.aeeL.theokntl.domain.model.AppLanguage
import com.aeeL.theokntl.domain.model.AppSettings
import com.aeeL.theokntl.domain.model.ConsoleState
import com.aeeL.theokntl.domain.model.TopUpOutcome
import com.aeeL.theokntl.domain.model.TopUpReview
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
    const val MAIN = "main"
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

private fun pageIndex(route: String): Int =
    bottomDestinations.indexOfFirst { it.route == route }.coerceAtLeast(0)

@Composable
fun TheoConsoleRoot(
    appViewModel: AppViewModel = hiltViewModel(),
    consoleViewModel: ConsoleViewModel = hiltViewModel(),
) {
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val language = remember(context) { currentAppLanguage(context) }
    TheoConsoleTheme(
        themeMode = settings.themeMode,
        dynamicColor = settings.dynamicColor,
        palette = settings.palette,
    ) {
        CompositionLocalProvider(LocalDebugDetails provides settings.debugDetails) {
            TheoConsoleApp(
                settings = settings,
                language = language,
                onLanguage = { selected -> setAppLanguage(context, selected) },
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
    val navRoute = backStackEntry?.destination?.route ?: Routes.MAIN
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val debugDetails = settings.debugDetails
    val immersive = navRoute == Routes.TOP_UP_SUCCESS
    var mainPage by rememberSaveable { mutableIntStateOf(pageIndex(Routes.HOME)) }
    var successOutcome by remember { mutableStateOf<TopUpOutcome?>(null) }
    val homePage = pageIndex(Routes.HOME)

    val pageRoute = bottomDestinations.getOrNull(mainPage)?.route ?: Routes.HOME
    val displayRoute = if (navRoute == Routes.MAIN) pageRoute else navRoute
    val goToMain: (Int) -> Unit = { page ->
        mainPage = page.coerceIn(0, bottomDestinations.lastIndex)
        if (!navController.popBackStack(Routes.MAIN, inclusive = false)) {
            navController.navigate(Routes.MAIN) { launchSingleTop = true }
        }
    }
    val openRoute: (String) -> Unit = { route ->
        if (navController.currentDestination?.route != route) {
            navController.navigate(route) { launchSingleTop = true }
        }
    }

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
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (!immersive) {
                ConsoleTopBar(
                    route = displayRoute,
                    canGoBack = navRoute in detailRoutes,
                    onBack = { navController.popBackStack() },
                    onActivity = { openRoute(Routes.ACTIVITY) },
                    onSettings = { openRoute(Routes.SETTINGS) },
                )
            }
        },
        bottomBar = {
            if (!immersive && navRoute != Routes.LOGIN) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp,
                ) {
                    bottomDestinations.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            selected = mainPage == index,
                            onClick = {
                                if (navRoute == Routes.MAIN) {
                                    mainPage = index
                                } else {
                                    goToMain(index)
                                }
                            },
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
        val insetSides = if (navRoute == Routes.LOGIN) {
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        } else {
            WindowInsetsSides.Horizontal
        }
        NavHost(
            navController = navController,
            startDestination = Routes.MAIN,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(insetSides))
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
            composable(Routes.MAIN) {
                MainTabs(
                    currentPage = mainPage,
                    onPageSelected = { mainPage = it },
                    console = console,
                    review = review,
                    consoleViewModel = consoleViewModel,
                    openRoute = openRoute,
                )
            }
            composable(Routes.LOGIN) {
                LaunchedEffect(console.signedIn) {
                    if (console.signedIn) {
                        goToMain(homePage)
                    }
                }
                LoginScreen(
                    initialUsername = settings.username,
                    console = console,
                    onLogin = consoleViewModel::login,
                    onBackHome = { goToMain(homePage) },
                    onStop = consoleViewModel::stopOperation,
                )
            }
            composable(Routes.TOP_UP_SUCCESS) {
                val finishSuccess = {
                    goToMain(homePage)
                    successOutcome = null
                }
                BackHandler(onBack = finishSuccess)
                TopUpSuccessScreen(
                    outcome = successOutcome ?: console.lastTopUp,
                    onDone = finishSuccess,
                )
            }
            composable(Routes.SESSION) {
                SessionScreen(
                    state = console,
                    onLogin = { openRoute(Routes.LOGIN) },
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
                    onHelp = { openRoute(Routes.HELP) },
                    onProtocol = { openRoute(Routes.PROTOCOL) },
                    onDeveloper = { openRoute(Routes.DEVELOPER) },
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
private fun MainTabs(
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    console: ConsoleState,
    review: TopUpReview?,
    consoleViewModel: ConsoleViewModel,
    openRoute: (String) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { bottomDestinations.size },
    )
    val topUpPage = pageIndex(Routes.TOP_UP)
    val purchasesPage = pageIndex(Routes.PURCHASES)
    val homePage = pageIndex(Routes.HOME)

    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage) {
            pagerState.animateScrollToPage(currentPage)
        }
    }
    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage != currentPage) {
            onPageSelected(pagerState.settledPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = pagerState.settledPage != topUpPage,
    ) { page ->
        when (page) {
            homePage -> HomeScreen(
                state = console,
                onLogin = { openRoute(Routes.LOGIN) },
                onRefresh = consoleViewModel::refreshBalance,
                onSync = consoleViewModel::syncFeatures,
                onStop = consoleViewModel::stopOperation,
                onTopUp = { onPageSelected(topUpPage) },
                onPurchases = { onPageSelected(purchasesPage) },
                onSession = { openRoute(Routes.SESSION) },
                onProtocol = { openRoute(Routes.PROTOCOL) },
                onDeveloper = { openRoute(Routes.DEVELOPER) },
                onHelp = { openRoute(Routes.HELP) },
            )
            topUpPage -> TopUpScreen(
                state = console,
                review = review,
                onLogin = { openRoute(Routes.LOGIN) },
                onReview = consoleViewModel::reviewTopUp,
                onTopUp = consoleViewModel::topUp,
                onApply = consoleViewModel::applyTopUp,
                onCancelReview = consoleViewModel::cancelTopUp,
                onStop = consoleViewModel::stopOperation,
            )
            purchasesPage -> PurchasesScreen(
                state = console,
                onLogin = { openRoute(Routes.LOGIN) },
                onRead = consoleViewModel::readPurchases,
                onSync = consoleViewModel::syncFeatures,
                onStop = consoleViewModel::stopOperation,
            )
            else -> AccountScreen(
                state = console,
                onLogin = { openRoute(Routes.LOGIN) },
                onOpenSession = { openRoute(Routes.SESSION) },
                onSignOut = {
                    consoleViewModel.signOut()
                    onPageSelected(homePage)
                },
                onStop = consoleViewModel::stopOperation,
            )
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
        windowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
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
    Routes.MAIN, Routes.HOME -> R.string.app_name
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

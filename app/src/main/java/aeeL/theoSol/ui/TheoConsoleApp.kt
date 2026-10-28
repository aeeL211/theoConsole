package aeeL.theoSol.ui

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle as AccountCircleOutlined
import androidx.compose.material.icons.outlined.Home as HomeOutlined
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import aeeL.theoSol.R
import aeeL.theoSol.domain.model.AppLanguage
import aeeL.theoSol.domain.model.AppSettings
import aeeL.theoSol.domain.model.ConsoleState
import aeeL.theoSol.domain.model.TopUpOutcome
import aeeL.theoSol.domain.model.TopUpReview
import aeeL.theoSol.ui.icons.DiamondFilled
import aeeL.theoSol.ui.icons.DiamondOutlined
import aeeL.theoSol.ui.screens.AccountScreen
import aeeL.theoSol.ui.screens.ActivityScreen
import aeeL.theoSol.ui.screens.DeveloperScreen
import aeeL.theoSol.ui.screens.HelpScreen
import aeeL.theoSol.ui.screens.HomeScreen
import aeeL.theoSol.ui.screens.LoginScreen
import aeeL.theoSol.ui.screens.ProtocolScreen
import aeeL.theoSol.ui.screens.SessionScreen
import aeeL.theoSol.ui.screens.SettingsScreen
import aeeL.theoSol.ui.screens.TopUpScreen
import aeeL.theoSol.ui.screens.TopUpSuccessScreen
import aeeL.theoSol.ui.theme.Dimens
import aeeL.theoSol.ui.theme.Spacing
import aeeL.theoSol.ui.theme.TheoConsoleTheme
import kotlinx.coroutines.flow.collectLatest

object Routes {
    const val MAIN = "main"
    const val HOME = "home"
    const val LOGIN = "login"
    const val TOP_UP = "top_up"
    const val TOP_UP_SUCCESS = "top_up_success"
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
    val selectedIcon: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination(
        route = Routes.HOME,
        label = R.string.title_home,
        icon = Icons.Outlined.HomeOutlined,
        selectedIcon = Icons.Filled.Home,
    ),
    BottomDestination(
        route = Routes.TOP_UP,
        label = R.string.title_topup,
        icon = DiamondOutlined,
        selectedIcon = DiamondFilled,
    ),
    BottomDestination(
        route = Routes.ACCOUNT,
        label = R.string.title_account,
        icon = Icons.Outlined.AccountCircleOutlined,
        selectedIcon = Icons.Filled.AccountCircle,
    ),
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
    val baseContext = LocalContext.current
    val configuration = LocalConfiguration.current
    var language by remember { mutableStateOf(currentAppLanguage(baseContext)) }
    val localized = remember(baseContext, language, configuration) {
        localizedContext(baseContext, language)
    }
    val layoutDirection = when (localized.resources.configuration.layoutDirection) {
        View.LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }
    CompositionLocalProvider(
        LocalConfiguration provides localized.resources.configuration,
        LocalResources provides localized.resources,
        LocalLayoutDirection provides layoutDirection,
    ) {
        TheoConsoleTheme(
            themeMode = settings.themeMode,
            dynamicColor = settings.dynamicColor,
            palette = settings.palette,
        ) {
            CompositionLocalProvider(LocalDebugDetails provides settings.debugDetails) {
                TheoConsoleApp(
                    settings = settings,
                    language = language,
                    onLanguage = { selected ->
                        language = selected
                        setAppLanguage(baseContext, selected)
                    },
                    appViewModel = appViewModel,
                    consoleViewModel = consoleViewModel,
                )
            }
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
    val failureResources = LocalResources.current
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

    BackHandler(enabled = navRoute == Routes.MAIN && mainPage != homePage) {
        mainPage = homePage
    }

    LaunchedEffect(debugDetails, failureResources) {
        consoleViewModel.failures.collect { failure ->
            snackbarHostState.showSnackbar(
                message = failureText(failureResources, failure, debugDetails),
                duration = SnackbarDuration.Long,
            )
        }
    }

    LaunchedEffect(navController) {
        consoleViewModel.topUpSuccess.collectLatest { outcome ->
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
            AnimatedVisibility(
                visible = !immersive,
                enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { -it },
                exit = fadeOut(tween(140)) + slideOutVertically(tween(180)) { -it },
            ) {
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
            AnimatedVisibility(
                visible = navRoute == Routes.MAIN,
                enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it },
                exit = fadeOut(tween(140)) + slideOutVertically(tween(180)) { it },
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                    ) {
                        bottomDestinations.forEachIndexed { index, destination ->
                            val selected = mainPage == index
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (navRoute == Routes.MAIN) {
                                        mainPage = index
                                    } else {
                                        goToMain(index)
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) {
                                            destination.selectedIcon
                                        } else {
                                            destination.icon
                                        },
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(stringResource(destination.label)) },
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val insetSides = when (navRoute) {
            Routes.MAIN, Routes.TOP_UP_SUCCESS -> WindowInsetsSides.Horizontal
            else -> WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
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
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                    initialOffsetX = { it / 8 },
                ) + fadeIn(animationSpec = tween(220))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                    targetOffsetX = { -it / 8 },
                ) + fadeOut(animationSpec = tween(160))
            },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                    initialOffsetX = { -it / 8 },
                ) + fadeIn(animationSpec = tween(220))
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                    targetOffsetX = { it / 8 },
                ) + fadeOut(animationSpec = tween(160))
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
            composable(
                route = Routes.TOP_UP_SUCCESS,
                enterTransition = {
                    fadeIn(tween(280, easing = FastOutSlowInEasing)) +
                        scaleIn(
                            animationSpec = tween(280, easing = FastOutSlowInEasing),
                            initialScale = 0.96f,
                        )
                },
                exitTransition = { fadeOut(tween(160)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = {
                    fadeOut(tween(220)) +
                        scaleOut(
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                            targetScale = 0.98f,
                        )
                },
            ) {
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
    val homePage = pageIndex(Routes.HOME)
    val latestPage by rememberUpdatedState(currentPage)
    val latestOnPageSelected by rememberUpdatedState(onPageSelected)
    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = tween(250, easing = FastOutSlowInEasing),
        snapPositionalThreshold = 0.28f,
    )

    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage) {
            pagerState.animateScrollToPage(
                page = currentPage,
                animationSpec = tween(250, easing = FastOutSlowInEasing),
            )
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settled ->
            if (settled != latestPage) {
                latestOnPageSelected(settled)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        // The swipe-to-confirm control consumes its own horizontal drag. Every
        // other part of Top up leaves the gesture to the pager.
        userScrollEnabled = true,
        beyondViewportPageCount = 1,
        flingBehavior = flingBehavior,
    ) { page ->
        when (page) {
            homePage -> HomeScreen(
                state = console,
                onLogin = { openRoute(Routes.LOGIN) },
                onRefresh = consoleViewModel::refreshBalance,
                onSync = consoleViewModel::syncFeatures,
                onStop = consoleViewModel::stopOperation,
                onTopUp = { onPageSelected(topUpPage) },
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

@Composable
private fun ConsoleTopBar(
    route: String,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onActivity: () -> Unit,
    onSettings: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // The status bar is hidden, so only side cutouts need padding.
                // The top cutout inset here would leave a dead band above the bar.
                .windowInsetsPadding(
                    WindowInsets.displayCutout.only(
                        WindowInsetsSides.Horizontal,
                    ),
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Dimens.topBarHeight)
                    .padding(horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (canGoBack) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                }
                Text(
                    text = stringResource(routeTitle(route)),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (canGoBack) Spacing.xs else Spacing.lg),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
            }
        }
    }
}

@StringRes
private fun routeTitle(route: String): Int = when (route) {
    Routes.MAIN, Routes.HOME -> R.string.app_name
    Routes.LOGIN -> R.string.title_login
    Routes.TOP_UP -> R.string.title_topup
    Routes.TOP_UP_SUCCESS -> R.string.topup_success_title
    Routes.ACCOUNT -> R.string.title_account
    Routes.SESSION -> R.string.title_session
    Routes.PROTOCOL -> R.string.title_protocol
    Routes.DEVELOPER -> R.string.title_developer
    Routes.HELP -> R.string.title_help
    Routes.SETTINGS -> R.string.title_settings
    Routes.ACTIVITY -> R.string.title_activity
    else -> R.string.app_name
}

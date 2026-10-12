package com.subway.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.subway.app.data.auth.AuthManager
import com.subway.app.data.settings.ThemeMode
import com.subway.app.ui.navigation.Screen
import com.subway.app.ui.screens.*
import com.subway.app.viewmodel.MetroViewModel

enum class Tab(val route: String, val icon: ImageVector, val label: String) {
    HOME(Screen.Home.route, Icons.Filled.Home, "\u9996\u9875"),
    LINES(Screen.LineList.route, Icons.Filled.Train, "\u7EBF\u8DEF"),
    ROUTE(Screen.RoutePlan.route, Icons.Filled.Route, "\u6362\u4E58"),
    ME(Screen.Profile.route, Icons.Filled.Person, "\u6211\u7684"),
}

@Composable
fun AppNavigation(
    viewModel: MetroViewModel,
    themeMode: ThemeMode,
    fontScale: Float,
    onThemeModeChange: (ThemeMode) -> Unit,
    onFontScaleChange: (Float) -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    var loggedIn by remember { mutableStateOf(authManager.isLoggedIn()) }
    val navController = rememberNavController()
    LaunchedEffect(Unit) { viewModel.initialize(context) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val tabs = Tab.entries
    val isMainTab = loggedIn && currentRoute in tabs.map { it.route }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isMainTab) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    FloatingPillTabs(
                        tabs = tabs,
                        currentRoute = currentRoute ?: Screen.Home.route,
                        onTabClick = { tab -> navigateToTab(tab.route) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (loggedIn) Screen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    authManager = authManager,
                    onLoginSuccess = {
                        loggedIn = true
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSearch = { navigateToTab(Screen.StationSearch.route) },
                    onNavigateToLines = { navigateToTab(Screen.LineList.route) },
                    onNavigateToRoute = { navigateToTab(Screen.RoutePlan.route) },
                    onNearbyStationClick = { station ->
                        viewModel.setStartStation(station)
                        navigateToTab(Screen.RoutePlan.route)
                    },
                    viewModel = viewModel
                )
            }
            composable(Screen.LineList.route) {
                LineListScreen(
                    onLineClick = { lineId -> navController.navigate(Screen.LineDetail.createRoute(lineId)) },
                    viewModel = viewModel
                )
            }
            composable(route = Screen.LineDetail.route, arguments = listOf(navArgument("lineId") { type = NavType.IntType })) { entry ->
                LineDetailScreen(
                    lineId = entry.arguments?.getInt("lineId") ?: return@composable,
                    onStationClick = { stationId -> navController.navigate(Screen.StationDetail.createRoute(stationId)) },
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
            composable(Screen.StationSearch.route) {
                StationSearchScreen(
                    onStationClick = { stationId -> navController.navigate(Screen.StationDetail.createRoute(stationId)) },
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
            composable(route = Screen.StationDetail.route, arguments = listOf(navArgument("stationId") { type = NavType.IntType })) { entry ->
                StationDetailScreen(
                    stationId = entry.arguments?.getInt("stationId") ?: return@composable,
                    onNavigateToLine = { lineId -> navController.navigate(Screen.LineDetail.createRoute(lineId)) },
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
            composable(Screen.RoutePlan.route) {
                RoutePlanScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onHistoryClick = { start, end ->
                        navigateToTab(Screen.RoutePlan.route)
                        viewModel.useHistory(start, end)
                    },
                    onNearbyStationClick = { station ->
                        viewModel.setStartStation(station)
                        navigateToTab(Screen.RoutePlan.route)
                    },
                    viewModel = viewModel
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    authManager = authManager,
                    themeMode = themeMode,
                    fontScale = fontScale,
                    onThemeModeChange = onThemeModeChange,
                    onFontScaleChange = onFontScaleChange,
                    onBack = { navController.popBackStack() },
                    onChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                    onLogout = {
                        loggedIn = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.ChangePassword.route) {
                ChangePasswordScreen(
                    authManager = authManager,
                    onBack = { navController.popBackStack() },
                    onPasswordChanged = {
                        authManager.logout()
                        loggedIn = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FloatingPillTabs(tabs: List<Tab>, currentRoute: String, onTabClick: (Tab) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .then(if (selected) Modifier.background(MaterialTheme.colorScheme.primaryContainer) else Modifier)
                        .clickable { onTabClick(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(20.dp),
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

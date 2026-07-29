package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.viewmodel.CoverageViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Live : Screen("live", "Live Metrics", Icons.Default.CellTower)
    object Speed : Screen("speed", "Speed Test", Icons.Default.Speed)
    object Map : Screen("map", "Map Heatmap", Icons.Default.Map)
    object Charts : Screen("charts", "Charts", Icons.Default.BarChart)
    object History : Screen("history", "History & CSV", Icons.Default.History)
}

val navItems = listOf(
    Screen.Live,
    Screen.Speed,
    Screen.Map,
    Screen.Charts,
    Screen.History
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CoverageViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Live.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            Screen.Live.route -> "Live Metrics"
                            Screen.Speed.route -> "Speed Test"
                            Screen.Map.route -> "Coverage Heatmap Map"
                            Screen.Charts.route -> "Trend Charts"
                            Screen.History.route -> "History & Export"
                            else -> "Coverage Analyzer"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_navigation")
            ) {
                navItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Live.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Live.route) {
                LiveMetricsScreen(
                    viewModel = viewModel,
                    onNavigateToSpeedTest = {
                        navController.navigate(Screen.Speed.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Speed.route) {
                SpeedTestScreen(viewModel = viewModel)
            }
            composable(Screen.Map.route) {
                MapCoverageScreen(viewModel = viewModel)
            }
            composable(Screen.Charts.route) {
                ChartsScreen(viewModel = viewModel)
            }
            composable(Screen.History.route) {
                HistoryScreen(viewModel = viewModel)
            }
        }
    }
}

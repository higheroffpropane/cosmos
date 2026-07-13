package com.dskochilov.cosmos.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dskochilov.cosmos.core.ui.LocalSharedTransitionScope
import com.dskochilov.cosmos.navigation.CosmosNavHost
import com.dskochilov.cosmos.navigation.TopLevelDestination

/** Width from which the bottom bar is replaced by a navigation rail. */
private val WIDE_LAYOUT_MIN_WIDTH = 600.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CosmosApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= WIDE_LAYOUT_MIN_WIDTH

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (!useNavigationRail) {
                    CosmosBottomBar(navController = navController)
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    CosmosNavigationRail(navController = navController)
                }
                SharedTransitionLayout(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        CosmosNavHost(
                            navController = navController,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CosmosBottomBar(navController: NavHostController, modifier: Modifier = Modifier) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar(modifier = modifier) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination.isSelected(destination),
                onClick = { navController.navigateToTopLevel(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null
                    )
                },
                label = { Text(text = stringResource(destination.labelRes)) }
            )
        }
    }
}

@Composable
private fun CosmosNavigationRail(navController: NavHostController, modifier: Modifier = Modifier) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationRail(modifier = modifier) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationRailItem(
                selected = currentDestination.isSelected(destination),
                onClick = { navController.navigateToTopLevel(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null
                    )
                },
                label = { Text(text = stringResource(destination.labelRes)) }
            )
        }
    }
}

private fun NavDestination?.isSelected(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

package com.dskochilov.cosmos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.dskochilov.cosmos.feature.apod.ApodRoute
import com.dskochilov.cosmos.feature.apod.apodScreen
import com.dskochilov.cosmos.feature.favorites.favoritesScreen
import com.dskochilov.cosmos.feature.marsrover.MarsPhotoDetailRoute
import com.dskochilov.cosmos.feature.marsrover.marsPhotoDetailScreen
import com.dskochilov.cosmos.feature.marsrover.marsRoverScreen
import com.dskochilov.cosmos.feature.search.searchScreen
import com.dskochilov.cosmos.feature.settings.settingsScreen

@Composable
fun CosmosNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = ApodRoute,
        modifier = modifier
    ) {
        apodScreen()
        marsRoverScreen(
            onPhotoClick = { photoId -> navController.navigate(MarsPhotoDetailRoute(photoId)) }
        )
        marsPhotoDetailScreen(onBack = navController::popBackStack)
        searchScreen()
        favoritesScreen()
        settingsScreen()
    }
}

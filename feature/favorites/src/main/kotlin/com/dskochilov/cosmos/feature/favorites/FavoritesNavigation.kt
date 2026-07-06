package com.dskochilov.cosmos.feature.favorites

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object FavoritesRoute

fun NavGraphBuilder.favoritesScreen() {
    composable<FavoritesRoute> {
        FavoritesScreen()
    }
}

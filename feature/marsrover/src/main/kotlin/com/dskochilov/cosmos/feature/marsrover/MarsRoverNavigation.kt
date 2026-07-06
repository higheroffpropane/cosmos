package com.dskochilov.cosmos.feature.marsrover

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object MarsRoverRoute

fun NavGraphBuilder.marsRoverScreen() {
    composable<MarsRoverRoute> {
        MarsRoverScreen()
    }
}

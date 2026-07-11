package com.dskochilov.cosmos.feature.marsrover

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dskochilov.cosmos.feature.marsrover.detail.MarsPhotoDetailScreen
import kotlinx.serialization.Serializable

@Serializable
data object MarsRoverRoute

@Serializable
data class MarsPhotoDetailRoute(val photoId: String)

fun NavGraphBuilder.marsRoverScreen(onPhotoClick: (String) -> Unit) {
    composable<MarsRoverRoute> {
        MarsRoverScreen(onPhotoClick = onPhotoClick)
    }
}

fun NavGraphBuilder.marsPhotoDetailScreen(onBack: () -> Unit) {
    composable<MarsPhotoDetailRoute> {
        MarsPhotoDetailScreen(onBack = onBack)
    }
}

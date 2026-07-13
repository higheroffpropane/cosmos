package com.dskochilov.cosmos.feature.marsrover

import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dskochilov.cosmos.core.ui.LocalNavAnimatedVisibilityScope
import com.dskochilov.cosmos.feature.marsrover.detail.MarsPhotoDetailScreen
import kotlinx.serialization.Serializable

@Serializable
data object MarsRoverRoute

@Serializable
data class MarsPhotoDetailRoute(val photoId: String)

fun NavGraphBuilder.marsRoverScreen(onPhotoClick: (String) -> Unit) {
    composable<MarsRoverRoute> {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            MarsRoverScreen(onPhotoClick = onPhotoClick)
        }
    }
}

fun NavGraphBuilder.marsPhotoDetailScreen(onBack: () -> Unit) {
    composable<MarsPhotoDetailRoute> {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            MarsPhotoDetailScreen(onBack = onBack)
        }
    }
}

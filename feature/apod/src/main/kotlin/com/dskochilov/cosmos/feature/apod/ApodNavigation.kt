package com.dskochilov.cosmos.feature.apod

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object ApodRoute

fun NavGraphBuilder.apodScreen() {
    composable<ApodRoute> {
        ApodScreen()
    }
}

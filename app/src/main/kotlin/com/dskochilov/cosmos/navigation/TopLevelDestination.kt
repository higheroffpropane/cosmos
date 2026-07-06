package com.dskochilov.cosmos.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.dskochilov.cosmos.R
import com.dskochilov.cosmos.feature.apod.ApodRoute
import com.dskochilov.cosmos.feature.favorites.FavoritesRoute
import com.dskochilov.cosmos.feature.marsrover.MarsRoverRoute
import com.dskochilov.cosmos.feature.search.SearchRoute
import com.dskochilov.cosmos.feature.settings.SettingsRoute

/**
 * Top-level destinations shown in the app's bottom navigation bar.
 */
enum class TopLevelDestination(val route: Any, val icon: ImageVector, @param:StringRes val labelRes: Int) {
    APOD(ApodRoute, Icons.Rounded.Star, R.string.destination_apod),
    MARS_ROVER(MarsRoverRoute, Icons.Rounded.Place, R.string.destination_mars_rover),
    SEARCH(SearchRoute, Icons.Rounded.Search, R.string.destination_search),
    FAVORITES(FavoritesRoute, Icons.Rounded.Favorite, R.string.destination_favorites),
    SETTINGS(SettingsRoute, Icons.Rounded.Settings, R.string.destination_settings)
}

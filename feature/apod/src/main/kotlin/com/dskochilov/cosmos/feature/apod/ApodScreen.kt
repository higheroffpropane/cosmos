package com.dskochilov.cosmos.feature.apod

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dskochilov.cosmos.core.designsystem.component.ScreenPlaceholder

@Composable
internal fun ApodScreen(modifier: Modifier = Modifier) {
    ScreenPlaceholder(
        title = "Astronomy Picture of the Day",
        modifier = modifier
    )
}

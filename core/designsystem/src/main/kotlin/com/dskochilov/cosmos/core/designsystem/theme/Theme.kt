package com.dskochilov.cosmos.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = NebulaVioletDim,
    onPrimary = StarWhite,
    secondary = CometTealDim,
    onSecondary = StarWhite,
    tertiary = SolarGold,
    error = MarsRed
)

private val DarkColorScheme = darkColorScheme(
    primary = NebulaViolet,
    onPrimary = DeepSpace,
    secondary = CometTeal,
    onSecondary = DeepSpace,
    tertiary = SolarGold,
    error = MarsRed,
    background = DeepSpace,
    onBackground = StarWhite,
    surface = SpaceSurface,
    onSurface = StarWhite
)

/**
 * Cosmos app theme: Material 3, dark theme aware, with optional dynamic color on Android 12+.
 */
@Composable
fun CosmosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme

        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CosmosTypography,
        content = content
    )
}

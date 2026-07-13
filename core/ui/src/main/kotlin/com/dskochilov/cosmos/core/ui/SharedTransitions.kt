package com.dskochilov.cosmos.core.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Bridges the app-level [SharedTransitionScope] and each destination's
 * [AnimatedVisibilityScope] to feature modules without direct coupling.
 * Both default to null so screens render unchanged in previews and UI tests.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Marks this element as a shared element across navigation destinations.
 * No-op when the transition scopes are not available (tests, previews).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.cosmosSharedBounds(key: String): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current

    return if (sharedTransitionScope == null || animatedVisibilityScope == null) {
        this
    } else {
        with(sharedTransitionScope) {
            sharedBounds(
                sharedContentState = rememberSharedContentState(key),
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    }
}

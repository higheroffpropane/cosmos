package com.dskochilov.cosmos.core.domain.favorites

/**
 * Warms the image disk cache so favorites stay viewable offline.
 * Implemented by the app layer on top of the image loader.
 */
fun interface ImagePrefetcher {
    fun prefetch(url: String)
}

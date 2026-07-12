package com.dskochilov.cosmos.core.domain.favorites

import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto

/**
 * A locally saved item, newest first on the favorites screen.
 */
sealed interface Favorite {
    val id: String
    val savedAt: Long

    data class ApodEntry(val apod: Apod, override val savedAt: Long) : Favorite {
        override val id: String get() = apod.favoriteId()
    }

    data class MarsPhotoEntry(val photo: MarsPhoto, override val savedAt: Long) : Favorite {
        override val id: String get() = photo.favoriteId()
    }
}

fun Apod.favoriteId(): String = "apod:$date"

fun MarsPhoto.favoriteId(): String = "mars:$id"

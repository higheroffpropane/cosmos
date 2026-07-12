package com.dskochilov.cosmos.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A locally saved favorite: either an APOD entry or a Mars photo, discriminated
 * by [type]. Per-type payload columns are nullable; the row is self-contained so
 * favorites remain fully viewable offline regardless of other caches.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val savedAt: Long,
    val apodDate: String? = null,
    val apodExplanation: String? = null,
    val apodCopyright: String? = null,
    val apodVideoUrl: String? = null,
    val marsRover: String? = null,
    val marsCameraName: String? = null,
    val marsCameraFullName: String? = null,
    val marsEarthDate: String? = null,
    val marsSol: Int? = null
) {
    companion object {
        const val TYPE_APOD = "APOD"
        const val TYPE_MARS_PHOTO = "MARS_PHOTO"
    }
}

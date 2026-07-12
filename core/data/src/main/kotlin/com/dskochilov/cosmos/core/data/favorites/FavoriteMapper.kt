package com.dskochilov.cosmos.core.data.favorites

import com.dskochilov.cosmos.core.database.entity.FavoriteEntity
import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.domain.favorites.Favorite
import com.dskochilov.cosmos.core.domain.favorites.favoriteId
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import java.time.LocalDate

private val FALLBACK_DATE: LocalDate = LocalDate.of(1970, 1, 1)

internal fun Apod.toFavoriteEntity(savedAt: Long): FavoriteEntity {
    val (imageUrl, videoUrl) = when (val media = media) {
        is ApodMedia.Image -> media.url to null
        is ApodMedia.Video -> media.thumbnailUrl.orEmpty() to media.videoUrl
        ApodMedia.Unsupported -> "" to null
    }
    return FavoriteEntity(
        id = favoriteId(),
        type = FavoriteEntity.TYPE_APOD,
        title = title,
        imageUrl = imageUrl,
        thumbnailUrl = imageUrl,
        savedAt = savedAt,
        apodDate = date.toString(),
        apodExplanation = explanation,
        apodCopyright = copyright,
        apodVideoUrl = videoUrl
    )
}

internal fun MarsPhoto.toFavoriteEntity(savedAt: Long): FavoriteEntity = FavoriteEntity(
    id = favoriteId(),
    type = FavoriteEntity.TYPE_MARS_PHOTO,
    title = cameraFullName,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    savedAt = savedAt,
    marsRover = rover.apiName,
    marsCameraName = cameraName,
    marsCameraFullName = cameraFullName,
    marsEarthDate = earthDate.toString(),
    marsSol = sol
)

/**
 * Rebuilds the domain favorite; returns null for rows of unknown type
 * (defensive: they could only appear after a bad migration).
 */
internal fun FavoriteEntity.toDomain(): Favorite? = when (type) {
    FavoriteEntity.TYPE_APOD -> Favorite.ApodEntry(apod = toApod(), savedAt = savedAt)
    FavoriteEntity.TYPE_MARS_PHOTO -> Favorite.MarsPhotoEntry(photo = toMarsPhoto(), savedAt = savedAt)
    else -> null
}

private fun FavoriteEntity.toApod(): Apod {
    val videoUrl = apodVideoUrl
    return Apod(
        date = parseDateOrFallback(apodDate),
        title = title,
        explanation = apodExplanation.orEmpty(),
        copyright = apodCopyright,
        media = when {
            videoUrl != null -> ApodMedia.Video(videoUrl = videoUrl, thumbnailUrl = imageUrl.ifEmpty { null })
            imageUrl.isNotEmpty() -> ApodMedia.Image(url = imageUrl, hdUrl = null)
            else -> ApodMedia.Unsupported
        }
    )
}

private fun FavoriteEntity.toMarsPhoto(): MarsPhoto = MarsPhoto(
    id = id.removePrefix("mars:"),
    rover = MarsRover.fromApiName(marsRover.orEmpty()) ?: MarsRover.CURIOSITY,
    cameraName = marsCameraName.orEmpty(),
    cameraFullName = marsCameraFullName ?: title,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    earthDate = parseDateOrFallback(marsEarthDate),
    sol = marsSol ?: 0
)

private fun parseDateOrFallback(raw: String?): LocalDate =
    runCatching { LocalDate.parse(raw) }.getOrDefault(FALLBACK_DATE)

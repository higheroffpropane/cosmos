package com.dskochilov.cosmos.core.data.apod

import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.network.apod.ApodDto
import java.time.LocalDate

internal fun ApodDto.toDomain(): Apod = Apod(
    date = LocalDate.parse(date),
    title = title,
    explanation = explanation,
    copyright = copyright?.trim(),
    media = toMedia()
)

private fun ApodDto.toMedia(): ApodMedia = when (mediaType) {
    "image" -> url?.let { ApodMedia.Image(url = it, hdUrl = hdUrl) } ?: ApodMedia.Unsupported
    "video" -> url?.let { ApodMedia.Video(videoUrl = it, thumbnailUrl = thumbnailUrl) } ?: ApodMedia.Unsupported
    else -> ApodMedia.Unsupported
}

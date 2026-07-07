package com.dskochilov.cosmos.core.domain.apod

import java.time.LocalDate

/**
 * A single Astronomy Picture of the Day entry.
 */
data class Apod(
    val date: LocalDate,
    val title: String,
    val explanation: String,
    val copyright: String?,
    val media: ApodMedia
)

/**
 * APOD entries can be images, videos (hosted externally, e.g. YouTube),
 * or occasionally something the API does not describe further.
 */
sealed interface ApodMedia {
    data class Image(val url: String, val hdUrl: String?) : ApodMedia

    data class Video(val videoUrl: String, val thumbnailUrl: String?) : ApodMedia

    data object Unsupported : ApodMedia
}

object ApodDates {
    /** The APOD archive starts on this date; earlier requests are invalid. */
    val FIRST_ENTRY: LocalDate = LocalDate.of(1995, 6, 16)
}

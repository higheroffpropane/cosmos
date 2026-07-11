package com.dskochilov.cosmos.core.data.search

import com.dskochilov.cosmos.core.domain.search.MediaItem
import com.dskochilov.cosmos.core.network.images.NasaImagesItemDto
import java.time.LocalDate

private const val ISO_DATE_LENGTH = 10

/**
 * Maps a search hit to the domain, or null when it cannot be rendered
 * (no metadata or no preview image).
 */
internal fun NasaImagesItemDto.toDomain(): MediaItem? {
    val data = data.firstOrNull()
    val thumbnail = links.firstOrNull { it.rel == "preview" && it.render == "image" }?.href
        ?: links.firstOrNull { it.render == "image" }?.href
    if (data == null || thumbnail == null) return null

    return MediaItem(
        nasaId = data.nasaId,
        title = data.title.ifBlank { data.nasaId },
        description = data.description?.trim()?.ifBlank { null },
        center = data.center,
        dateCreated = data.dateCreated
            ?.take(ISO_DATE_LENGTH)
            ?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() },
        // Some asset URLs contain literal spaces, which Coil's URL parsing rejects.
        thumbnailUrl = thumbnail.replace(" ", "%20")
    )
}

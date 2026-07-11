package com.dskochilov.cosmos.core.domain.search

import java.time.LocalDate

/**
 * One image from NASA's Image and Video Library.
 */
data class MediaItem(
    val nasaId: String,
    val title: String,
    val description: String?,
    val center: String?,
    val dateCreated: LocalDate?,
    val thumbnailUrl: String
)

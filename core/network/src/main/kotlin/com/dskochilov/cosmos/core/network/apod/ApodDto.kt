package com.dskochilov.cosmos.core.network.apod

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw response of `GET /planetary/apod` (with `thumbs=true`).
 */
@Serializable
data class ApodDto(
    @SerialName("date") val date: String,
    @SerialName("title") val title: String,
    @SerialName("explanation") val explanation: String,
    @SerialName("media_type") val mediaType: String,
    @SerialName("url") val url: String? = null,
    @SerialName("hdurl") val hdUrl: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("copyright") val copyright: String? = null
)

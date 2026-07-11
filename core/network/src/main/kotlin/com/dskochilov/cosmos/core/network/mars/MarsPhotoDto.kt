package com.dskochilov.cosmos.core.network.mars

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw responses of the mars.nasa.gov raw-image feeds (see ADR 0006).
 * Perseverance (mars2020) and Curiosity (MSL) use different endpoints and shapes.
 */
@Serializable
data class PerseverancePhotosResponseDto(
    @SerialName("images") val images: List<PerseveranceImageDto>,
    @SerialName("total_results") val totalResults: Int
)

@Serializable
data class PerseveranceImageDto(
    @SerialName("imageid") val imageId: String,
    @SerialName("sol") val sol: Int,
    @SerialName("camera") val camera: PerseveranceCameraDto,
    @SerialName("image_files") val imageFiles: PerseveranceImageFilesDto,
    @SerialName("date_taken_utc") val dateTakenUtc: String? = null
)

@Serializable
data class PerseveranceCameraDto(@SerialName("instrument") val instrument: String)

@Serializable
data class PerseveranceImageFilesDto(
    @SerialName("small") val small: String? = null,
    @SerialName("medium") val medium: String? = null,
    @SerialName("large") val large: String? = null,
    @SerialName("full_res") val fullRes: String? = null
)

@Serializable
data class CuriosityPhotosResponseDto(@SerialName("items") val items: List<CuriosityImageDto>)

@Serializable
data class CuriosityImageDto(
    @SerialName("imageid") val imageId: String,
    @SerialName("sol") val sol: Int,
    @SerialName("instrument") val instrument: String,
    @SerialName("url") val url: String,
    @SerialName("https_url") val httpsUrl: String? = null,
    @SerialName("date_taken") val dateTaken: String? = null
)

package com.dskochilov.cosmos.core.network.mars

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw responses of the Mars Rover Photos API
 * (`/mars-photos/api/v1/rovers/{rover}/photos` and `/mars-photos/api/v1/manifests/{rover}`).
 */
@Serializable
data class MarsPhotosResponseDto(@SerialName("photos") val photos: List<MarsPhotoDto>)

@Serializable
data class MarsPhotoDto(
    @SerialName("id") val id: Long,
    @SerialName("sol") val sol: Int,
    @SerialName("camera") val camera: MarsCameraDto,
    @SerialName("img_src") val imageUrl: String,
    @SerialName("earth_date") val earthDate: String,
    @SerialName("rover") val rover: MarsRoverDto
)

@Serializable
data class MarsCameraDto(@SerialName("name") val name: String, @SerialName("full_name") val fullName: String)

@Serializable
data class MarsRoverDto(@SerialName("name") val name: String)

@Serializable
data class MarsManifestResponseDto(@SerialName("photo_manifest") val photoManifest: MarsManifestDto)

@Serializable
data class MarsManifestDto(
    @SerialName("name") val name: String,
    @SerialName("max_sol") val maxSol: Int,
    @SerialName("max_date") val maxDate: String,
    @SerialName("total_photos") val totalPhotos: Long
)

package com.dskochilov.cosmos.core.network.images

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw response of `GET images-api.nasa.gov/search` (collection+json flavored).
 */
@Serializable
data class NasaImagesSearchResponseDto(@SerialName("collection") val collection: NasaImagesCollectionDto)

@Serializable
data class NasaImagesCollectionDto(
    @SerialName("items") val items: List<NasaImagesItemDto> = emptyList(),
    @SerialName("metadata") val metadata: NasaImagesMetadataDto? = null
)

@Serializable
data class NasaImagesMetadataDto(@SerialName("total_hits") val totalHits: Int = 0)

@Serializable
data class NasaImagesItemDto(
    @SerialName("data") val data: List<NasaImagesItemDataDto> = emptyList(),
    @SerialName("links") val links: List<NasaImagesLinkDto> = emptyList()
)

@Serializable
data class NasaImagesItemDataDto(
    @SerialName("nasa_id") val nasaId: String,
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String? = null,
    @SerialName("date_created") val dateCreated: String? = null,
    @SerialName("center") val center: String? = null
)

@Serializable
data class NasaImagesLinkDto(
    @SerialName("href") val href: String,
    @SerialName("rel") val rel: String? = null,
    @SerialName("render") val render: String? = null
)

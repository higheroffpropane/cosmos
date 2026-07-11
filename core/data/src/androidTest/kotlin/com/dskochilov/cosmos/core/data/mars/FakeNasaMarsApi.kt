package com.dskochilov.cosmos.core.data.mars

import com.dskochilov.cosmos.core.network.mars.CuriosityImageDto
import com.dskochilov.cosmos.core.network.mars.CuriosityPhotosResponseDto
import com.dskochilov.cosmos.core.network.mars.NasaMarsApi
import com.dskochilov.cosmos.core.network.mars.PerseveranceCameraDto
import com.dskochilov.cosmos.core.network.mars.PerseveranceImageDto
import com.dskochilov.cosmos.core.network.mars.PerseveranceImageFilesDto
import com.dskochilov.cosmos.core.network.mars.PerseverancePhotosResponseDto

/**
 * Scriptable fake serving Curiosity pages from [curiosityPages]; page indexes
 * without an entry return an empty page.
 */
internal class FakeNasaMarsApi(private val curiosityPages: Map<Int, List<CuriosityImageDto>>) : NasaMarsApi {

    var photoRequests: Int = 0
        private set

    override suspend fun getPerseverancePhotos(perPage: Int, page: Int): PerseverancePhotosResponseDto {
        photoRequests += 1
        return PerseverancePhotosResponseDto(images = emptyList(), totalResults = 0)
    }

    override suspend fun getCuriosityPhotos(perPage: Int, page: Int): CuriosityPhotosResponseDto {
        photoRequests += 1
        return CuriosityPhotosResponseDto(items = curiosityPages[page].orEmpty())
    }
}

internal fun fakeCuriosityPhoto(id: String, sol: Int, instrument: String = "MAST_LEFT") = CuriosityImageDto(
    imageId = id,
    sol = sol,
    instrument = instrument,
    url = "https://mars.nasa.gov/$id.jpg",
    httpsUrl = "https://mars.nasa.gov/$id.jpg",
    dateTaken = "2026-07-01T00:00:00.000Z"
)

@Suppress("unused")
internal fun fakePerseverancePhoto(id: String, sol: Int) = PerseveranceImageDto(
    imageId = id,
    sol = sol,
    camera = PerseveranceCameraDto(instrument = "NAVCAM_LEFT"),
    imageFiles = PerseveranceImageFilesDto(medium = "https://mars.nasa.gov/$id.jpg"),
    dateTakenUtc = "2026-07-01T00:00:00.000"
)

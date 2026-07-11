package com.dskochilov.cosmos.core.data.mars

import com.dskochilov.cosmos.core.network.mars.MarsCameraDto
import com.dskochilov.cosmos.core.network.mars.MarsManifestDto
import com.dskochilov.cosmos.core.network.mars.MarsManifestResponseDto
import com.dskochilov.cosmos.core.network.mars.MarsPhotoDto
import com.dskochilov.cosmos.core.network.mars.MarsPhotosResponseDto
import com.dskochilov.cosmos.core.network.mars.MarsRoverDto
import com.dskochilov.cosmos.core.network.mars.NasaMarsApi

/**
 * Scriptable fake: photos are served per (sol, page); everything else is empty.
 */
internal class FakeNasaMarsApi(private val maxSol: Int, private val pages: Map<Pair<Int, Int>, List<MarsPhotoDto>>) :
    NasaMarsApi {

    var photoRequests: Int = 0
        private set

    override suspend fun getPhotos(rover: String, sol: Int, page: Int, camera: String?): MarsPhotosResponseDto {
        photoRequests += 1
        return MarsPhotosResponseDto(photos = pages[sol to page].orEmpty())
    }

    override suspend fun getManifest(rover: String): MarsManifestResponseDto = MarsManifestResponseDto(
        photoManifest = MarsManifestDto(
            name = rover,
            maxSol = maxSol,
            maxDate = "2026-07-07",
            totalPhotos = pages.values.sumOf { it.size }.toLong()
        )
    )
}

internal fun fakePhoto(id: Long, sol: Int, camera: String = "NAVCAM") = MarsPhotoDto(
    id = id,
    sol = sol,
    camera = MarsCameraDto(name = camera, fullName = "$camera full name"),
    imageUrl = "https://mars.nasa.gov/$id.jpg",
    earthDate = "2026-01-01",
    rover = MarsRoverDto(name = "Curiosity")
)

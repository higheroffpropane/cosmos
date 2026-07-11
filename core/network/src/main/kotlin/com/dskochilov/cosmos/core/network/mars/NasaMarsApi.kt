package com.dskochilov.cosmos.core.network.mars

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NasaMarsApi {
    /**
     * Photos taken by [rover] on Martian day [sol], paged by [page] (25 per page).
     * [camera] optionally narrows to one camera (API short name, e.g. `FHAZ`).
     */
    @GET("mars-photos/api/v1/rovers/{rover}/photos")
    suspend fun getPhotos(
        @Path("rover") rover: String,
        @Query("sol") sol: Int,
        @Query("page") page: Int,
        @Query("camera") camera: String?
    ): MarsPhotosResponseDto

    /**
     * Mission manifest; `max_sol` is the most recent Martian day with photos.
     */
    @GET("mars-photos/api/v1/manifests/{rover}")
    suspend fun getManifest(@Path("rover") rover: String): MarsManifestResponseDto
}

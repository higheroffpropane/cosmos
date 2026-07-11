package com.dskochilov.cosmos.core.network.images

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * NASA Image and Video Library (keyless; absolute URL overrides the Retrofit base URL).
 */
interface NasaImagesApi {

    @GET("https://images-api.nasa.gov/search?media_type=image")
    suspend fun search(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): NasaImagesSearchResponseDto
}

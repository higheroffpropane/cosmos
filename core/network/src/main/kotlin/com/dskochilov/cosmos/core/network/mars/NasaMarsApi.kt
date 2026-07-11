package com.dskochilov.cosmos.core.network.mars

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Raw-image feeds hosted on mars.nasa.gov (absolute URLs override the Retrofit base URL).
 * These are the same endpoints NASA's own mission pages use; the officially documented
 * api.nasa.gov/mars-photos proxy is permanently offline (ADR 0006).
 */
interface NasaMarsApi {

    @GET("https://mars.nasa.gov/rss/api/?feed=raw_images&category=mars2020&feedtype=json&order=sol+desc")
    suspend fun getPerseverancePhotos(
        @Query("num") perPage: Int,
        @Query("page") page: Int
    ): PerseverancePhotosResponseDto

    @GET("https://mars.nasa.gov/api/v1/raw_image_items/?order=sol+desc&condition_1=msl:mission")
    suspend fun getCuriosityPhotos(
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): CuriosityPhotosResponseDto
}

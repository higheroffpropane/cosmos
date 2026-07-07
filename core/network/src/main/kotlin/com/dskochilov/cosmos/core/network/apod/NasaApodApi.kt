package com.dskochilov.cosmos.core.network.apod

import retrofit2.http.GET
import retrofit2.http.Query

interface NasaApodApi {
    /**
     * Fetches the APOD entry for [date] (`yyyy-MM-dd`), or today's when null.
     * [thumbs] asks the API to include a video thumbnail for video entries.
     */
    @GET("planetary/apod")
    suspend fun getApod(@Query("date") date: String?, @Query("thumbs") thumbs: Boolean): ApodDto
}

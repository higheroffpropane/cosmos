package com.dskochilov.cosmos.core.network.di

import com.dskochilov.cosmos.core.network.ApiKeyInterceptor
import com.dskochilov.cosmos.core.network.BuildConfig
import com.dskochilov.cosmos.core.network.apod.NasaApodApi
import com.dskochilov.cosmos.core.network.images.NasaImagesApi
import com.dskochilov.cosmos.core.network.mars.NasaMarsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val NASA_BASE_URL = "https://api.nasa.gov/"
private const val CONNECT_TIMEOUT_SECONDS = 15L

// The mars.nasa.gov raw-image feeds routinely take ~15s to answer.
private const val READ_TIMEOUT_SECONDS = 30L

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun providesJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(apiKeyInterceptor: ApiKeyInterceptor): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(apiKeyInterceptor)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        // BODY would dump full APOD explanations; BASIC is enough to debug calls.
                        redactQueryParams("api_key")
                        level = HttpLoggingInterceptor.Level.BASIC
                    }
                )
            }
        }
        .build()

    @Provides
    @Singleton
    fun providesRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(NASA_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun providesNasaApodApi(retrofit: Retrofit): NasaApodApi = retrofit.create(NasaApodApi::class.java)

    @Provides
    @Singleton
    fun providesNasaMarsApi(retrofit: Retrofit): NasaMarsApi = retrofit.create(NasaMarsApi::class.java)

    @Provides
    @Singleton
    fun providesNasaImagesApi(retrofit: Retrofit): NasaImagesApi = retrofit.create(NasaImagesApi::class.java)
}

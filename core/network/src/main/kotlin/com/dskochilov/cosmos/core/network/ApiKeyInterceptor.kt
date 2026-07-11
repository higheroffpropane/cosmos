package com.dskochilov.cosmos.core.network

import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

private const val NASA_API_HOST = "api.nasa.gov"

/**
 * Appends the NASA `api_key` query parameter to api.nasa.gov requests so that
 * individual API declarations never deal with authentication. Other hosts
 * (e.g. the keyless mars.nasa.gov feeds) are left untouched.
 */
internal class ApiKeyInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.host != NASA_API_HOST) {
            return chain.proceed(request)
        }
        val authenticatedUrl = request.url.newBuilder()
            .addQueryParameter("api_key", BuildConfig.NASA_API_KEY)
            .build()
        return chain.proceed(request.newBuilder().url(authenticatedUrl).build())
    }
}

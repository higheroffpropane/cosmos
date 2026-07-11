package com.dskochilov.cosmos.core.network

import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Appends the NASA `api_key` query parameter to every request so that
 * individual API declarations never deal with authentication.
 */
internal class ApiKeyInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authenticatedUrl = request.url.newBuilder()
            .addQueryParameter("api_key", BuildConfig.NASA_API_KEY)
            .build()
        return chain.proceed(request.newBuilder().url(authenticatedUrl).build())
    }
}

package com.example.sportsbook.data.remote.api

import com.example.sportsbook.data.remote.firebase.FirebaseTokenProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: FirebaseTokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runBlocking { tokenProvider.getIdToken() }

        if (token == null) {
            return chain.proceed(originalRequest)
        }

        val response = chain.proceed(
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        )

        if (response.code == 401) {
            response.close()
            val freshToken = runBlocking { tokenProvider.getIdToken(forceRefresh = true) }
                ?: return chain.proceed(originalRequest)
            return chain.proceed(
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $freshToken")
                    .build()
            )
        }

        return response
    }
}

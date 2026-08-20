package com.clearance.app.data.api

import com.clearance.app.BuildConfig
import com.clearance.app.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Central place that builds the Retrofit + OkHttp instance used for
 * ALL calls to the existing Node.js/Express backend.
 *
 * BuildConfig.API_BASE_URL comes from API_BASE_URL in the root
 * gradle.properties file (Stage 2) — for physical-phone testing this
 * must be http://192.168.100.6:5000/, NOT 10.0.2.2 (emulator-only).
 *
 * STAGE 3B CHANGES (both isolated to this file, nothing else changed):
 *
 * 1. AuthInterceptor — NEW. Automatically attaches
 *    "Authorization: Bearer <token>" to outgoing requests whenever a
 *    token is stored in SessionManager. Login itself has no token
 *    yet, so no header is attached for that specific call. This is
 *    the foundation later API calls (student/department/admin/etc.)
 *    reuse automatically without extra wiring.
 *
 * 2. HttpLoggingInterceptor level — CHANGED from BODY to BASIC.
 *    Level.BODY was fine in Stage 2 when there were no real endpoints
 *    yet, but it would print full request/response BODIES to Logcat
 *    — including the plaintext "password" field sent to
 *    POST /api/auth/login. Level.BASIC still logs method, URL,
 *    response code, and timing (useful for confirming connectivity to
 *    192.168.100.6:5000) without ever printing headers or bodies, so
 *    credentials never reach Logcat.
 */
object RetrofitClient {

    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val token = SessionManager.getToken()

            val requestToSend = if (!token.isNullOrEmpty()) {
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                originalRequest
            }

            return chain.proceed(requestToSend)
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
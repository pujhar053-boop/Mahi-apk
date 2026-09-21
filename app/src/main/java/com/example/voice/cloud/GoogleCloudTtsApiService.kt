package com.example.voice.cloud

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GoogleCloudTtsApiService {
    @POST("v1/text:synthesize")
    suspend fun synthesizeSpeech(
        @Query("key") apiKey: String,
        @Body request: GoogleTtsRequest
    ): GoogleTtsResponse
}

object GoogleCloudTtsClient {
    private const val BASE_URL = "https://texttospeech.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val apiService: GoogleCloudTtsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GoogleCloudTtsApiService::class.java)
    }
}

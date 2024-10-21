package com.sleepinggrizzly.alguardianguyproject.video

import com.sleepinggrizzly.alguardianguyproject.ApiConstants.BASE_URL
import com.sleepinggrizzly.alguardianguyproject.video.DeleteFileService
import com.sleepinggrizzly.alguardianguyproject.video.SendFilesService
import com.sleepinggrizzly.alguardianguyproject.video.SendMessageService
import com.sleepinggrizzly.alguardianguyproject.video.VideoStatusService
import com.sleepinggrizzly.alguardianguyproject.video.VideoUploadService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies (optional)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Add the logging interceptor (optional)
        .build()

    val videoUploadService: VideoUploadService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient).addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VideoUploadService::class.java)
    }

    val videoStatusService: VideoStatusService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient).addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VideoStatusService::class.java)
    }

    val sendFilesService: SendFilesService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient).addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SendFilesService::class.java)
    }

    val deleteFileService: DeleteFileService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient).addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeleteFileService::class.java)
    }

    val sendMessageService: SendMessageService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient).addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SendMessageService::class.java)
    }
}

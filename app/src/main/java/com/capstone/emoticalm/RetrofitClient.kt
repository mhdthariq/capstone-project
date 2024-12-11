package com.capstone.emoticalm

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://flask-api2-45923905449.asia-southeast1.run.app/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)  // Increase the connection timeout
        .writeTimeout(60, TimeUnit.SECONDS)    // Increase the write timeout
        .readTimeout(60, TimeUnit.SECONDS)     // Increase the read timeout
        .build()

    val apiService: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}
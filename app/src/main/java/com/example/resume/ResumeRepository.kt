package com.example.resume

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ResumeRepository {
    private const val BASE_URL = "https://example.com/" // TODO: replace with your backend URL

    private val api: ResumeApi by lazy {
        val logging = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BASIC) }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ResumeApi::class.java)
    }

    suspend fun getResume(): Resume {
        return api.getResume()
    }
}


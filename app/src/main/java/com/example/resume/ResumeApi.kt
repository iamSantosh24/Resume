package com.example.resume

import retrofit2.http.GET

interface ResumeApi {
    @GET("resume")
    suspend fun getResume(): Resume
}


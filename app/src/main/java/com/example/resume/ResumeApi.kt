package com.example.resume

import retrofit2.http.GET

interface ResumeApi {
    // The admin backend exposes /resume (server reads/writes the RTDB path users/myResumeProfile)
    @GET("resume")
    suspend fun getResumeRaw(): Map<String, Any>
}

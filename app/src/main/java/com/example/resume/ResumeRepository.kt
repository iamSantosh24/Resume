package com.example.resume

import com.example.resume.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.firebase.FirebaseApp

object ResumeRepository {
    // Use the Gradle-provided BuildConfig field so the URL can be configured per-build.
    private val BASE_URL: String = BuildConfig.BASE_URL

    private val api: ResumeApi by lazy {
        // For more detailed debugging during development, log request and response bodies.
        val logging = HttpLoggingInterceptor().apply {
            // Only enable BODY-level logging in debug builds; otherwise use BASIC to reduce noise.
            setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC)
        }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ResumeApi::class.java)
    }

    // Try network first (Retrofit). If that fails, only fall back to Firebase if it's initialized.
    suspend fun getResume(): Resume {
        return try {
            api.getResume()
        } catch (networkException: Exception) {
            // Only attempt Firebase fallback if FirebaseApp is initialized in the process.
            val firebaseInitialized = try {
                FirebaseApp.getInstance(); true
            } catch (ie: IllegalStateException) {
                false
            }

            return if (firebaseInitialized) {
                FirebaseResumeRepository.getResume()
            } else {
                // Re-throw the original network exception so the UI can show a meaningful error
                throw networkException
            }
        }
    }
}

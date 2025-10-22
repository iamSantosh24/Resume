package com.example.resume

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.firebase.FirebaseApp
import java.util.concurrent.TimeUnit

object ResumeRepository {
    // Use the Gradle-provided BuildConfig field so the URL can be configured per-build.
    private val BASE_URL: String = BuildConfig.BASE_URL

    private val api: ResumeApi by lazy {
        // For more detailed debugging during development, log request and response bodies.
        val logging = HttpLoggingInterceptor().apply {
            // Only enable BODY-level logging in debug builds; otherwise use BASIC to reduce noise.
            setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC)
        }
        // Add sensible timeouts to avoid very short default timeouts causing spurious failures
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ResumeApi::class.java)
    }

    // Try network first (Retrofit). If that fails, only fall back to Firebase if it's initialized.
    suspend fun getResume(): Resume {
        // Detect whether Firebase is initialized in this process.
        val firebaseInitialized = try {
            FirebaseApp.getInstance(); true
        } catch (ie: IllegalStateException) {
            false
        }

        // If the build is configured to prefer Firebase and Firebase is available, use it first.
        if (firebaseInitialized && BuildConfig.PREFER_FIREBASE) {
            Log.i("ResumeRepository", "PREFER_FIREBASE=true and Firebase is initialized — using Firebase as primary data source")
            return FirebaseResumeRepository.getResume()
        }

        return try {
            // Call the raw endpoint and parse into the app Resume model with tolerant parsing
            val raw = api.getResumeRaw()
            Log.i("ResumeRepository", "Loaded resume from network: ${'$'}{BASE_URL}")
            parseResumeFromMap(raw)
        } catch (networkException: Exception) {
            Log.w("ResumeRepository", "Network load failed, will attempt Firebase fallback if available: ${'$'}{networkException.message}")
            // Only attempt Firebase fallback if FirebaseApp is initialized in the process.
            return if (firebaseInitialized) {
                Log.i("ResumeRepository", "Falling back to Firebase because network failed and Firebase is initialized")
                FirebaseResumeRepository.getResume()
            } else {
                // Re-throw the original network exception so the UI can show a meaningful error
                throw networkException
            }
        }
    }
}

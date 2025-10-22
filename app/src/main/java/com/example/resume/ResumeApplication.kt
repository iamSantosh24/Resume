package com.example.resume

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class ResumeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            var app = FirebaseApp.initializeApp(this)
            if (app == null) {
                // Try initialization from BuildConfig if the google-services.json isn't present
                val appId = BuildConfig.FIREBASE_APP_ID
                val apiKey = BuildConfig.FIREBASE_API_KEY
                val dbUrl = BuildConfig.FIREBASE_DATABASE_URL
                val projectId = BuildConfig.FIREBASE_PROJECT_ID
                if (appId.isNotBlank() && apiKey.isNotBlank() && dbUrl.isNotBlank()) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId(appId)
                        .setApiKey(apiKey)
                        .setDatabaseUrl(dbUrl)
                        .setProjectId(projectId.takeIf { it.isNotBlank() })
                        .build()
                    app = FirebaseApp.initializeApp(this, options)
                    Log.i("ResumeApplication", "Firebase initialized from BuildConfig fields: appId=$appId")
                } else {
                    Log.i("ResumeApplication", "Firebase not initialized: missing google-services.json and BuildConfig fields")
                }
            } else {
                Log.i("ResumeApplication", "Firebase default app initialized")
            }

            // Runtime visibility: print key BuildConfig values so we can confirm what the build injected
            Log.i("ResumeApplication", "BuildConfig.BASE_URL=${BuildConfig.BASE_URL}")
            Log.i("ResumeApplication", "BuildConfig.PREFER_FIREBASE=${BuildConfig.PREFER_FIREBASE}")
            Log.i("ResumeApplication", "BuildConfig.FIREBASE_DATABASE_URL=${BuildConfig.FIREBASE_DATABASE_URL}")

        } catch (t: Throwable) {
            Log.w("ResumeApplication", "Firebase initialization failed", t)
        }
    }
}

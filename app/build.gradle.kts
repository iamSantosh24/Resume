plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Google Services plugin (enables reading google-services.json and Firebase setup)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.resume"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.resume"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Expose a default BASE_URL to the app via BuildConfig. This will be
        // filled from gradle.properties when available (PROD_BASE_URL), otherwise
        // it falls back to the hardcoded production placeholder.
        val prodUrl: String = (project.findProperty("PROD_BASE_URL") as String?)
            ?: "https://api.yourdomain.com/"
        buildConfigField("String", "BASE_URL", "\"$prodUrl\"")

        // Optional Firebase configuration values. You can either drop google-services.json
        // in the app/ directory (preferred) or set these properties in gradle.properties
        // for local/dev builds (less secure). Example keys to set in gradle.properties:
        // FIREBASE_APP_ID=1:1234567890:android:abcdef123456
        // FIREBASE_API_KEY=AIzaSy...
        // FIREBASE_DATABASE_URL=https://your-project.firebaseio.com
        // FIREBASE_PROJECT_ID=your-project-id
        val fbAppId: String = (project.findProperty("FIREBASE_APP_ID") as String?) ?: ""
        val fbApiKey: String = (project.findProperty("FIREBASE_API_KEY") as String?) ?: ""
        val fbDatabaseUrl: String = (project.findProperty("FIREBASE_DATABASE_URL") as String?) ?: ""
        val fbProjectId: String = (project.findProperty("FIREBASE_PROJECT_ID") as String?) ?: ""
        buildConfigField("String", "FIREBASE_APP_ID", "\"$fbAppId\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"$fbApiKey\"")
        buildConfigField("String", "FIREBASE_DATABASE_URL", "\"$fbDatabaseUrl\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$fbProjectId\"")

        // New flag to let builds prefer Firebase as the primary data source.
        // Default is false (use network first). Debug build will override to true.
        val preferFbProp: String = (project.findProperty("PREFER_FIREBASE") as String?) ?: "false"
        buildConfigField("boolean", "PREFER_FIREBASE", preferFbProp)
    }

    buildTypes {
        // Debug buildType: point to local dev server accessible from emulator.
        debug {
            // Emulator (Android AVD) -> host machine's localhost is 10.0.2.2
            // Allow overriding the debug host via a Gradle property named DEV_HOST.
            // Point debug builds at the admin server running on host:4000
            val devHostRaw: String = (project.findProperty("DEV_HOST") as String?) ?: "http://10.0.2.2:4000"
            val devHost = if (devHostRaw.endsWith("/")) devHostRaw else "$devHostRaw/"
            buildConfigField("String", "BASE_URL", "\"$devHost\"")

            // For debugging, prefer Firebase (if initialized) instead of the local server.
            buildConfigField("boolean", "PREFER_FIREBASE", "true")
        }

        // Staging buildType: useful to test against a staging server.
        create("staging") {
            // You can set STAGING_BASE_URL in gradle.properties to override this.
            val stagingUrl: String = (project.findProperty("STAGING_BASE_URL") as String?)
                ?: "https://staging-api.yourdomain.com/"
            buildConfigField("String", "BASE_URL", "\"$stagingUrl\"")
            initWith(getByName("debug"))
            matchingFallbacks += listOf("debug")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Release build should use production backend (same as defaultConfig).
            val prodUrlRelease: String = (project.findProperty("PROD_BASE_URL") as String?)
                ?: "https://api.yourdomain.com/"
            buildConfigField("String", "BASE_URL", "\"$prodUrlRelease\"")

            // Ensure release does not prefer Firebase unless explicitly set in gradle.properties.
            buildConfigField("boolean", "PREFER_FIREBASE", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        // enable Compose
        compose = true
        // Ensure BuildConfig fields from build.gradle are generated and available in code
        buildConfig = true
    }
    // Compose compiler extension version - pick a reasonably recent stable version
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.5")

    // Jetpack Compose (using explicit versions; keep these if you hit compiler mismatches)
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.material:material:1.5.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
    // Material icons (extended) to get extra icons like Code and Business
    implementation("androidx.compose.material:material-icons-extended:1.5.3")
    // Coil for image loading in Compose
    implementation("io.coil-kt:coil-compose:2.4.0")
    // libphonenumber for robust phone parsing/formatting
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.29")
    // Tooling artifact (debug only) - provides classes used by the Compose tooling (previews, inspectors)
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")

    // Networking: Retrofit + Gson + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Allow unit tests to make HTTP calls to the local admin server
    testImplementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Firebase Realtime Database (BOM + KTX)
    implementation(platform("com.google.firebase:firebase-bom:32.1.0"))
    implementation("com.google.firebase:firebase-database-ktx")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

// Add buildscript classpath for Google Services plugin so it can be applied in the app module.
// We add the classpath dependency in a buildscript block which is compatible with the current
// Kotlin DSL root build file layout. Using a recent stable google-services plugin version.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}

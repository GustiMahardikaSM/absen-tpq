// build.gradle.kts - Konfigurasi build root untuk aplikasi TPQ
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false // Android application plugin
    alias(libs.plugins.kotlin.android) apply false // Kotlin Android plugin
    alias(libs.plugins.kotlin.compose) apply false // Kotlin Compose plugin
    id("com.google.devtools.ksp") version "2.0.0-1.0.24" apply false // KSP plugin untuk Room compiler (kompatibel Kotlin 2.0.0)
}
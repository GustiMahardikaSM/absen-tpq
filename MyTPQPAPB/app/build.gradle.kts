// build.gradle.kts - Konfigurasi build untuk aplikasi TPQ
plugins {
    alias(libs.plugins.android.application) // Android application plugin
    alias(libs.plugins.kotlin.android) // Kotlin Android plugin
    id("com.google.devtools.ksp") version "2.0.0-1.0.24" // KSP untuk Room compiler (kompatibel Kotlin 2.0.0)
    alias(libs.plugins.kotlin.compose) // Kotlin Compose plugin
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tpqku" // Package name aplikasi
        minSdk = 24 // Minimum Android API level
        targetSdk = 35 // Target Android API level
        versionCode = 1 // Version code untuk Play Store
        versionName = "1.0" // Version name untuk user

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Disable minification untuk development
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Java 17 compatibility
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true // Enable desugaring untuk java.time APIs
    }
    kotlinOptions {
        jvmTarget = "17" // Kotlin JVM target
    }
    buildFeatures {
        compose = true // Enable Jetpack Compose
    }
    
    lint {
        abortOnError = false // Disable lint errors untuk development
        checkReleaseBuilds = false
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Core library desugaring untuk java.time APIs (LocalDate, LocalDateTime, dll)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // Room Database dependencies
    implementation("androidx.room:room-runtime:2.6.1") // Room runtime
    implementation("androidx.room:room-ktx:2.6.1") // Room Kotlin extensions
    ksp("androidx.room:room-compiler:2.6.1") // Room compiler (KSP)
    
    // Navigation Component
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // ViewModel dan Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Gson untuk JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")

    // Extended Material Icons untuk icon tambahan
    implementation("androidx.compose.material:material-icons-extended")
    
    // Accompanist SwipeRefresh untuk pull-to-refresh
    implementation("com.google.accompanist:accompanist-swiperefresh:0.33.2-alpha")
    
    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
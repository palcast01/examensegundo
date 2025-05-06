plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.examensegundo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.examensegundo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0"
    }
}

dependencies {
    // Dependencias principales de Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui:1.6.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.4")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")


    // Dependencias de AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Dependencias para pruebas en Compose
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.4")

    // Dependencias para pruebas y testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Dependencias adicionales (opcional)
    implementation("androidx.core:core-ktx:1.9.0")  // Para trabajo con almacenamiento y archivos

    // Dependencias de material3 para los componentes de UI
    implementation(libs.androidx.material3)

    // Dependencia para manejar Kotlin Serialization (opcional)
    // implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}

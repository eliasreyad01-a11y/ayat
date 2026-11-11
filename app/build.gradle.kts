plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ayat.tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ayat.tv"
        minSdk = 21
        targetSdk = 34
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
        versionName = "1.0"
    }
    signingConfigs {
        create("ayat") {
            storeFile = file("ayat.keystore")
            storePassword = "ayat123456"
            keyAlias = "ayat"
            keyPassword = "ayat123456"
        }
    }
    buildTypes {
        debug { signingConfig = signingConfigs.getByName("ayat") }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("ayat")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.11.0")
}

plugins {
    id("com.android.application")
    id("com.code-intelligence.cifuzz") version "dev"
}

// cifuzz.androidVariant.set("debug")

android {
    namespace = "org.example"
    compileSdk = 33

    defaultConfig {
        applicationId = "org.example"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(project(":lib"))
}

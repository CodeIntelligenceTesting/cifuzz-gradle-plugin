import com.android.build.api.variant.AndroidComponentsExtension
import com.code_intelligence.cifuzz.tasks.ClasspathPrinter

plugins {
    id("com.android.application") version "7.4.2"
    id("org.jetbrains.kotlin.android") version "1.8.20"
    id("com.code-intelligence.cifuzz") version "dev"
}

// cifuzz.androidVariant.set("release")

repositories {
    mavenCentral()
    google()
}

android {
    namespace = "org.example"
    compileSdk = 33

    defaultConfig {
        applicationId = "org.example"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("com.code-intelligence:jazzer-junit:0.15.0")
}
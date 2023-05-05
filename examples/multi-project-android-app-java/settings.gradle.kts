pluginManagement {
    includeBuild("../..")
    repositories.google()
    repositories.gradlePluginPortal()

    plugins {
        id("com.android.application") version "7.4.2"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include("app")
include("lib")
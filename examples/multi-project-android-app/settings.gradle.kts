pluginManagement {
    includeBuild("../..")
    repositories.google()
    repositories.gradlePluginPortal()
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include("app")
include("lib")
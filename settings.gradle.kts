plugins {
    id("com.gradle.enterprise") version "3.12.1"
}

dependencyResolutionManagement {
    repositories.gradlePluginPortal()
    repositories.google()
}

gradleEnterprise {
    buildScan {
        publishAlways()
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

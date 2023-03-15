pluginManagement {
    includeBuild("gradle/plugins")
}

dependencyResolutionManagement {
    repositories.mavenCentral()
}

include("module-a")
include("module-b")
include("module-c")

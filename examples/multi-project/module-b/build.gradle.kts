plugins {
    id("java-library")
}

repositories.mavenCentral()

dependencies {
    api(project(":module-a"))
}
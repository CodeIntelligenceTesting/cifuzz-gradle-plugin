plugins {
    id("org.example.gradle.java-library")
}

dependencies {
    api(project(":module-a"))
}
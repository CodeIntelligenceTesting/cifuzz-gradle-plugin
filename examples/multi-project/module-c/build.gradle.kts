plugins {
    id("org.example.gradle.java-library")
}

dependencies {
    implementation(project(":module-b"))
}
plugins {
    id("java-library")
    id("com.code-intelligence.cifuzz") version "0.1"
}

repositories.mavenCentral()

dependencies {
    implementation(project(":module-b"))
}

plugins {
    id("java-library")
    id("com.code-intelligence.cifuzz") version "dev"
}

repositories.mavenCentral()

dependencies {
    implementation(project(":module-b"))
}

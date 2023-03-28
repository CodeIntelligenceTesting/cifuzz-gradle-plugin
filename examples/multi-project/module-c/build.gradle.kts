plugins {
    id("java-library")
    id("com.code-intelligence.cifuzz")
}

repositories.mavenCentral()

dependencies {
    implementation(project(":module-b"))
}

plugins {
    id("com.code-intelligence.cifuzz") version "dev"
    id("java-library")
    id("scala")
}

repositories.mavenCentral()

dependencies {
    implementation("org.scala-lang:scala-library:2.13.9")
}
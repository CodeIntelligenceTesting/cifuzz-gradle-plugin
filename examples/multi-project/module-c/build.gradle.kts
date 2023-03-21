plugins {
    id("java-library")
    id("com.code-intelligence.cifuzz") version "0.1"
}

repositories.mavenCentral()

dependencies {
    implementation(project(":module-b"))
}

/*
testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
    dependencies {
        implementation("com.code-intelligence:jazzer-junit:0.15.0")
    }
}
*/

tasks.test {
    useJUnitPlatform()
}
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("com.code-intelligence:jazzer-junit:0.15.0")
}

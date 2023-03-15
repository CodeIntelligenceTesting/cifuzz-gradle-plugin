plugins {
    id("com.code-intelligence.cifuzz") version "0.1"
    id("java-library")
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
}

testing.suites.register<JvmTestSuite>("integrationTest") {
    dependencies {
        implementation(project(path))
        implementation("com.code-intelligence:jazzer-junit:0.15.0")
    }
}

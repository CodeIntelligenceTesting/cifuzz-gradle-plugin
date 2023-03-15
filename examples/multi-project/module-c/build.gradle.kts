plugins {
    id("java-library")
    id("com.code-intelligence.cifuzz") version "0.1"
}


testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
}

dependencies {
    implementation(project(":module-b"))

    testImplementation("com.code-intelligence:jazzer-junit:0.15.0")
}

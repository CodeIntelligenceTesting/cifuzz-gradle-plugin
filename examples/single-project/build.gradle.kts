plugins {
    id("com.code-intelligence.cifuzz")
    id("java-library")
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
}

testing.suites.register<JvmTestSuite>("integrationTest") {
    useJUnitJupiter()
}

dependencies {
    testImplementation("com.code-intelligence:jazzer-junit:0.15.0")
}

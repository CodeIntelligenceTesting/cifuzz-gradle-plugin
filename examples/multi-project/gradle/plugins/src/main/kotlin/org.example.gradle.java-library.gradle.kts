plugins {
    id("com.code-intelligence.cifuzz")
    id("java-library")
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
}

dependencies {
    testImplementation("com.code-intelligence:jazzer-junit:0.15.0")
}

plugins {
    id("com.code-intelligence.cifuzz") version "0.1"
    id("java-library")
}

repositories.mavenCentral()

/*
testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
}

testing.suites.register<JvmTestSuite>("integrationTest") {
    dependencies {
        implementation(project(path))
        implementation("com.code-intelligence:jazzer-junit:0.15.0")
    }
}
*/

tasks.test {
    useJUnitPlatform()
}

val integrationTest = sourceSets.create("integrationTest")
tasks.register<Test>(integrationTest.name) {
    classpath = integrationTest.runtimeClasspath
    testClassesDirs = integrationTest.output.classesDirs
    useJUnitPlatform()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    "integrationTestImplementation"(project(path))
    "integrationTestImplementation"("org.junit.jupiter:junit-jupiter:5.9.2")
    "integrationTestImplementation"("com.code-intelligence:jazzer-junit:0.15.0")
}

plugins {
    id("com.code-intelligence.cifuzz") version "0.1"
    id("java-library")
}

repositories.mavenCentral()

/*
testing.suites.register<JvmTestSuite>("fuzzTest") {
    dependencies {
        implementation(project(path))
    }
}
*/

val fuzzTest = sourceSets.create("fuzzTest")
tasks.register<Test>(fuzzTest.name) {
    classpath = fuzzTest.runtimeClasspath
    testClassesDirs = fuzzTest.output.classesDirs
    useJUnitPlatform()
}

cifuzz {
    testSourceSet.set(fuzzTest)
}

dependencies {
    "fuzzTestImplementation"(project(path))
}

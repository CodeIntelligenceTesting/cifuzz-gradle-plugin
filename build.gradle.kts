plugins {
    id("com.gradle.plugin-publish") version "1.1.0"
    `embedded-kotlin`
}

// Versions to test in addition to the version used to build the plugin (gradle/wrapper/gradle-wrapper.properties)
val testedGradleVersions = listOf("6.1.1", "7.0.2", "7.3.3", "7.4.2", "7.5.1", "7.6.1")

group = "com.code-intelligence"
version = providers.gradleProperty("pluginVersion").getOrElse("dev")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

gradlePlugin {
    plugins.create("cifuzz") {
        id = "$group.$name"
        implementationClass = "com.code_intelligence.cifuzz.CIFuzzPlugin"
        displayName = "cifuzz Gradle plugin"
        description = "cifuzz is a CLI tool that helps you to integrate and run fuzzing based tests into your project."
        website.set("https://www.code-intelligence.com")
        vcsUrl.set("https://github.com/CodeIntelligenceTesting/cifuzz-gradle-plugin")
        tags.addAll("cifuzz", "fuzz testing")
    }
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
    dependencies {
        implementation("org.hamcrest:hamcrest:2.2")
    }
}

testedGradleVersions.forEach { gradleVersionUnderTest ->
    val testGradle = tasks.register<Test>("testGradle$gradleVersionUnderTest") {
        group = "verification"
        description = "Runs tests against Gradle $gradleVersionUnderTest"
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        useJUnitPlatform()
        systemProperty("gradleVersionUnderTest", gradleVersionUnderTest)
    }
    tasks.check {
        dependsOn(testGradle)
    }
}

publishing.repositories.maven(layout.buildDirectory.dir("pluginUnderTestRepo"))

tasks.withType<Test>().configureEach {
    dependsOn(tasks.publish)
    maxParallelForks = 4
}

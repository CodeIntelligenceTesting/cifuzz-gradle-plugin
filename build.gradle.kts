plugins {
    id("com.gradle.plugin-publish") version "1.1.0"
    `embedded-kotlin`
}

// Versions to test in addition to the version used to build the plugin (gradle/wrapper/gradle-wrapper.properties)
val testedGradleVersions = listOf("6.1.1", "7.0.2", "7.3.3", "7.4.2", "7.5.1", "7.6.1")
// Versions for which the Android support, which is not available for older Gradle versions, is tested
val testedGradleVersionsAndroid = listOf("7.5.1", "7.6.1")
// Versions of the Android plugin to test in addition to the one used in examples/multi-project-android-app
val testedAndroidPluginVersions = listOf("8.0.0")

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

configurations.compileClasspath {
    // Allow Java 11 dependencies on compile classpath
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 11)
}
dependencies {
    compileOnly("com.android.tools.build:gradle:7.4.2")
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
    dependencies {
        implementation("org.hamcrest:hamcrest:2.2")
    }
    targets.all {
        testTask {
            // Android testing requires Java 11
            javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(11)) })
        }
    }
}

// Test additional Gradle versions
testedGradleVersions.forEach { gradleVersionUnderTest ->
    val testGradle = tasks.register<Test>("testGradle$gradleVersionUnderTest") {
        group = "verification"
        description = "Runs tests against Gradle $gradleVersionUnderTest"
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        systemProperty("gradleVersionUnderTest", gradleVersionUnderTest)
        if (testedGradleVersionsAndroid.contains(gradleVersionUnderTest)) {
            javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(11)) })
            useJUnitPlatform()
        } else {
            useJUnitPlatform { excludeTags = setOf("android") }
        }
    }
    tasks.check {
        dependsOn(testGradle)
    }
}

// Test additional Android Gradle Plugin versions (with the current Gradle version)
testedAndroidPluginVersions.forEach { androidPluginVersions ->
    val testGradle = tasks.register<Test>("testAndroid$androidPluginVersions") {
        group = "verification"
        description = "Runs tests against Android Gradle Plugin $androidPluginVersions"
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        systemProperty("androidPluginVersionUnderTest", androidPluginVersions)
        useJUnitPlatform { includeTags = setOf("android") }
        javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(17)) })
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

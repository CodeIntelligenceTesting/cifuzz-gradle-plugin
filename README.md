# CIFuzz Gradle plugin

[![Build Status](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2FCodeIntelligenceTesting%2Fcifuzz-gradle-plugin%2Fbadge%3Fref%3Dmain&style=flat)](https://actions-badge.atrox.dev/CodeIntelligenceTesting/cifuzz-gradle-plugin/goto?ref=main)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v?label=Plugin%20Portal&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fcom%2Fcode-intelligence%2Fcifuzz%2Fcom.code-intelligence.cifuzz.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/com.code-intelligence.cifuzz)

[cifuzz](https://github.com/CodeIntelligenceTesting/cifuzz) is a CLI tool that helps you to integrate and run fuzzing based tests into your project.
If you are using Gradle, add this plugin to your build.
    
## How to use

Apply the plugin directly in the `build.gradle` (or `build.gradle.kts`) files of the project containing fuzz tests and a `cifuzz.yaml`.
In a single project, this file is located in the root.
In a multi-project, it is located in one of the subproject folders.

See also [getting started with cifuzz](https://github.com/CodeIntelligenceTesting/cifuzz#getting-started).

```kotlin
plugins {
    id("com.code-intelligence.cifuzz") version "<<latest_version>>"
}
```

The minimum supported Gradle version is **Gradle 6.1**.

## Writing fuzz tests with Jazzer and JUnit 5

The plugin sets up everything to write and run fuzz tests with [Jazzer](https://github.com/CodeIntelligenceTesting/jazzer) and JUnit 5.
See the [Jazzer documentation](https://github.com/CodeIntelligenceTesting/jazzer#junit-5) for examples of such tests.
You can then use the [cifuzz](https://github.com/CodeIntelligenceTesting/cifuzz) tool to run the fuzz tests and also run them directly as regression tests through Gradle. 

## Configuration options

By default, the plugin expects all fuzz tests to be in the default test sources set, which is usually located in `src/test`.
If the tests are in a separate test source set – or test suite – you have to configure that.

If you use [test suites](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html) (available since Gradle 7.4) you can do the configuration as follows:
```kotlin
testing.suites.register("fuzzTest", JvmTestSuite::class) {
    cifuzz.testSourceSet.set(sources)
    // ... (further configuration of the test suite)
}
```

If you create the test source set directly you can do a configuration like this.

```kotlin
val fuzzTest = sourceSets.create("fuzzTest")
val fuzzTestTask = tasks.register("runFuzzTest", Test::class) {
    classpath = fuzzTest.runtimeClasspath
    testClassesDirs = fuzzTest.output.classesDirs
    // ... (further configuration of the custom test task)
}

cifuzz {
    testSourceSet.set(fuzzTest)
    testTask.set(specialTestTask)
}


cifuzz {
    testSourceSet.set(fuzzTest)
    testTask.set(fuzzTestTask) // only needed if the test task name is different from the source set name
}
```


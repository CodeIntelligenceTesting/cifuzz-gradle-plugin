package com.code_intelligence.cifuzz

import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test

/**
 * Configuration options for custom Gradle test setup.
 * This can be used to configure that fuzz tests are NOT located in the default source set (src/test).
 */
interface CIFuzzExtension {

    /**
     * Configure the source set that contains the fuzz tests – defaults to 'sourceSets.test'.
     */
    val testSourceSet: Property<SourceSet>

    /**
     * Configures the task that executes the fuzz tests - defaults to the task with the same name as the source set.
     */
    val testTask: Property<Test>

    /**
     * Configure the source set that contains the production code under tests – defaults to 'sourceSets.main'.
     */
    val mainSourceSet: Property<SourceSet>
}

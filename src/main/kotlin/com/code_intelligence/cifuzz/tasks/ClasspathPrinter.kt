package com.code_intelligence.cifuzz.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

/**
 * Prints the test runtime classpath for the fuzz tests.
 * This information is used by 'cifuzz'.
 */
abstract class ClasspathPrinter : DefaultTask() {

    @get:InputFiles
    abstract val testRuntimeClasspath: ConfigurableFileCollection

    @TaskAction
    fun print() {
        println("cifuzz.test.classpath=${testRuntimeClasspath.asPath}")
    }

}
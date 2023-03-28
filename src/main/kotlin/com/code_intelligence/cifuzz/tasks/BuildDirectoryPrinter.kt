package com.code_intelligence.cifuzz.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Prints the build directory of the project to wich the cifuzz plugin has been applied.
 * This information is used by 'cifuzz'.
 */
abstract class BuildDirectoryPrinter : DefaultTask() {

    @get:Input
    abstract val buildDirectory: Property<String>

    @TaskAction
    fun print() {
        println("cifuzz.buildDir=${buildDirectory.get()}")
    }

}
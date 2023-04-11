package com.code_intelligence.cifuzz.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

/**
 * Prints the packages of all production code on the given classpath.
 * This information is used by 'cifuzz'.
 */
abstract class PackagesPrinter : DefaultTask() {

    @get:InputFiles
    abstract val runtimeClasspath: ConfigurableFileCollection

    @TaskAction
    fun print() {
        val packages = sortedSetOf<String>()
        runtimeClasspath.filter { it.isDirectory }.forEach { classesFolder ->
            classesFolder.walk().filter { it.extension == "class" }.forEach { classFile ->
                packages.add(classFile.parentFile.relativeTo(classesFolder).toPath().joinToString("."))
            }
        }
        println("cifuzz.packages=${packages.joinToString(",")}")
    }

}
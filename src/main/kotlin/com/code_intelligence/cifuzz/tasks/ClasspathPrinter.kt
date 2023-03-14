package com.code_intelligence.cifuzz.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

abstract class ClasspathPrinter : DefaultTask() {

    @get:InputFiles
    abstract val runtimeClasspath: ConfigurableFileCollection

    @TaskAction
    fun print() {
        println("cifuzz.test.classpath=${runtimeClasspath.asPath}")
    }

}
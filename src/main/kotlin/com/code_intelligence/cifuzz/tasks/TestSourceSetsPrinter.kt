package com.code_intelligence.cifuzz.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

/**
 * prints all the source test set paths to handle
 * custom source sets inside cifuzz
 */
abstract class TestSourceSetsPrinter : DefaultTask() {

    @get:InputFiles
    abstract val testSourceSets: ConfigurableFileCollection
    
    @TaskAction
    fun print(){
        println("cifuzz.test.source-folders=${testSourceSets.asPath}")
    }
}
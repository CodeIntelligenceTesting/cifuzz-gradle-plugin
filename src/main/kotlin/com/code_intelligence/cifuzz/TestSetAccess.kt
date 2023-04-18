package com.code_intelligence.cifuzz

import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider

interface TestSetAccess {
    val testRuntimeClasspath: FileCollection
    val testImplementationConfigurationName: String
    val testRuntimeOnlyConfigurationName: String
    val testRuntimeClasspathConfigurationName: String
    val mainClasses: FileCollection
    val mainSources: FileCollection
    val testTask: Provider<out Task>
}
package com.code_intelligence.cifuzz

import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider

class StandardJvmTestSetAccess(
    private val ciFuzz: CIFuzzExtension
) : TestSetAccess {

    override val testRuntimeClasspath: FileCollection
        get() = ciFuzz.testSourceSet.get().runtimeClasspath

    override val testImplementationConfigurationName: String
        get() = ciFuzz.testSourceSet.get().implementationConfigurationName

    override val testRuntimeOnlyConfigurationName: String
        get() = ciFuzz.testSourceSet.get().runtimeOnlyConfigurationName

    override val testRuntimeClasspathConfigurationName: String
        get() = ciFuzz.testSourceSet.get().runtimeClasspathConfigurationName

    override val mainClasses: FileCollection
        get() = ciFuzz.mainSourceSet.get().output

    override val mainSources: FileCollection
        get() = ciFuzz.mainSourceSet.get().allSource.sourceDirectories

    override val testTask: Provider<out Task>
        get() = ciFuzz.testTask

    override val testSources: FileCollection
        get() = ciFuzz.testSourceSet.get().allSource.sourceDirectories
}
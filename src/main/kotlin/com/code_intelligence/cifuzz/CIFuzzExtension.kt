package com.code_intelligence.cifuzz

import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test

interface CIFuzzExtension {
    val testSourceSet: Property<SourceSet>
    val testTask: Property<TaskProvider<Test>>
}

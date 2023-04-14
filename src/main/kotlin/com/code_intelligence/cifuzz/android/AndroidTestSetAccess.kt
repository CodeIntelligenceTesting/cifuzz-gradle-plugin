package com.code_intelligence.cifuzz.android

import com.android.build.api.variant.TestComponent
import com.android.build.api.variant.UnitTest
import com.android.build.api.variant.Variant
import com.code_intelligence.cifuzz.TestSetAccess
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.configurationcache.extensions.capitalized

class AndroidTestSetAccess(
    private val project: Project,
    private val selectedVariant: Variant,
    private val testType: Class<out TestComponent>
) : TestSetAccess {

    private fun testComponent() = selectedVariant.nestedComponents.find { testType.isAssignableFrom(it.javaClass) }!! // TODO give proper error if tests of given type do not exist

    private fun testConfigurationPrefix() = if (testType == UnitTest::class.java) "test" else "androidTest"

    private fun testTaskPrefix() = if (testType == UnitTest::class.java) "test" else "connected"

    override val testRuntimeClasspath: FileCollection
        get() = (testComponent().compileClasspath + testComponent().runtimeConfiguration)

    override val testImplementationConfigurationName: String
        get() = "${testConfigurationPrefix()}Implementation"

    override val testRuntimeOnlyConfigurationName: String
        get() = "${testConfigurationPrefix()}RuntimeOnly"

    override val testRuntimeClasspathConfigurationName: String
        get() = testComponent().runtimeConfiguration.name

    override val mainClasses: FileCollection
        get() = project.objects.fileCollection() // the classes are part of the 'testRuntimeClasspathConfiguration' already

    override val mainSources: FileCollection
        get() = project.objects.fileCollection().from(selectedVariant.sources.java?.all, selectedVariant.sources.kotlin?.all)

    override val testTask: Provider<Test>
        get() = project.provider { project.tasks.named("${testTaskPrefix()}${testComponent().name.capitalized()}", Test::class.java) }.flatMap { it }
}
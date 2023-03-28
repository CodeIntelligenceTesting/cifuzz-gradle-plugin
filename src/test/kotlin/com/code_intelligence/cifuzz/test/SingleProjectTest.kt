package com.code_intelligence.cifuzz.test

import com.code_intelligence.cifuzz.test.fixture.CIFuzzPluginTest
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SingleProjectTest : CIFuzzPluginTest() {

    override fun example() = "single-project"

    @Test
    fun `cifuzzPrintPluginVersion task can be called`() {
        val result = runner("cifuzzPrintPluginVersion", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":cifuzzPrintPluginVersion")?.outcome)
        // assertThat(result.output, containsString("cifuzz.plugin.version=")) - no version in testkit mode
    }

    @Test
    fun `cifuzzPrintBuildDir task can be called`() {
        val result = runner("cifuzzPrintBuildDir", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":cifuzzPrintBuildDir")?.outcome)
        assertThat(result.output, containsString("cifuzz.buildDir="))
        assertThat(result.output, containsString("/build\n"))
    }

    @Test
    fun `cifuzzPrintTestClasspath task can be called`() {
        val result = runner("cifuzzPrintTestClasspath", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":cifuzzPrintTestClasspath")?.outcome)
        assertThat(result.output, containsString("build/classes/java/test"))
        assertThat(result.output, containsString("cifuzz.test.classpath="))
    }
}

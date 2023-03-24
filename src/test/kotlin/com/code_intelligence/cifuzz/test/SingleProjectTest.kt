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
    fun `printBuildDir task can be called`() {
        val result = runner("printBuildDir", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":printBuildDir")?.outcome)
        assertThat(result.output, containsString("cifuzz.test.buildDir="))
        assertThat(result.output, containsString("/build\n"))
    }

    @Test
    fun `printClasspath task can be called`() {
        val result = runner("printClasspath", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":printClasspath")?.outcome)
        assertThat(result.output, containsString("build/classes/java/test"))
        assertThat(result.output, containsString("cifuzz.test.classpath="))
    }
}

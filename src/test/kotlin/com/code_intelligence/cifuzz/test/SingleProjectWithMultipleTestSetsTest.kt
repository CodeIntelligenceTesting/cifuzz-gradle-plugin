package com.code_intelligence.cifuzz.test

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.lang.management.ManagementFactory

open class SingleProjectWithMultipleTestSetsTest {

    @TempDir
    private lateinit var projectDir: File

    @BeforeEach
    fun setup() {
        File("examples/single-project-multiple-test-sets").copyRecursively(projectDir)
    }

    @Test
    fun `printClasspath task can be called with cifuzz-fuzztest-path`() {
        val pathToFuzzTest = "src/integrationTest/java/org/example/integtest/ExampleIntegTest.java"
        val result = runner("printClasspath", "-q", "-Pcifuzz.fuzztest.path=${pathToFuzzTest}").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":printClasspath")?.outcome)
        assertThat(result.output, containsString("build/classes/java/integrationTest"))
        assertThat(result.output, containsString("jazzer-junit"))
    }

    private fun runner(vararg args: String): GradleRunner {
        return GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments(args.toList())
            .withDebug(ManagementFactory.getRuntimeMXBean().inputArguments.toString().contains("-agentlib:jdwp"))
    }
}

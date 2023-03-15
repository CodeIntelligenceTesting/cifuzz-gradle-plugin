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

open class SingleProjectTest {

    @TempDir
    private lateinit var projectDir: File

    @BeforeEach
    fun setup() {
        File("examples/single-project").copyRecursively(projectDir)
    }

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

    private fun runner(vararg args: String): GradleRunner {
        val gradleVersionUnderTest: String? = System.getProperty("gradleVersionUnderTest")
        return GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments(args.toList())
            .withDebug(ManagementFactory.getRuntimeMXBean().inputArguments.toString().contains("-agentlib:jdwp")).also {
                if (gradleVersionUnderTest != null) it.withGradleVersion(gradleVersionUnderTest)
            }
    }
}

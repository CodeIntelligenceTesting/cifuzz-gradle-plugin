package com.code_intelligence.cifuzz.test

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Files

open class CIFuzzPluginTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup() {
        projectDir = Files.createTempDirectory("gradle-build").toFile()
        val settingsFile = File(projectDir, "settingsFile.gradle")
        val buildFile = File(projectDir, "build.gradle")
        settingsFile.writeText("")
        buildFile.writeText("""
            plugins {
                id("com.code-intelligence.cifuzz")
                id("java-library")
            }
        """.trimIndent())
    }

    @Test
    fun `printClasspath task can be called`() {
        val result = runner(listOf(":printClasspath")).build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":printClasspath")?.outcome)
        assertTrue(result.output.contains("cifuzz.test.classpath="))
    }

    @Test
    fun `printBuildDir task can be called`() {
        val result = runner(listOf(":printBuildDir")).build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":printBuildDir")?.outcome)
        assertTrue(result.output.contains("cifuzz.test.buildDir="))
    }

    private fun runner(args: List<String>): GradleRunner {
        return GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments(args)
            .withDebug(ManagementFactory.getRuntimeMXBean().inputArguments.toString().contains("-agentlib:jdwp"))
    }
}

package com.code_intelligence.cifuzz.test

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.lang.management.ManagementFactory

open class MultiProjectTest {

    @TempDir
    private lateinit var projectDir: File

    @BeforeEach
    fun setup() {
        File("examples/multi-project").copyRecursively(projectDir)
        File(projectDir, "settings.gradle.kts").let {
            it.writeText(it.readText().replace("""pluginManagement { includeBuild("../..") }""", ""))
        }
    }

    @Test
    fun `printBuildDir task can be called`() {
        val result = runner("printBuildDir", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":module-c:printBuildDir")?.outcome)
        assertThat(result.output, containsString("cifuzz.test.buildDir="))
        assertThat(result.output, containsString("/module-c/build\n"))
    }

    @Test
    fun `printClasspath task can be called`() {
        val result = runner("printClasspath", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":module-c:printClasspath")?.outcome)
        assertThat(result.output, containsString("build/classes/java/test"))
        assertThat(result.output, containsString("cifuzz.test.classpath="))
    }

    @Test
    fun `cifuzzReport produces xml coverage report`() {
        val reportFile = File(projectDir, "module-c/build/reports/jacoco/cifuzzReport/cifuzzReport.xml")

        val result = runner("cifuzzReport", "-Pcifuzz.fuzztest=org.example.c.test.ModuleCTest.testC").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":module-c:cifuzzReport")?.outcome)
        assertTrue(reportFile.exists())
        assertThat(reportFile.readText(), containsString("""<class name="org/example/a/ModuleA" sourcefilename="ModuleA.java">"""))
        assertThat(reportFile.readText(), containsString("""<class name="org/example/b/ModuleB" sourcefilename="ModuleB.java">"""))
        assertThat(reportFile.readText(), containsString("""<class name="org/example/c/ModuleC" sourcefilename="ModuleC.java">"""))
    }

    private fun runner(vararg args: String): GradleRunner {
        val gradleVersionUnderTest: String? = System.getProperty("gradleVersionUnderTest")
        return GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(File(projectDir, "module-c")) // location of 'cifuzz.yaml'
            .withArguments(args.toList() + listOf("-s"))
            .withDebug(ManagementFactory.getRuntimeMXBean().inputArguments.toString().contains("-agentlib:jdwp")).also {
                if (gradleVersionUnderTest != null) it.withGradleVersion(gradleVersionUnderTest)
            }
    }
}

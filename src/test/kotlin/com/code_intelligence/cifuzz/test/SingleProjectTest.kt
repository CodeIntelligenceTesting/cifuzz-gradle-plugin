package com.code_intelligence.cifuzz.test

import com.code_intelligence.cifuzz.test.fixture.CIFuzzPluginTest
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

abstract class SingleProjectTest : CIFuzzPluginTest() {

    abstract fun jvmLanguageFolder() : String

    open fun jvmLanguageFileExt() = jvmLanguageFolder()

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
        assertThat(result.output, containsString("build/classes/${jvmLanguageFolder()}/test"))
        assertThat(result.output, containsString("cifuzz.test.classpath="))
    }

    @Test
    fun `cifuzzReport executes cifuzz test tasks and produces xml coverage report`() {
        val reportFile = File(projectDir, "build/reports/jacoco/cifuzzReport/cifuzzReport.xml")

        val result = runner("cifuzzReport", "-Pcifuzz.fuzztest=org.example.test.ExampleTest.fuzzTest").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":test")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":cifuzzReport")?.outcome)

        Assertions.assertTrue(reportFile.exists())
        assertThat(reportFile.readText(), containsString("""<class name="org/example/ExampleLib" sourcefilename="ExampleLib.${jvmLanguageFileExt()}">"""))
    }
}

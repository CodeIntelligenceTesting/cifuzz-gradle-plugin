package com.code_intelligence.cifuzz.test

import com.code_intelligence.cifuzz.test.fixture.CIFuzzPluginTest
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class MultiProjectTest : CIFuzzPluginTest() {

    override fun example() = "multi-project"

    override fun cifuzzProjectDir() = File(projectDir, "module-c")

    @Test
    fun `cifuzzPrintBuildDir task can be called`() {
        val result = runner("cifuzzPrintBuildDir", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":module-c:cifuzzPrintBuildDir")?.outcome)
        assertThat(result.output, containsString("cifuzz.buildDir="))
        assertThat(result.output, containsString("/module-c/build\n"))
    }

    @Test
    fun `cifuzzPrintTestClasspath task can be called`() {
        val result = runner("cifuzzPrintTestClasspath", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":module-c:cifuzzPrintTestClasspath")?.outcome)
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
}

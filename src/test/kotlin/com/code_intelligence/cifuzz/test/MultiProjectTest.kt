package com.code_intelligence.cifuzz.test

import com.code_intelligence.cifuzz.test.fixture.CIFuzzPluginTest
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths

class MultiProjectTest : CIFuzzPluginTest() {

    override fun example() = "multi-project"

    override fun cifuzzProjectDir() = File(projectDir, "module-c")

    @Test
    fun `cifuzzPrintPluginVersion task can be called`() {
        val result = runner("cifuzzPrintPluginVersion", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":module-c:cifuzzPrintPluginVersion")?.outcome)
        // assertThat(result.output, containsString("cifuzz.plugin.version=")) - no version in testkit mode
    }

    @Test
    fun `cifuzzPrintBuildDir task can be called`() {
        val result = runner("cifuzzPrintBuildDir", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":module-c:cifuzzPrintBuildDir")?.outcome)
        assertThat(result.output, containsString("cifuzz.buildDir="))
        assertThat(result.output, containsString(Paths.get("module-c", "build").toString()))
    }

    @Test
    fun `cifuzzPrintTestClasspath task can be called`() {
        val result = runner("cifuzzPrintTestClasspath", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":module-c:cifuzzPrintTestClasspath")?.outcome)
        assertThat(result.output, containsString(Paths.get("build", "classes", "java", "test").toString()))
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

    @Test
    fun `cifuzzReport does not execute normal unit tests`() {
        val reportFile = File(projectDir, "module-c/build/reports/tests/test/classes/org.example.c.test.ModuleCTest.html")

        // ModuleCTest contains a @FuzzTest and a @Test
        val result = runner("cifuzzReport", "-Pcifuzz.fuzztest=org.example.c.test.ModuleCTest").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":module-c:cifuzzReport")?.outcome)
        assertTrue(reportFile.exists())
        assertThat(reportFile.readText(), containsString(""">fuzzTest(byte[])[1]<"""))
        assertThat(reportFile.readText(), not(containsString(""">unitTest()<""")))
    }
}

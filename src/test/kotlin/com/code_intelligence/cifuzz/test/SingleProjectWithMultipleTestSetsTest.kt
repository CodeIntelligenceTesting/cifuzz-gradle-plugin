package com.code_intelligence.cifuzz.test

import com.code_intelligence.cifuzz.test.fixture.CIFuzzPluginTest
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths

class SingleProjectWithMultipleTestSetsTest : CIFuzzPluginTest() {

    override fun example() = "single-project-multiple-test-sets"

    @Test
    fun `cifuzzPrintTestClasspath task can be called for a test in another source set`() {
        val result = runner("cifuzzPrintTestClasspath", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":cifuzzPrintTestClasspath")?.outcome)
        assertThat(result.output, containsString(Paths.get("build", "classes", "java", "fuzzTest").toString()))
        assertThat(result.output, containsString("jazzer-junit"))
    }

    @Test
    fun `cifuzzReport executes cifuzz test tasks and produces xml coverage report`() {
        val reportFile = File(projectDir, "build/reports/jacoco/cifuzzReport/cifuzzReport.xml")

        val result = runner("cifuzzReport", "-Pcifuzz.fuzztest=org.example.fuzztest.ExampleFuzzTest.testB").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":fuzzTest")?.outcome)

        Assertions.assertTrue(reportFile.exists())
        assertThat(reportFile.readText(), containsString("""<class name="org/example/ExampleLib" sourcefilename="ExampleLib.java">"""))
        assertThat(reportFile.readText(), containsString("""<class name="org/example/ExampleApp" sourcefilename="ExampleApp.java">"""))
    }

    @Test
    fun `cifuzzPrintTestSourceFolders returns custom test source sets`() {
        val result = runner("cifuzzPrintTestSourceFolders", "-q").build()
        assertThat(result.output, containsString("src${File.separator}fuzzTest"))
        assertThat(result.output, containsString("src${File.separator}fuzzTest"))
        assertThat(result.output, containsString("cifuzz.test.source-folders="))
    }
}

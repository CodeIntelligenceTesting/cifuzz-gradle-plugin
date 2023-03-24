package com.code_intelligence.cifuzz.test

import com.code_intelligence.cifuzz.test.fixture.CIFuzzPluginTest
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class SingleProjectWithMultipleTestSetsTest : CIFuzzPluginTest() {

    override fun example() = "single-project-multiple-test-sets"

    @Test
    fun `printClasspath task can be called with cifuzz-fuzztest-path`() {
        val result = runner("printClasspath", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":printClasspath")?.outcome)
        assertThat(result.output, containsString("build/classes/java/fuzzTest"))
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
}

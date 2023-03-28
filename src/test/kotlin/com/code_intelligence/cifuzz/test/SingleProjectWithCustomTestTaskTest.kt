package com.code_intelligence.cifuzz.test

import com.code_intelligence.cifuzz.test.fixture.CIFuzzPluginTest
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class SingleProjectWithCustomTestTaskTest : CIFuzzPluginTest() {

    override fun example() = "single-project-custom-test-task"

    @Test
    fun `cifuzzReport executes cifuzz test tasks and produces xml coverage report`() {
        val reportFile = File(projectDir, "build/reports/jacoco/cifuzzReport/cifuzzReport.xml")

        val result = runner("cifuzzReport", "-Pcifuzz.fuzztest=org.example.fuzztest.ExampleFuzzTest.testB").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":specialTestTask")?.outcome)

        Assertions.assertTrue(reportFile.exists())
        assertThat(reportFile.readText(), containsString("""<class name="org/example/ExampleLib" sourcefilename="ExampleLib.java">"""))
        assertThat(reportFile.readText(), containsString("""<class name="org/example/ExampleApp" sourcefilename="ExampleApp.java">"""))
    }
}

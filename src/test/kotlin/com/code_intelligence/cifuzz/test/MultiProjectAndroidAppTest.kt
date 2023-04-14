package com.code_intelligence.cifuzz.test

import com.code_intelligence.cifuzz.test.fixture.CIFuzzPluginTest
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File

@Tag("android")
class MultiProjectAndroidAppTest : CIFuzzPluginTest() {

    override fun example() = "multi-project-android-app"

    override fun cifuzzProjectDir() = File(projectDir, "app")

    @Test
    fun `cifuzzPrintBuildDir task can be called`() {
        val result = runner("cifuzzPrintBuildDir", "-q").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":app:cifuzzPrintBuildDir")?.outcome)
        assertThat(result.output, containsString("cifuzz.buildDir="))
        assertThat(result.output, containsString("/app/build\n"))
    }

    @Test
    fun `cifuzzPrintTestClasspath task can be called`() {
        val result = runner("cifuzzPrintTestClasspath", "-a").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":app:cifuzzPrintTestClasspath")?.outcome)
        assertThat(result.output, containsString("app/build/tmp/kotlin-classes/releaseUnitTest"))
        assertThat(result.output, containsString("cifuzz.test.classpath="))
    }

    @Test
    fun `cifuzzReport produces xml coverage report`() {
        val reportFile = File(projectDir, "app/build/reports/jacoco/cifuzzReport/cifuzzReport.xml")

        val result = runner("cifuzzReport", "-Pcifuzz.fuzztest=org.example.ExampleUnitTest.fuzzTest").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":app:cifuzzReport")?.outcome)
        assertTrue(reportFile.exists())
        assertThat(reportFile.readText(), containsString("""<class name="org/example/MainFeature" sourcefilename="MainFeature.kt">"""))
        assertThat(reportFile.readText(), containsString("""<class name="org/example/MainActivity" sourcefilename="MainActivity.kt">"""))
    }

    @Test
    fun `cifuzzReport does not execute normal unit tests`() {
        val reportFile = File(projectDir, "app/build/reports/tests/testReleaseUnitTest/classes/org.example.ExampleUnitTest.html")

        // ModuleCTest contains a @FuzzTest and a @Test
        val result = runner("cifuzzReport", "-Pcifuzz.fuzztest=org.example.ExampleUnitTest").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":app:cifuzzReport")?.outcome)
        assertTrue(reportFile.exists())
        assertThat(reportFile.readText(), containsString(""">fuzzTest(byte[])[1]<"""))
        assertThat(reportFile.readText(), not(containsString(""">unitTest()<""")))
    }
}

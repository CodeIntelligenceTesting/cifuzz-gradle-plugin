package com.code_intelligence.cifuzz.test.fixture

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.lang.management.ManagementFactory

abstract class CIFuzzPluginTest {

    @TempDir
    lateinit var projectDir: File

    abstract fun example() : String

    open fun cifuzzProjectDir() = projectDir  // location of 'cifuzz.yaml'

    @BeforeEach
    fun setup() {
        File("examples/${example()}").copyRecursively(projectDir)
        File(projectDir, "settings.gradle.kts").apply {
            writeText(readText().replace("""includeBuild("../..")""", """repositories.maven("${File("build/pluginUnderTestRepo").absolutePath}")"""))
        }
        runner("clean")
    }

    fun runner(vararg args: String): GradleRunner {
        val gradleVersionUnderTest: String? = System.getProperty("gradleVersionUnderTest")
        return GradleRunner.create()
            .forwardOutput()
            .withProjectDir(cifuzzProjectDir())
            .withArguments(args.toList() + listOf("-s"))
            .withDebug(ManagementFactory.getRuntimeMXBean().inputArguments.toString().contains("-agentlib:jdwp")).also {
                if (gradleVersionUnderTest != null) it.withGradleVersion(gradleVersionUnderTest)
            }
    }
}
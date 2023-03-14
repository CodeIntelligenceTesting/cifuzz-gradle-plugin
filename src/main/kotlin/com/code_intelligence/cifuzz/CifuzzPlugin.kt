package com.code_intelligence.cifuzz

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

abstract class CIFuzzPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withId("java") {
            configureCIFuzz(project)
        }
    }

    private fun configureCIFuzz(project: Project) {
        project.plugins.apply("jacoco")

        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)

        project.tasks.register("printClasspath") {
            it.doLast {
                println("cifuzz.test.classpath=${sourceSets.getByName("test").runtimeClasspath.asPath}")
            }
        }

        project.tasks.register("printBuildDir") {
            it.doLast {
                println("cifuzz.test.buildDir=${project.layout.buildDirectory.get()}")
            }
        }

        val cifuzzTest = project.tasks.register("cifuzzTest", Test::class.java) { cifuzzTest ->
            cifuzzTest.useJUnitPlatform()
            cifuzzTest.ignoreFailures = true
            // disable jazzer hooks because they are not needed for coverage runs
            cifuzzTest.jvmArgs("-Djazzer.hooks=false")

            val jacoco = cifuzzTest.extensions.getByType(JacocoTaskExtension::class.java)
            jacoco.setDestinationFile(project.layout.buildDirectory.file("jacoco/cifuzz.exec").map { it.asFile })

            cifuzzTest.filter { filter ->
                val fuzzTest = project.providers.gradleProperty("cifuzz.fuzztest").getOrElse("*")
                filter.includeTestsMatching(fuzzTest)
            }
        }

        // we register own tasks for the report to avoid side effects on existing user tasks
        // when overwriting config values (like output path)

        // we need to set the exec file output path explicitly to make sure we find
        // the file in the `cifuzzReport` tasks
        project.tasks.register("cifuzzReport", JacocoReport::class.java) { cifuzzReport ->
            cifuzzReport.executionData(cifuzzTest.map { cifuzzTest ->
                val jacoco = cifuzzTest.extensions.getByType(JacocoTaskExtension::class.java)
                jacoco.destinationFile!!
            })
            cifuzzReport.classDirectories.from(project.files(sourceSets.getByName("main").output))
            cifuzzReport.sourceDirectories.from(project.files(sourceSets.getByName("main").java.srcDirs))

            cifuzzReport.reports { reports ->
                val output = project.providers.gradleProperty("cifuzz.report.output").orElse(
                    project.layout.buildDirectory.dir("reports/cifuzz").map { it.asFile.absolutePath }
                )
                val format = project.providers.gradleProperty("cifuzz.report.format").getOrElse("html")

                reports.html.required.set(format != "jacocoxml")
                reports.html.outputLocation.set(output.map { project.layout.projectDirectory.dir("$it/html") })
                reports.xml.required.set(true)
                reports.xml.outputLocation.set(output.map { project.layout.projectDirectory.file("$it/jacoco.xml") })
            }
        }
    }
}
package com.code_intelligence.cifuzz

import com.code_intelligence.cifuzz.tasks.BuildDirectoryPrinter
import com.code_intelligence.cifuzz.tasks.ClasspathPrinter
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
        val fuzzTestProperty = project.providers.gradleProperty("cifuzz.fuzztest")

        registerPrintBuildDir(project)
        registerPrintClasspath(project)

        if (fuzzTestProperty.isPresent) {
            configureAllTestTasks(project, fuzzTestProperty.get())
        }

        // we register own tasks for the report to avoid side effects on existing user tasks
        // when overwriting config values (like output path)

        // we need to set the exec file output path explicitly to make sure we find
        // the file in the `cifuzzReport` tasks
        project.tasks.register("cifuzzReport", JacocoReport::class.java) { cifuzzReport ->
            val allTestTasks = project.tasks.withType(Test::class.java)
            cifuzzReport.dependsOn(allTestTasks)
            cifuzzReport.executionData(allTestTasks.map { testTask ->
                testTask.extensions.getByType(JacocoTaskExtension::class.java).destinationFile!!
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

    private fun configureAllTestTasks(project: Project, fuzzTestFilter: String) {
        project.tasks.withType(Test::class.java).configureEach { testTask ->
            testTask.ignoreFailures = true
            testTask.jvmArgs("-Djazzer.hooks=false") // disable jazzer hooks as they are not needed for coverage runs
            testTask.filter { filter -> filter.includeTestsMatching(fuzzTestFilter) }
        }
    }

    private fun registerPrintBuildDir(project: Project) {
        project.tasks.register("printBuildDir", BuildDirectoryPrinter::class.java) { printBuildDir ->
            printBuildDir.buildDirectory.set(project.layout.buildDirectory.map { it.asFile.absolutePath })
        }
    }

    private fun registerPrintClasspath(project: Project) {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        project.tasks.register("printClasspath", ClasspathPrinter::class.java) { printClasspath ->
            // TODO which classpath do we use if there are multiple test sets?
            printClasspath.testRuntimeClasspath.from(sourceSets.getByName("test").runtimeClasspath)
        }
    }
}
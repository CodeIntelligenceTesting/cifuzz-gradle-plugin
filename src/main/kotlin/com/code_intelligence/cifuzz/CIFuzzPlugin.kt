package com.code_intelligence.cifuzz

import com.code_intelligence.cifuzz.tasks.BuildDirectoryPrinter
import com.code_intelligence.cifuzz.tasks.ClasspathPrinter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.util.GradleVersion
import java.io.File

@Suppress("unused")
abstract class CIFuzzPlugin : Plugin<Project> {

    private val isGradleVersionWithTestSuitesSupport = GradleVersion.current() >= GradleVersion.version("7.4")

    override fun apply(project: Project) {
        project.plugins.withId("java") {
            configureCIFuzz(project)
        }
    }

    private fun configureCIFuzz(project: Project) {
        val fuzzTestProperty = gradleProperty(project, "cifuzz.fuzztest")

        registerPrintBuildDir(project)
        registerPrintClasspath(project)

        if (fuzzTestProperty != null) {
            configureAllTestTasks(project, fuzzTestProperty)
        }

        // We register own tasks for the report to avoid side effects on existing user tasks when overwriting config
        // values (like the output path).
        if (isGradleVersionWithTestSuitesSupport) {
            registerCoverageReportingTask(project)
        } else {
            registerCoverageReportingTaskLegacy(project)
        }
    }

    private fun configureAllTestTasks(project: Project, fuzzTestFilter: String) {
        project.tasks.withType(Test::class.java).configureEach { testTask ->
            testTask.ignoreFailures = true
            testTask.jvmArgs("-Djazzer.hooks=false") // disable jazzer hooks as they are not needed for coverage runs

            testTask.filter { filter ->
                filter.includeTestsMatching(fuzzTestFilter)
                filter.isFailOnNoMatchingTests = false
            }
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
            val fuzzTestPath = gradleProperty(project, "cifuzz.fuzztest.path")
            val sourceSet = if (fuzzTestPath != null) {
                sourceSets.find {
                    File(fuzzTestPath).startsWith(it.java.srcDirs.first().relativeTo(project.projectDir))
                } ?: sourceSets.getByName("test")
            } else {
                sourceSets.getByName("test")
            }
            printClasspath.testRuntimeClasspath.from(sourceSet.runtimeClasspath)
        }
    }

    private fun registerCoverageReportingTask(project: Project) {
        project.plugins.apply("jacoco-report-aggregation")

        val reporting = project.extensions.getByType(ReportingExtension::class.java)

        reporting.reports.register("cifuzzReport", JacocoCoverageReport::class.java) { report ->
            report.testType.convention("undefined") // Gradle 7.x requires this to not fail test classpath resolution
            configureJacocoReportTask(report.reportTask, project)
        }
    }

    private fun registerCoverageReportingTaskLegacy(project: Project) {
        project.plugins.apply("jacoco")

        val main = project.extensions.getByType(SourceSetContainer::class.java).getByName("main")

        val reportTask = project.tasks.register("cifuzzReport", JacocoReport::class.java) { cifuzzReport ->
            val classesFolders =
                project.configurations.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME).incoming.artifactView { view ->
                    view.componentFilter { id -> id is ProjectComponentIdentifier }
                    view.attributes.attribute(
                        LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(
                            LibraryElements::class.java, LibraryElements.CLASSES
                        )
                    )
                }.files

            cifuzzReport.classDirectories.from(main.output)
            cifuzzReport.sourceDirectories.from(main.java.srcDirs)

            cifuzzReport.classDirectories.from(classesFolders)
            cifuzzReport.sourceDirectories.from(classesFolders.elements.map { classFolder ->
                classFolder.map { File(it.asFile, "../../../../src/main/java") }
            })
        }
        configureJacocoReportTask(reportTask, project)
    }

    private fun configureJacocoReportTask(reportTask: TaskProvider<JacocoReport>, project: Project) {
        reportTask.configure { cifuzzReport ->
            val allTestTasks = project.tasks.withType(Test::class.java)
            cifuzzReport.dependsOn(allTestTasks)
            cifuzzReport.executionData.setFrom(project.files(allTestTasks.map { testTask ->
                testTask.extensions.getByType(JacocoTaskExtension::class.java).destinationFile!!
            }).filter { it.exists() })

            cifuzzReport.reports { reports ->
                val format = gradleProperty(project, "cifuzz.report.format") ?: "html"
                reports.html.required.set(format != "jacocoxml")
                reports.xml.required.set(true)

                val output = gradleProperty(project, "cifuzz.report.output")
                if (output != null) {
                    reports.html.outputLocation.set(project.layout.projectDirectory.dir("$output/html"))
                    reports.xml.outputLocation.set(project.layout.projectDirectory.file("$output/jacoco.xml"))
                }
            }
        }
    }

    private fun gradleProperty(project: Project, name: String): String? = if (isGradleVersionWithTestSuitesSupport) {
        project.providers.gradleProperty(name).orNull
    } else {
        project.findProperty(name) as String?
    }
}
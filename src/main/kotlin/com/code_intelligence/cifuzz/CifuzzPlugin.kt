package com.code_intelligence.cifuzz

import com.code_intelligence.cifuzz.tasks.BuildDirectoryPrinter
import com.code_intelligence.cifuzz.tasks.ClasspathPrinter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.TestSuiteType
import org.gradle.api.attributes.VerificationType
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.base.TestingExtension
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport
import java.io.File

abstract class CIFuzzPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withId("java") {
            configureCIFuzz(project)
        }
    }

    private fun configureCIFuzz(project: Project) {
        project.plugins.apply("jacoco-report-aggregation")

        val fuzzTestProperty = project.providers.gradleProperty("cifuzz.fuzztest")

        registerPrintBuildDir(project)
        registerPrintClasspath(project)

        if (fuzzTestProperty.isPresent) {
            configureAllTestTasks(project, fuzzTestProperty.get())
        }

        // We register own tasks for the report to avoid side effects on existing user tasks when overwriting config
        // values (like the output path).
        registerCoverageReportingTask(project)
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
            val fuzzTestPath = project.providers.gradleProperty("cifuzz.fuzztest.path")
            val sourceSet = if (fuzzTestPath.isPresent) {
                sourceSets.find {
                    File(fuzzTestPath.get()).startsWith(it.java.srcDirs.first().relativeTo(project.projectDir))
                } ?: sourceSets.getByName("test")
            } else {
                sourceSets.getByName("test")
            }
            printClasspath.testRuntimeClasspath.from(sourceSet.runtimeClasspath)
        }
    }

    private fun registerCoverageReportingTask(project: Project) {
        val objects = project.objects
        val testing = project.extensions.getByType(TestingExtension::class.java)
        val reporting = project.extensions.getByType(ReportingExtension::class.java)

        reporting.reports.register("cifuzzReport", JacocoCoverageReport::class.java) { report ->
            // project.tasks.register("cifuzzReport", JacocoReport::class.java) { cifuzzReport -> }
            report.reportTask.configure { cifuzzReport ->
                // TODO the below is Gradle 8 specific and uses low-level API - can we use 'cifuzz.fuzztest.path' as well and decide for only one 'testType'
                // report.testType.set(testType-from-cifuzz.fuzztest.path)
                val codeCoverageResults = project.configurations.getByName("aggregateCodeCoverageReportResults")
                cifuzzReport.executionData.setFrom(
                    testing.suites.withType(JvmTestSuite::class.java).map {
                        artifactsForTestType(codeCoverageResults, objects, it.testType)
                    }
                )

                cifuzzReport.reports { reports ->
                    val format = project.providers.gradleProperty("cifuzz.report.format").getOrElse("html")
                    reports.html.required.set(format != "jacocoxml")
                    reports.xml.required.set(true)

                    val output = project.providers.gradleProperty("cifuzz.report.output")
                    if (output.isPresent) {
                        reports.html.outputLocation.set(output.map { project.layout.projectDirectory.dir("$it/html") })
                        reports.xml.outputLocation.set(output.map { project.layout.projectDirectory.file("$it/jacoco.xml") })
                    }
                }
            }
        }
    }

    private fun artifactsForTestType(codeCoverageResults: Configuration, objects: ObjectFactory, testType: Provider<String>) =
        codeCoverageResults.incoming.artifactView { view ->
            view.withVariantReselection()
            view.componentFilter { id -> id is ProjectComponentIdentifier }
            view.attributes { attributes ->
                attributes.attribute(
                    Category.CATEGORY_ATTRIBUTE,
                    objects.named(Category::class.java, Category.VERIFICATION)
                )
                attributes.attributeProvider(
                    TestSuiteType.TEST_SUITE_TYPE_ATTRIBUTE,
                    testType.map { objects.named(TestSuiteType::class.java, it) }
                )
                attributes.attribute(
                    VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
                    objects.named(VerificationType::class.java, VerificationType.JACOCO_RESULTS)
                )
                attributes.attribute(
                    ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE,
                    ArtifactTypeDefinition.BINARY_DATA_TYPE
                )
            }
        }.files
}
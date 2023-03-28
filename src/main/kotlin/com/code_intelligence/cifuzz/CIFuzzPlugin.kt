package com.code_intelligence.cifuzz

import com.code_intelligence.cifuzz.tasks.BuildDirectoryPrinter
import com.code_intelligence.cifuzz.tasks.PluginVersionPrinter
import com.code_intelligence.cifuzz.tasks.ClasspathPrinter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.SourceSet
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

    private val isSupportedGradleVersion = GradleVersion.current() >= GradleVersion.version("6.1")
    private val isGradleVersionWithTestSuitesSupport = GradleVersion.current() >= GradleVersion.version("7.4")

    override fun apply(project: Project) {
        if (!isSupportedGradleVersion) {
            throw IllegalStateException("Plugin requires at least Gradle 6.1");
        }

        project.plugins.withId("java") {
            configureCIFuzz(project)
        }
    }

    private fun configureCIFuzz(project: Project) {
        val fuzzTestProperty = gradleProperty(project, "cifuzz.fuzztest")

        val cifuzz = project.extensions.create("cifuzz", CIFuzzExtension::class.java)
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)

        cifuzz.testSourceSet.convention(sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME))
        cifuzz.testTask.convention(cifuzz.testSourceSet.map { sourceSet ->
            project.tasks.named(sourceSet.name, Test::class.java)
        })

        registerPrintCIFuzzPluginVersion(project)
        registerPrintBuildDir(project)
        registerPrintClasspath(project, cifuzz.testSourceSet)

        addJazzerDependencies(project, cifuzz.testSourceSet)

        if (fuzzTestProperty != null) {
            configureTestTasks(project, cifuzz.testTask, fuzzTestProperty)
        }

        // We register own tasks for the report to avoid side effects on existing user tasks when overwriting config
        // values (like the output path).
        if (isGradleVersionWithTestSuitesSupport) {
            registerCoverageReportingTask(project, cifuzz.testTask)
        } else {
            registerCoverageReportingTaskLegacy(project, cifuzz.testTask)
        }
    }

    private fun addJazzerDependencies(project: Project, testSourceSet: Provider<SourceSet>) {
        project.tasks.named("test", Test::class.java) {
            if (testSourceSet.get().name == SourceSet.TEST_SOURCE_SET_NAME) {
                it.useJUnitPlatform()
            }
        }
        project.configurations.all { config ->
            config.withDependencies { dependencySet ->
                val sourceSet = testSourceSet.get()
                if (config.name == sourceSet.implementationConfigurationName) {
                    dependencySet.add(project.dependencies.create("com.code-intelligence:jazzer-junit:0.16.0"))
                }
                if (config.name == sourceSet.runtimeOnlyConfigurationName) {
                    dependencySet.add(project.dependencies.create("org.junit.jupiter:junit-jupiter-engine"))
                }
            }
        }
    }

    private fun configureTestTasks(project: Project, testTaskProvider: Provider<TaskProvider<Test>>, fuzzTestFilter: String) {
        project.tasks.withType(Test::class.java).configureEach { testTask ->
            if (testTask == testTaskProvider.get().get()) {
                testTask.ignoreFailures = true
                testTask.jvmArgs("-Djazzer.hooks=false") // disable jazzer hooks as they are not needed for coverage runs

                testTask.filter { filter ->
                    filter.includeTestsMatching(fuzzTestFilter)
                    filter.isFailOnNoMatchingTests = false
                }
            }
        }
    }

    private fun registerPrintCIFuzzPluginVersion(project: Project) {
        project.tasks.register("cifuzzPrintPluginVersion", PluginVersionPrinter::class.java)
    }

    private fun registerPrintBuildDir(project: Project) {
        project.tasks.register("cifuzzPrintBuildDir", BuildDirectoryPrinter::class.java) { printBuildDir ->
            printBuildDir.buildDirectory.set(project.layout.buildDirectory.map { it.asFile.absolutePath })
        }
    }

    private fun registerPrintClasspath(project: Project, testSourceSet: Provider<SourceSet>) {
        project.tasks.register("cifuzzPrintTestClasspath", ClasspathPrinter::class.java) { printClasspath ->
            printClasspath.testRuntimeClasspath.from(testSourceSet.get().runtimeClasspath)
        }
    }

    private fun registerCoverageReportingTask(project: Project, testTask: Provider<TaskProvider<Test>>) {
        project.plugins.apply("jacoco-report-aggregation")

        val reporting = project.extensions.getByType(ReportingExtension::class.java)

        reporting.reports.register("cifuzzReport", JacocoCoverageReport::class.java) { report ->
            report.testType.convention("undefined") // Gradle 7.x requires this to not fail test classpath resolution
            configureJacocoReportTask(project, report.reportTask, testTask)
        }
    }

    private fun registerCoverageReportingTaskLegacy(project: Project, testTask: Provider<TaskProvider<Test>>) {
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
        configureJacocoReportTask(project, reportTask, testTask)
    }

    private fun configureJacocoReportTask(project: Project, reportTask: TaskProvider<JacocoReport>, testTask: Provider<TaskProvider<Test>>) {
        reportTask.configure { cifuzzReport ->
            cifuzzReport.executionData.from(testTask.get().map { testTask ->
                testTask.extensions.getByType(JacocoTaskExtension::class.java).destinationFile!!
            })

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
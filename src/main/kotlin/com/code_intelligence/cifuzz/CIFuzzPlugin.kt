package com.code_intelligence.cifuzz

import com.code_intelligence.cifuzz.tasks.BuildDirectoryPrinter
import com.code_intelligence.cifuzz.tasks.PluginVersionPrinter
import com.code_intelligence.cifuzz.tasks.ClasspathPrinter
import com.code_intelligence.cifuzz.tasks.PackagesPrinter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.file.FileCollection
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
    private val jazzerVersion = "0.16.1"
    private val jazzerJUnit5TestTag = "jazzer"

    override fun apply(project: Project) {
        if (!isSupportedGradleVersion) {
            throw IllegalStateException("Plugin requires at least Gradle 6.1")
        }

        // The plugin relies on the 'java' plugin being applied first, which is implicitly applied by plugins like:
        // 'application', 'java-library', 'groovy', 'scala', 'kotlin("jvm")', ...
        project.plugins.withId("java") {
            project.configureCIFuzzPlugin()
        }
    }
    private fun Project.configureCIFuzzPlugin() {
        val fuzzTestProperty = gradleProperty("cifuzz.fuzztest")
        
        // Register extension for fine-tuning - can be used in 'build.gradle' files like this:
        //   cifuzz {
        //     testSourceSet.set(customFuzzTestSourceSet)
        //     testTask.set(customFuzzTestTask)
        //   }
        val cifuzz = registerCIFuzzExtension()
        
        // Register cifuzz help tasks
        registerPrintCIFuzzPluginVersion()
        registerPrintBuildDir()
        registerPrintClasspath(cifuzz.testSourceSet)
        registerPrintPackages(cifuzz.testSourceSet)

        // Automatically add dependencies to Jazzer
        addJazzerDependencies(cifuzz.testSourceSet)

        // The test task selected for fuzz testing is only (re)configured when started through 'cifuzz' for coverage
        // to keep normal Gradle test execution untouched.
        if (fuzzTestProperty != null) {
            reconfigureTestTasks(cifuzz.testTask, fuzzTestProperty)
        }

        // Register a custom JacocoReport task to avoid side effects on existing user tasks when overwriting config
        // values (like the output path).
        if (isGradleVersionWithTestSuitesSupport) {
            registerCoverageReportingTask(cifuzz.testTask)
        } else {
            registerCoverageReportingTaskLegacy(cifuzz.testTask, cifuzz.testSourceSet)
        }
    }

    private fun Project.registerCIFuzzExtension() = extensions.create("cifuzz", CIFuzzExtension::class.java).apply {
        val sourceSets = extensions.getByType(SourceSetContainer::class.java)
        
        testSourceSet.convention(sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME))
        testTask.convention(testSourceSet.flatMap { sourceSet -> tasks.named(sourceSet.name, Test::class.java) })
    }
    
    private fun Project.registerPrintCIFuzzPluginVersion() {
        tasks.register("cifuzzPrintPluginVersion", PluginVersionPrinter::class.java)
    }

    private fun Project.registerPrintBuildDir() {
        tasks.register("cifuzzPrintBuildDir", BuildDirectoryPrinter::class.java) { printBuildDir ->
            printBuildDir.buildDirectory.set(layout.buildDirectory.map { it.asFile.absolutePath })
        }
    }

    private fun Project.registerPrintClasspath(testSourceSet: Provider<SourceSet>) {
        tasks.register("cifuzzPrintTestClasspath", ClasspathPrinter::class.java) { printClasspath ->
            printClasspath.testRuntimeClasspath.from(testSourceSet.get().runtimeClasspath)
        }
    }

    private fun Project.registerPrintPackages(testSourceSet: Provider<SourceSet>) {
        tasks.register("cifuzzPrintPackages", PackagesPrinter::class.java) { printClasspath ->
            printClasspath.testRuntimeClasspath.from(mainSourceSet().output)
            printClasspath.testRuntimeClasspath.from(classesFolderView(testSourceSet))
        }
    }

    private fun Project.addJazzerDependencies(testSourceSet: Provider<SourceSet>) {
        configurations.all { configuration ->
            // Use 'withDependencies { }' to access 'testSourceSet.get()' at the latest point possible.
            configuration.withDependencies { dependencySet ->
                val sourceSet = testSourceSet.get()
                if (configuration.name == sourceSet.implementationConfigurationName) {
                    // To write fuzz tests, add 'jazzer-junit' to 'implementation' scope of the selected source set.
                    // Jazzer brings in the JUnit5 API with a defined version.
                    dependencySet.add(dependencies.create("com.code-intelligence:jazzer-junit:$jazzerVersion"))
                }
                if (configuration.name == sourceSet.runtimeOnlyConfigurationName) {
                    // To run the tests, add 'junit-jupiter-engine' to 'runtimeOnly' scope of the selected source set.
                    // Add it without version - the version is automatically aligned with the JUnit 5 API.
                    dependencySet.add(dependencies.create("org.junit.jupiter:junit-jupiter-engine"))
                }
            }
        }
    }

    private fun Project.reconfigureTestTasks(testTaskProvider: Provider<Test>, fuzzTestFilter: String) {
        tasks.withType(Test::class.java).configureEach { testTask ->
            // Only configure the test task that runs the fuzz tests
            if (testTask == testTaskProvider.get()) {
                // The task needs to use JUnit5 (useJUnitPlatform) and should only run @FuzzTest Jazzer tests
                // (and no normal @Test tests).
                testTask.useJUnitPlatform {
                    it.includeTags = setOf(jazzerJUnit5TestTag)
                }

                testTask.ignoreFailures = true // Do not fail the build if a test fails
                testTask.jvmArgs("-Djazzer.hooks=false") // disable Jazzer hooks as they are not needed for coverage

                testTask.filter { filter ->
                    filter.includeTestsMatching(fuzzTestFilter) // Only include the fuzz test(s) specified by 'cifuzz'
                    filter.isFailOnNoMatchingTests = false // Do not fail the build if the test does not exist
                }
            }
        }
    }

    private fun Project.registerCoverageReportingTask(testTask: Provider<Test>) {
        plugins.apply("jacoco-report-aggregation") // This plugin was added in Gradle 7.4

        val reporting = extensions.getByType(ReportingExtension::class.java)

        // Register a new JacocoCoverageReport which will automatically add a task and configure it to pick up
        // source code and classes from all dependencies, which is required in a multi-project setup.
        reporting.reports.register("cifuzzReport", JacocoCoverageReport::class.java) { report ->
            report.testType.convention("undefined") // Gradle 7.x requires this to not fail test classpath resolution
            configureJacocoReportTask(report.reportTask, testTask)
        }
    }

    private fun Project.registerCoverageReportingTaskLegacy(testTask: Provider<Test>,
                                                            testSourceSet: Provider<SourceSet>) {
        plugins.apply("jacoco")

        // Directly register a JacocoReport task, as JacocoCoverageReport (see above) is not available in Gradle
        // versions older than 7.4.
        val reportTask = tasks.register("cifuzzReport", JacocoReport::class.java) { cifuzzReport ->
            val classesFolders = classesFolderView(testSourceSet)

            // Add sources and classes (compiled code) from the CURRENT project
            cifuzzReport.classDirectories.from(mainSourceSet().output)
            cifuzzReport.sourceDirectories.from(mainSourceSet().java.srcDirs)

            // Add sources and classes (compiled code) from other projects the project depends on
            cifuzzReport.classDirectories.from(classesFolders)
            cifuzzReport.sourceDirectories.from(classesFolders.elements.map { classFolders ->
                // Because the other projects do not always export their source code (before Gradle 7.4), we make the
                // assumption that we can find it in a location relative to the classes.
                classFolders.map {
                    val classFolder = it.asFile
                    val sourceSet = classFolder.name // usually 'main'
                    val jvmLanguage = classFolder.parentFile.name // usually 'java'
                    val projectRoot = classFolder.parentFile.parentFile.parentFile.parentFile
                    File(projectRoot, "$sourceSet/$jvmLanguage")
                }
            })
        }

        configureJacocoReportTask(reportTask, testTask)
    }

    private fun Project.configureJacocoReportTask(reportTask: TaskProvider<JacocoReport>,
                                                  testTask: Provider<Test>) {
        reportTask.configure { cifuzzReport ->
            // Take the execution data from the fuzz test task.
            // This adds a dependency to the task so that it will run before and produce the data.
            cifuzzReport.executionData.setFrom(testTask.map { testTask ->
                testTask.extensions.getByType(JacocoTaskExtension::class.java).destinationFile!!
            })

            cifuzzReport.reports { reports ->
                // Configure which reports are produced, which can be influenced by 'cifuzz' through a Gradle property.
                val format = gradleProperty("cifuzz.report.format") ?: "html"
                reports.html.required.set(format != "jacocoxml")
                reports.xml.required.set(true)

                // Configure where the reports go, which can be influenced by 'cifuzz' through a Gradle property.
                val output = gradleProperty("cifuzz.report.output")
                if (output != null) {
                    reports.html.outputLocation.set(layout.projectDirectory.dir("$output/html"))
                    reports.xml.outputLocation.set(layout.projectDirectory.file("$output/jacoco.xml"))
                }
            }
        }
    }

    private fun Project.classesFolderView(testSourceSet: Provider<SourceSet>): FileCollection {
        val testRuntimeClasspath = configurations.getByName(testSourceSet.get().runtimeClasspathConfigurationName)

        // Get access to all 'classes' folders (compiled code) of dependencies through dependency management.
        return testRuntimeClasspath.incoming.artifactView { view ->
                view.componentFilter { id -> id is ProjectComponentIdentifier }
                view.attributes.attribute(
                    LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(
                        LibraryElements::class.java, LibraryElements.CLASSES
                    )
                )
            }.files
    }

    private fun Project.mainSourceSet(): SourceSet =
        extensions.getByType(SourceSetContainer::class.java).getByName("main")

    private fun Project.gradleProperty(name: String): String? = if (isGradleVersionWithTestSuitesSupport) {
        providers.gradleProperty(name).orNull
    } else {
        // Fallback for older Gradle versions that either do not have 'Providers.gradleProperty' or require the
        // now deprecated 'Provider.forUseAtConfigurationTime()'.
        findProperty(name) as String?
    }
}
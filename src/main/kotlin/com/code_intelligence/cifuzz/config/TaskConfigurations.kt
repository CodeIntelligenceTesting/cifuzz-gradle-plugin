package com.code_intelligence.cifuzz.config

import com.code_intelligence.cifuzz.StandardJvmTestSetAccess
import com.code_intelligence.cifuzz.TestSetAccess
import com.code_intelligence.cifuzz.tasks.*
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.util.GradleVersion
import java.io.File

private val isGradleVersionWithTestSuitesSupport = GradleVersion.current() >= GradleVersion.version("7.4")
private const val JAZZER_VERSION = "0.19.0"
private const val JAZZER_JUNIT5_TEST_TAG = "jazzer"

internal fun Project.configureCIFuzzPlugin(testSetAccess: TestSetAccess) {
    val fuzzTestProperty = gradleProperty("cifuzz.fuzztest")

    // Register cifuzz help tasks
    registerPrintCIFuzzPluginVersion()
    registerPrintBuildDir()
    registerPrintClasspath(testSetAccess)
    registerPrintPackages(testSetAccess)
    registerPrintTestSourceSets(testSetAccess)

    // Automatically add dependencies to Jazzer
    addJazzerDependencies(testSetAccess)

    // The test task selected for fuzz testing is only (re)configured when started through 'cifuzz' for coverage
    // to keep normal Gradle test execution untouched.
    if (fuzzTestProperty != null) {
        reconfigureTestTasks(testSetAccess.testTask, fuzzTestProperty)
    }

    // Register a custom JacocoReport task to avoid side effects on existing user tasks when overwriting config
    // values (like the output path).
    if (isGradleVersionWithTestSuitesSupport && testSetAccess is StandardJvmTestSetAccess) {
        registerCoverageReportingTask(testSetAccess.testTask)
    } else {
        registerCoverageReportingTaskLegacy(testSetAccess)
    }
}

private fun Project.registerPrintCIFuzzPluginVersion() {
    tasks.register("cifuzzPrintPluginVersion", PluginVersionPrinter::class.java)
}

private fun Project.registerPrintBuildDir() {
    tasks.register("cifuzzPrintBuildDir", BuildDirectoryPrinter::class.java) { printBuildDir ->
        printBuildDir.buildDirectory.set(layout.buildDirectory.map { it.asFile.absolutePath })
    }
}

private fun Project.registerPrintClasspath(testSetAccess: TestSetAccess) {
    tasks.register("cifuzzPrintTestClasspath", ClasspathPrinter::class.java) { printClasspath ->
        printClasspath.testRuntimeClasspath.from(testSetAccess.testRuntimeClasspath)
    }
}

private fun Project.registerPrintPackages(testSetAccess: TestSetAccess) {
    tasks.register("cifuzzPrintPackages", PackagesPrinter::class.java) { printClasspath ->
        printClasspath.runtimeClasspath.from(testSetAccess.mainClasses)
        printClasspath.runtimeClasspath.from(classesFolderView(testSetAccess))
    }
}

private fun Project.registerPrintTestSourceSets(testSetAccess: TestSetAccess) {
    tasks.register("cifuzzPrintTestSourceFolders", TestSourceSetsPrinter::class.java) { printTestSourceSets ->
        printTestSourceSets.testSourceSets.from(testSetAccess.testSources)
    }
}

private fun Project.addJazzerDependencies(testSetAccess: TestSetAccess) {
    configurations.all { configuration ->
        // Use 'withDependencies { }' to access 'testSourceSet.get()' at the latest point possible.
        configuration.withDependencies { dependencySet ->
            if (configuration.name == testSetAccess.testImplementationConfigurationName) {
                // To write fuzz tests, add 'jazzer-junit' to 'implementation' scope of the selected source set.
                // Jazzer brings in the JUnit5 API with a defined version.
                dependencySet.add(dependencies.create("com.code-intelligence:jazzer-junit:$JAZZER_VERSION"))
            }
            if (configuration.name == testSetAccess.testRuntimeOnlyConfigurationName) {
                // To run the tests, add 'junit-jupiter-engine' to 'runtimeOnly' scope of the selected source set.
                // Add it without version - the version is automatically aligned with the JUnit 5 API.
                dependencySet.add(dependencies.create("org.junit.jupiter:junit-jupiter-engine"))
            }
        }
    }
}

private fun Project.reconfigureTestTasks(testTaskProvider: Provider<out Task>, fuzzTestFilter: String) {
    tasks.withType(Test::class.java).configureEach { testTask ->
        // Only configure the test task that runs the fuzz tests
        if (testTask == testTaskProvider.get()) {
            // The task needs to use JUnit5 (useJUnitPlatform) and should only run @FuzzTest Jazzer tests
            // (and no normal @Test tests).
            testTask.useJUnitPlatform {
                it.includeTags = setOf(JAZZER_JUNIT5_TEST_TAG)
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

private fun Project.registerCoverageReportingTask(testTask: Provider<out Task>) {
    plugins.apply("jacoco-report-aggregation") // This plugin was added in Gradle 7.4

    val reporting = extensions.getByType(ReportingExtension::class.java)

    // Register a new JacocoCoverageReport which will automatically add a task and configure it to pick up
    // source code and classes from all dependencies, which is required in a multi-project setup.
    reporting.reports.register("cifuzzReport", JacocoCoverageReport::class.java) { report ->
        report.testType.convention("undefined") // Gradle 7.x requires this to not fail test classpath resolution
        configureJacocoReportTask(report.reportTask, testTask)
    }
}

private fun Project.registerCoverageReportingTaskLegacy(testSetAccess: TestSetAccess) {
    plugins.apply("jacoco")

    // Directly register a JacocoReport task, as JacocoCoverageReport (see above) is not available in Gradle
    // versions older than 7.4.
    val reportTask = tasks.register("cifuzzReport", JacocoReport::class.java) { cifuzzReport ->
        val classesFolders = classesFolderView(testSetAccess)

        // Add sources and classes (compiled code) from the CURRENT project
        cifuzzReport.classDirectories.from(testSetAccess.mainClasses)
        cifuzzReport.sourceDirectories.from(testSetAccess.mainSources)

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

    configureJacocoReportTask(reportTask, testSetAccess.testTask)
}

private fun Project.configureJacocoReportTask(reportTask: TaskProvider<JacocoReport>,
                                              testTask: Provider<out Task>
) {
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

private fun Project.classesFolderView(testSetAccess: TestSetAccess): FileCollection {
    val testRuntimeClasspath = configurations.getByName(testSetAccess.testRuntimeClasspathConfigurationName)

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

private fun Project.gradleProperty(name: String): String? = if (isGradleVersionWithTestSuitesSupport) {
    providers.gradleProperty(name).orNull
} else {
    // Fallback for older Gradle versions that either do not have 'Providers.gradleProperty' or require the
    // now deprecated 'Provider.forUseAtConfigurationTime()'.
    findProperty(name) as String?
}

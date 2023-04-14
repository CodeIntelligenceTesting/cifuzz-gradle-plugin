package com.code_intelligence.cifuzz

import com.code_intelligence.cifuzz.android.registerCIFuzzAndroidExtensionAndConfigure
import com.code_intelligence.cifuzz.config.configureCIFuzzPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.util.GradleVersion

@Suppress("unused")
abstract class CIFuzzPlugin : Plugin<Project> {

    private val isSupportedGradleVersion = GradleVersion.current() >= GradleVersion.version("6.1")

    override fun apply(project: Project) {
        if (!isSupportedGradleVersion) {
            throw IllegalStateException("Plugin requires at least Gradle 6.1")
        }

        // The plugin relies on the 'java' plugin being applied first, which is implicitly applied by plugins like:
        // 'application', 'java-library', 'groovy', 'scala', 'kotlin("jvm")', ...
        project.plugins.withId("java") {
            val cifuzz = project.registerCIFuzzExtension()
            project.configureCIFuzzPlugin(StandardJvmTestSetAccess(cifuzz))
        }
        project.plugins.withId("com.android.base") {
            project.registerCIFuzzAndroidExtensionAndConfigure()
            // the above registers a callback to 'configureCIFuzzPlugin(...)'
        }
    }

    private fun Project.registerCIFuzzExtension() = extensions.create("cifuzz", CIFuzzExtension::class.java).apply {
        // Register extension for fine-tuning - can be used in 'build.gradle' files like this:
        //   cifuzz {
        //     testSourceSet.set(customFuzzTestSourceSet)
        //     testTask.set(customFuzzTestTask)
        //   }
        val sourceSets = extensions.getByType(SourceSetContainer::class.java)

        testSourceSet.convention(sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME))
        mainSourceSet.convention(sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME))
        testTask.convention(testSourceSet.flatMap { sourceSet -> tasks.named(sourceSet.name, Test::class.java) })
    }
}
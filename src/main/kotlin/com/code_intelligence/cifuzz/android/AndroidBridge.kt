package com.code_intelligence.cifuzz.android

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.AndroidTest
import com.android.build.api.variant.UnitTest
import com.code_intelligence.cifuzz.config.configureCIFuzzPlugin
import org.gradle.api.Project
import java.util.Locale

internal fun Project.registerCIFuzzAndroidExtensionAndConfigure() {
    extensions.create("cifuzz", CIFuzzAndroidExtension::class.java).apply {
        androidVariant.convention("release")
        androidTest.convention(false) // false == UnitTest
        extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
            if (variant.name == androidVariant.get()) {
                configureCIFuzzPlugin(AndroidTestSetAccess(project, variant,
                    if (androidTest.get()) AndroidTest::class.java else UnitTest::class.java))

                tasks.configureEach {
                    val unitTest = variant.unitTest
                    if (unitTest != null && it.name == "compile${unitTest.name.capitalized()}Kotlin") {
                        val bundleClassesToCompileJarTask = "bundle${variant.name.capitalized()}ClassesToCompileJar"
                        if (tasks.names.contains(bundleClassesToCompileJarTask)) {
                            // TODO figure out why 'cifuzzPrintTestClasspath' causes an error without this
                            it.dependsOn(tasks.named(bundleClassesToCompileJarTask))
                        }
                    }
                }
            }
        }
    }
}

internal fun CharSequence.capitalized(): String =
    when {
        isEmpty() -> ""
        else -> get(0).let { initial ->
            when {
                initial.isLowerCase() -> initial.titlecase(Locale.getDefault()) + substring(1)
                else -> toString()
            }
        }
    }

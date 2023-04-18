package com.code_intelligence.cifuzz.android

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.UnitTest
import com.code_intelligence.cifuzz.config.configureCIFuzzPlugin
import org.gradle.api.Project
import java.util.Locale

internal fun Project.registerCIFuzzAndroidExtensionAndConfigure() {
    extensions.create("cifuzz", CIFuzzAndroidExtension::class.java).apply {
        androidVariant.convention("release")
        // androidTest.convention(false) // false == UnitTest
        extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
            if (variant.name == androidVariant.get()) {
                configureCIFuzzPlugin(AndroidTestSetAccess(project, variant, UnitTest::class.java))
                    // if (androidTest.get()) AndroidTest::class.java else UnitTest::class.java))
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

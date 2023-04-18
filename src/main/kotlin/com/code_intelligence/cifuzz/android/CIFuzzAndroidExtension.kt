package com.code_intelligence.cifuzz.android

import org.gradle.api.provider.Property

/**
 * Configuration options for custom Gradle test setup of Android projects.
 * This can be used to configure that fuzz tests are NOT located in the default source set (src/test).
 */
interface CIFuzzAndroidExtension {
    /**
     * The 'Android Variant' for which to run the fuzz tests (e.g. 'release', 'fullDebug').
     * The default is 'release'.
     */
    val androidVariant: Property<String>

    // val androidTest: Property<Boolean>
}

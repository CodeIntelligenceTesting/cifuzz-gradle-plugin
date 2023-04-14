package com.code_intelligence.cifuzz.android

import org.gradle.api.provider.Property

/**
 * Configuration options for custom Gradle test setup.
 * This can be used to configure that fuzz tests are NOT located in the default source set (src/test).
 */
interface CIFuzzAndroidExtension {
    val androidVariant: Property<String>
    val androidTest: Property<Boolean>
}

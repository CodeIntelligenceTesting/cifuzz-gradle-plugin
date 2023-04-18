package com.code_intelligence.cifuzz.test.android

import org.junit.jupiter.api.BeforeEach
import java.io.File


class MultiProjectAndroidLibDemoReleaseVariantTest : MultiProjectAndroidTest() {

    override fun testedAndroidVariant() = "demoRelease"

    @BeforeEach
    fun adjustSample() {
        File(projectDir, "app/build.gradle.kts").appendText("""
            cifuzz.androidVariant.set("demoRelease")    
            
            android {
                flavorDimensions += "version"
                productFlavors {
                    create("demo") {
                        dimension = "version"
                        applicationIdSuffix = ".demo"
                        versionNameSuffix = "-demo"
                    }
                    create("full") {
                        dimension = "version"
                        applicationIdSuffix = ".full"
                        versionNameSuffix = "-full"
                    }
                }
            }
        """.trimIndent())
    }
}

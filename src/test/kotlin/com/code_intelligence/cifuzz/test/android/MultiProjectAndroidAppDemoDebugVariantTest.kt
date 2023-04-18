package com.code_intelligence.cifuzz.test.android

import org.junit.jupiter.api.BeforeEach
import java.io.File


class MultiProjectAndroidAppDemoDebugVariantTest : MultiProjectAndroidTest() {

    override fun testedAndroidVariant() = "demoDebug"

    @BeforeEach
    fun adjustSample() {
        File(projectDir, "app/build.gradle.kts").appendText("""
            cifuzz.androidVariant.set("demoDebug")    
            
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

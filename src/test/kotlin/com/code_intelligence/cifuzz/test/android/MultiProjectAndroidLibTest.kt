package com.code_intelligence.cifuzz.test.android

import org.junit.jupiter.api.BeforeEach
import java.io.File


abstract class MultiProjectAndroidLibTest : MultiProjectAndroidTest() {

    @BeforeEach
    fun turnSampleIntoLibrary() {
        File(projectDir, "app/build.gradle.kts").apply {
            writeText(readText()
                .replace("com.android.application", "com.android.library")
                .replace("""applicationId = "org.example"""", "")
                .replace("versionCode = 1", "")
                .replace("""versionName = "1.0"""", "")
            )
        }
    }
}

package com.code_intelligence.cifuzz.test.android

import org.junit.jupiter.api.BeforeEach
import java.io.File


class MultiProjectAndroidJavaLibDebugVariantTest : MultiProjectAndroidLibTest() {

    override fun testedAndroidVariant() = "debug"

    override fun example() = "multi-project-android-app-java"

    override fun languageFileExt() = "java"

    override fun languageTestClassesPath() = "intermediates/javac"

    @BeforeEach
    fun adjustSample() {
        File(projectDir, "app/build.gradle.kts").appendText("""cifuzz.androidVariant.set("debug")""")
    }
}

package com.code_intelligence.cifuzz.test.android

import org.junit.jupiter.api.Tag

@Tag("android")
class MultiProjectAndroidJavaAppReleaseTest : MultiProjectAndroidTest() {

    override fun testedAndroidVariant() =  "release"

    override fun example() = "multi-project-android-app-java"

    override fun languageFileExt() = "java"

    override fun languageTestClassesPath() = "intermediates/javac"
}

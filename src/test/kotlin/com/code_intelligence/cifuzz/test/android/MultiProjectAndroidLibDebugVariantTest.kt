package com.code_intelligence.cifuzz.test.android

import org.junit.jupiter.api.BeforeEach
import java.io.File


class MultiProjectAndroidLibDebugVariantTest : MultiProjectAndroidLibTest() {

    override fun testedAndroidVariant() = "debug"

    @BeforeEach
    fun adjustSample() {
        File(projectDir, "app/build.gradle.kts").appendText("""cifuzz.androidVariant.set("debug")""")
    }
}

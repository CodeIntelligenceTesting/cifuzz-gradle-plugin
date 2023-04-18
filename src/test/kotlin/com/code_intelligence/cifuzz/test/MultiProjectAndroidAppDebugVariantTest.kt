package com.code_intelligence.cifuzz.test

import org.junit.jupiter.api.BeforeEach
import java.io.File


class MultiProjectAndroidAppDebugVariantTest : MultiProjectAndroidTest() {

    override fun testedAndroidVariant() = "debug"

    @BeforeEach
    fun adjustSample() {
        File(projectDir, "app/build.gradle.kts").appendText("""cifuzz.androidVariant.set("debug")""")
    }
}

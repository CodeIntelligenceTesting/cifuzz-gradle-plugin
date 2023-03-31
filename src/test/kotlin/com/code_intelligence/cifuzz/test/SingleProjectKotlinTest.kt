package com.code_intelligence.cifuzz.test


class SingleProjectKotlinTest : SingleProjectTest() {

    override fun example() = "single-project-kotlin"

    override fun jvmLanguageFolder() = "kotlin"

    override fun jvmLanguageFileExt() = "kt"
}

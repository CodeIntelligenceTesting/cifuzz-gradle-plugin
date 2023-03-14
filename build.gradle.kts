plugins {
    id("com.gradle.plugin-publish") version "1.1.0"
    `embedded-kotlin`
}

group = "com.code-intelligence"

gradlePlugin {
    plugins.create("cifuzz") {
        id = "$group.$name"
        implementationClass = "com.code_intelligence.cifuzz.CIFuzzPlugin"
    }
}

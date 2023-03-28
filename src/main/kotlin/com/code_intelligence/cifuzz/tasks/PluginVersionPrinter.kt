package com.code_intelligence.cifuzz.tasks

import com.code_intelligence.cifuzz.CIFuzzPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class PluginVersionPrinter : DefaultTask() {
    private val pluginJarName = "cifuzz-gradle-plugin"

    @TaskAction
    fun print() {
        val pluginJar = CIFuzzPlugin::class.java.protectionDomain.codeSource.location.path
        if (pluginJar.contains("/$pluginJarName-")) {
            val pluginVersion = pluginJar.substring(
                pluginJar.lastIndexOf("/$pluginJarName-") + "/$pluginJarName-".length,
                pluginJar.lastIndexOf(".jar")
            )
            println("cifuzz.plugin.version=$pluginVersion")
        }
    }

}
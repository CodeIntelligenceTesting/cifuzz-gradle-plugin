package com.code_intelligence.cifuzz.tasks

import com.code_intelligence.cifuzz.CIFuzzPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Prints the current cifuzz plugin version.
 * This information is used by 'cifuzz'.
 */
abstract class PluginVersionPrinter : DefaultTask() {

    @TaskAction
    fun print() {
        val pluginJarName = "cifuzz-gradle-plugin"
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
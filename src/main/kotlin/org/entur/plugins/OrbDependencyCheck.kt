package org.entur.plugins

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

open class OrbDependencyCheck : DefaultTask() {

    init {
        group = "check"
        description = "Checks each orb present in .circleci/config.yml against the latest version on Circleci.com"
    }


    @get: InputFile
    val circleCiConfigFile: File = project.file(".circleci/config.yml")

    @TaskAction
    fun doDependencyCheck() {
        // Only enable task when we don't use the --offline command line argument.
        //if (gradle.startParameter.offline) return

        if (!circleCiConfigFile.exists() || !circleCiConfigFile.canRead()) {
            throw GradleException("Cannot read file .circleci/config.yml!")
        }

        val configText = circleCiConfigFile.readText(Charsets.UTF_8)
        val orbNameRegex = "[a-z]+/[a-z]+@[0-9]+\\.[0-9]+\\.[0-9]+".toRegex()

        val currentOrbs = orbNameRegex.findAll(configText)
        val orbsWithVersions = currentOrbs.map { it.value }
            .map { Pair(it, fetchLatestVersion(it)) }
            .groupBy { (key, value) ->
                value?.let {
                    key.split("@").last() == value
                }
            }

        println("------------------------------------------------------------")
        println(": Project Orb Dependency Updates ")
        println("------------------------------------------------------------")
        println()

        if (!orbsWithVersions[true].isNullOrEmpty()) {
            println("The following orbs are using the latest release version:")
            orbsWithVersions[true]?.forEach { (key, _) -> println("- $key") }
            println()
        }

        if (!orbsWithVersions[false].isNullOrEmpty()) {
            println("The following orbs have later release versions:")
            orbsWithVersions[false]?.forEach { (key, value) ->
                println("- $key -> $value")
                println("        https://circleci.com/orbs/registry/orb/${key.split("@").first()}")
            }
            println()
        }

        if (!orbsWithVersions[null].isNullOrEmpty()) {
            println("Unable to determine version for orbs:")
            orbsWithVersions[null]?.forEach { (key, _) -> println("- $key") }
        }

    }

    private fun fetchLatestVersion(orbName: String): String? {
        val circleCiApi = "https://circleci.com/graphql-unstable"
        val orbQuery =
            "{\"query\" : \"query{ orbVersion(orbVersionRef: \\\"$orbName\\\") { id orb {  name versions(count: 1) { version }}} }\"}"

        return try {
            val req = URL(circleCiApi).openConnection() as HttpURLConnection
            req.requestMethod = "POST"
            req.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            req.doOutput = true
            req.outputStream.write(orbQuery.toByteArray(Charsets.UTF_8))

            val gson = GsonBuilder().create()
            val responseObject = gson.fromJson(InputStreamReader(req.inputStream), JsonObject::class.java)

            return responseObject
                .getAsJsonObject("data")
                .getAsJsonObject("orbVersion")
                .getAsJsonObject("orb")
                .getAsJsonArray("versions")
                .first()
                .asJsonObject
                .getAsJsonPrimitive("version")
                .asString

        } catch (ex: Exception) {
            println("Error getting latest version for orb $orbName, ${ex.message}")
            null
        }
    }


}

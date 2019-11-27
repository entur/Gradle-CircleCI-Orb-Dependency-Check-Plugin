package org.entur.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class OrbDependencyCheckPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("orbsDependencyCheck", OrbDependencyCheck::class.java)
    }

}


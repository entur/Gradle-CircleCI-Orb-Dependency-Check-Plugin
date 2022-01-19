package org.entur.plugins

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldNotBe
import org.gradle.testfixtures.ProjectBuilder

class PluginTest : WordSpec({

  "Using the Plugin ID" should {
    "Apply the Plugin" {
      val project = ProjectBuilder.builder().build()
      project.pluginManager.apply("org.entur.plugins.orbdependencycheck")

      project.plugins.getPlugin(OrbDependencyCheckPlugin::class.java) shouldNotBe null
    }
  }

})
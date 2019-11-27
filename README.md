# Orb depedency check plugin

This is a simple gradle plugin to check the orbs used in the circle ci config against the newest 
config at [Circleci orb registry](https://circleci.com/orbs/registry). The config is assumed to be located under `$projectRoot/.circleci/config.yml`

Inspired by https://github.com/ben-manes/gradle-versions-plugin

## How to use

Add the following to build.gradle
```
plugins {
    id 'org.entur.plugins.orbdependencycheck'
}

check.dependsOn orbsDependencyCheck

```

### Example output

```

------------------------------------------------------------
: Project Dependency Updates 
------------------------------------------------------------

The following orbs are using the latest release version:
- circleci/gradle@1.0.11

The following orbs have later release versions:
- circleci/slack@2.2.0 -> 3.4.1
        https://circleci.com/orbs/registry/orb/circleci/slack

Unable to determime version for orbs:
- unknown/orb@1.2.3

```

## Deploy

    gw publishPlugins -Pgradle.publish.key=[KEY] -Pgradle.publish.secret=[SECRET]

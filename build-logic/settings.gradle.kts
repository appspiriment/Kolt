pluginManagement {
    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
    // The `libs` catalog is auto-created by kotlin-dsl from gradle/libs.versions.toml.
}

rootProject.name = "build-logic"
include(":conventions")
// Lean, separate plugin module that carries ONLY the vanniktech maven-publish plugin (no KGP/AGP).
// Library modules apply io.github.appspiriment.kolt.publish from here so vanniktech shares one
// classloader across the whole build, while modules that don't need Kotlin (e.g. the BOM
// java-platform) don't pull KGP into their classloader scope.
include(":publish")

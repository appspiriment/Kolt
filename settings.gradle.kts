pluginManagement {
    // Convention plugins are supplied from the included build (build-logic) directly
    // from source — no publish-to-mavenLocal round-trip during local development.
    includeBuild("build-logic")
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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
    versionCatalogs {
        // `libs` is auto-created from gradle/libs.versions.toml (lib-module build deps).
        create("koltlibs") {
            from(files("gradle/koltlibs.versions.toml"))
        }
        create("kmplibs") {
            from(files("gradle/kmplibs.versions.toml"))
        }
    }
}

rootProject.name = "UtilsLibs"

// ── Runtime library modules ──────────────────────────────────────────────────
// Each applies the in-repo convention plugins from build-logic.
include(":libs:bom")            // Bill of Materials — publish first before individual libs
include(":libs:utils")
include(":libs:logutils")
include(":libs:compose-utils")
include(":libs:compose-kmp")
include(":libs:update-utils")
include(":libs:location")
include(":demo-app")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        // Appspiriment convention plugins + pinned Android library versions
        create("koltlibs") {
            from("io.github.appspiriment.kolt:appspiriment-catalog:0.1.6")
            // Replace 0.1.0 with the current release from https://github.com/appspiriment/UtilsLibs/releases
        }
    }
}

rootProject.name = "<AppName>"        // ← replace

include(":app")

// Feature / layer modules — uncomment as you add them:
// include(":feature:home")
// include(":feature:settings")
// include(":domain")
// include(":data")
// include(":core:common")

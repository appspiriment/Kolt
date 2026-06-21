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
        // Appspiriment Android convention plugins + pinned Android lib versions
        create("koltlibs") {
            from("io.github.appspiriment.kolt:appspiriment-catalog:0.1.6")
            // Replace 0.1.0 with the current release from https://github.com/appspiriment/UtilsLibs/releases
        }
        // Appspiriment KMP convention plugins + pinned KMP lib versions
        create("kmplibs") {
            from("io.github.appspiriment.kolt:kmp-catalog:0.1.6")
        }
    }
}

rootProject.name = "<AppName>"        // ← replace

include(":app")
include(":shared")
include(":data")

// Additional modules — uncomment as you add them:
// include(":core:common")
// include(":feature:home")

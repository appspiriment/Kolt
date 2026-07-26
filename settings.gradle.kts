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
    // PREFER_SETTINGS, not FAIL_ON_PROJECT_REPOS: Kotlin's own wasmJs/js tooling
    // (kotlinWasmNodeJsSetup) always registers its own project-level ivy repo to download
    // Node.js — that's unavoidable, not something a settings-level repo declaration can
    // suppress, so FAIL_ON_PROJECT_REPOS hard-fails the moment a wasmJs `browser {}` execution
    // task runs. PREFER_SETTINGS still gives the settings-declared repos priority for anything
    // they cover (everything below) and only falls back to a project repo — the Node.js one —
    // for what nothing here declares.
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        // Kotlin/JS-Wasm's own tooling (kotlinWasmNodeJsSetup) registers a project-level repo
        // to download Node.js, which FAIL_ON_PROJECT_REPOS blocks. Declaring it here — scoped
        // to just the org.nodejs group via exclusiveContent — is the documented workaround.
        exclusiveContent {
            forRepository {
                ivy("https://nodejs.org/dist/") {
                    name = "Node Distributions at https://nodejs.org/dist"
                    patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("org.nodejs") }
        }
        exclusiveContent {
            forRepository {
                ivy("https://github.com/WebAssembly/binaryen/releases/download") {
                    name = "Binaryen Distributions at $url"
                    patternLayout {
                        artifact("version_[revision]/[module]-version_[revision]-[classifier].[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("com.github.webassembly") }
        }
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
include(":libs:location-picker")
include(":demo-app")
include(":demo-web")

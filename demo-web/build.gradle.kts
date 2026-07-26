import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Standalone wasmJs-only demo app for browser verification of :libs:location-picker's Web
// target. Deliberately NOT part of :demo-app: that module's commonMain depends on
// :libs:compose-kmp / :libs:utils, neither of which has a wasmJs target — dragging those two
// modules into Web support just to prove this one already-working module runs in a browser
// isn't this task. :libs:location-picker itself has zero dependency on compose-kmp/utils, so a
// small standalone module keeps the blast radius to what's actually being verified.
kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "demo-web.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(project(":libs:location-picker"))
            }
        }
    }
}

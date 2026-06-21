// KMP shared module — ViewModels, domain use cases, and shared logic
// Platform-specific code goes in androidMain / iosMain / desktopMain via expect/actual
plugins {
    id("io.github.appspiriment.kolt.kmp.library-koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Domain / use-case dependencies only — no platform SDKs here
            // implementation(project(":core:common"))
        }
        androidMain.dependencies {
            // Android-specific runtime dependencies
        }
    }
}

// Optional: enable additional targets
// kmp {
//     enableIos.set(true)
//     enableDesktop.set(false)
//     enableUtils.set(true)     // default: true — adds utils + logutils
// }

// ------------------------------------------------------------------
// Source set structure (auto-created by the convention plugin):
//
//   src/
//     commonMain/kotlin/   ← ViewModels, use cases, domain entities
//     androidMain/kotlin/  ← Android-specific expect/actual impls
//     iosMain/kotlin/      ← (created if enableIos = true)
//     commonTest/kotlin/   ← unit tests (reducers, use cases, mappers)
//     androidUnitTest/kotlin/
// ------------------------------------------------------------------

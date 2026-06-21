// Android host module — thin launcher that starts the KMP shared module
plugins {
    id("io.github.appspiriment.kolt.kmp.application")
}

android {
    namespace = "com.<company>.<appname>"   // ← replace

    defaultConfig {
        applicationId = "com.<company>.<appname>"   // ← same as namespace
        versionCode = 1
        versionName = "1.0.0"
    }
}

// Optional: override KMP application defaults
// kmp {
//     enableMinify.set(false)                        // default: false
//     enableUtils.set(true)                          // default: true
//     addDevSuffixToDebug.set(true)                  // default: true — append suffix to debug applicationId
//     debugApplicationIdSuffix.set(".dev")           // default: ".dev" — override, e.g. ".qa"
//     appendTimestampToDebugVersion.set(true)        // default: true — append timestamp to debug versionName
//     debugVersionTimestampPattern.set("yyyyMMdd-HHmm") // default: "yyyyMMdd-HHmm"
// }

dependencies {
    // Wire the KMP shared module into the Android host
    implementation(project(":shared"))
    implementation(project(":data"))

    // compose-utils for the Android launcher UI (if your :shared doesn't expose Compose).
    // Version is pinned by the Appspiriment BOM the convention plugin injects — no version needed.
    implementation("io.github.appspiriment.kolt:compose")

    // Update dialog (optional)
    // implementation("io.github.appspiriment.kolt:update-utils")
}

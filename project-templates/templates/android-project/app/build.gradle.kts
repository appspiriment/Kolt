plugins {
    id("io.github.appspiriment.kolt.application")
}

android {
    namespace = "com.<company>.<appname>"   // ← replace (e.g. "com.example.myapp")

    defaultConfig {
        applicationId = "com.<company>.<appname>"   // ← same as namespace
        versionCode = 1
        versionName = "1.0.0"
    }
}

// Optional: override Appspiriment convention defaults
// appspiriment {
//     enableUtils.set(true)                          // default: true — adds utils + logutils
//     enableMinify.set(false)                        // default: false — enable R8 on release
//     addDevSuffixToDebug.set(true)                  // default: true — append suffix to debug applicationId
//     debugApplicationIdSuffix.set(".dev")           // default: ".dev" — override, e.g. ".qa"
//     appendTimestampToDebugVersion.set(true)        // default: true — append timestamp to debug versionName
//     debugVersionTimestampPattern.set("yyyyMMdd-HHmm") // default: "yyyyMMdd-HHmm"
//     scaffoldThemeResources.set(true)               // default: true — writes theme XMLs on first build
// }

dependencies {
    // App-specific dependencies here.
    //
    // The convention plugin auto-adds the Appspiriment runtime libs (utils, logutils,
    // compose-utils) plus the Compose UI stack, lottie, and hilt-navigation-compose — you
    // do NOT declare those here. All io.github.appspiriment.kolt:* versions are pinned by the
    // BOM the plugin injects, so reference them without a version if you ever add one
    // explicitly, e.g. implementation("io.github.appspiriment.kolt:update-utils").

    // Feature modules depend on domain, never on each other:
    // implementation(project(":domain"))

    // Third-party libs NOT provided by the plugin: declare the coordinate directly. The
    // koltlibs catalog is versions-only, so pull the version from it, e.g.:
    // implementation("io.coil-kt:coil-compose:${'$'}{koltlibs.versions.coil.get()}")
}

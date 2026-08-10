plugins {
    id("io.github.appspiriment.kolt.kmp.data")
    alias(kmplibs.plugins.kotlin.serialization)
    id("io.github.appspiriment.kolt.publish")
    alias(libs.plugins.dokka)
}

// Kotlin Compose *compiler* plugin only (not org.jetbrains.compose/Compose Multiplatform — that
// one hooks into KotlinJvmTarget registration, and this module's desktop target is created
// lazily in afterEvaluate by kmp.data, the exact ordering bug KmpLibraryComposeConventionPlugin's
// doc comment works around). The one Android-only Composable here
// (LocationSettingsResolver.android.kt) only needs androidx.compose.runtime + activity-compose,
// both plain Android artifacts below — no Compose Multiplatform needed.
//
// `pluginManager.apply(id)` (the API, not the `plugins { }` block's `alias(...)`/`id(...)`
// sugar, and not the top-level `apply(plugin = "...")` script function either) — the plugin
// class is already on the shared buildscript classpath (root build.gradle.kts's
// `classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.3.10")`, put there
// specifically so every module gets it from one ClassLoader, per that file's own comment).
// Both `plugins { alias(kmplibs.plugins.kotlin.compose.compiler) }` (requests a *version*
// through Gradle's plugin-portal-aware resolution) and the top-level `apply(plugin = "...")`
// script function threw "already on the classpath with an unknown version, so compatibility
// cannot be checked" — the same version-reconciliation path `pluginManager.apply(id)` skips
// entirely, which is exactly how `KmpExtensions.kt`'s `applyKmpPluginFromLibs` (used by every
// *working* Compose-enabled module in this repo) applies plugins already on that same shared
// classpath.
pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

kmp {
    enableUtils.set(false)
    enableDesktop.set(true)
    enableIos.set(true)
    enableWasm.set(true)
}

android {
    namespace = "io.github.appspiriment.kolt.location"
}

kmpDataLayer {
    ktor { enabled.set(true) }
    serialization { enabled.set(true) }
}

dependencies {
    "androidMainImplementation"("androidx.core:core-ktx:${koltlibs.versions.coreKtx.get()}")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

    // rememberLocationSettingsResolver's Compose + Activity-launcher plumbing. The Kotlin Compose
    // *compiler* plugin (applied below, imperatively) instruments every target's Kotlin
    // compilation in this module, not just androidMain's — so compose-runtime needs to be
    // resolvable everywhere too, or non-Android targets fail with "Compose Runtime not found on
    // the classpath" even though they contain no @Composable code. `commonMain` is the fix: the
    // plain multiplatform runtime artifact (not the `org.jetbrains.compose` Gradle plugin, which
    // is what actually carries the desktop-target ordering issue noted above).
    "commonMainImplementation"("org.jetbrains.compose.runtime:runtime:${kmplibs.versions.composeMultiplatform.get()}")
    "androidMainImplementation"("androidx.activity:activity-compose:${koltlibs.versions.activityCompose.get()}")

    // rememberLocationSettingsResolver's SettingsClient/ResolvableApiException in-app
    // "turn on location" dialog.
    "androidMainImplementation"("com.google.android.gms:play-services-location:${koltlibs.versions.playServicesLocation.get()}")
}

mavenPublishing {
    coordinates(artifactId = "location")
    pom {
        name = "Kolt Location"
        description = "Cross-platform current-location fetching: native GPS + reverse " +
            "geocoding on Android/iOS, IP-based geolocation on Desktop, browser Geolocation API on Web."
        url = "https://github.com/appspiriment/UtilsLibs"
    }
}

// Every publish to mavenLocal bumps LOCATION_DEV afterwards, so the next build picks up a
// fresh version automatically instead of silently overwriting the same artifact in ~/.m2
// (consumers on mavenLocal won't re-resolve an unchanged version+timestamp).
tasks.named("publishToMavenLocal") {
    finalizedBy(rootProject.tasks.named("bumpLocationVersion"))
}

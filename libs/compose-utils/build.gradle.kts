plugins {
    // Use the in-repo convention plugin so the Kotlin/Android/Compose plugins are loaded from
    // the build-logic classpath — the SAME classloader the KMP library modules use. Applying
    // the Kotlin plugin directly here (with a version) would load a second copy of KGP and
    // crash the multi-module build (KotlinNativeBundleBuildService classloader conflict).
    id("io.github.appspiriment.kolt.library-compose")
    id("io.github.appspiriment.kolt.publish")
    alias(libs.plugins.dokka)
}

// This module IS compose-utils — don't let the plugin add the Kolt runtime libs
// (utils/logutils/compose-utils) to itself. It pulls :libs:utils directly instead.
kolt {
    enableUtils.set(false)
}

android {
    namespace = "io.github.appspiriment.kolt.composeutils"
}

dependencies {
    // The convention plugin already provides the full Compose UI stack (BOM, foundation, ui,
    // material/material3, icons, navigation, lifecycle, serialization, lottie, tooling) — but
    // only as `implementation`, which the publish plugin maps to Maven scope "runtime". Since
    // this module's public API (e.g. rememberPermissionRequest, MessageDialog) exposes Compose
    // types directly, those same artifacts must ALSO be `api` here so external Maven consumers
    // get them on their *compile* classpath too — otherwise calls into this module's public
    // Composables fail to resolve downstream with "Unresolved reference".
    api(platform("androidx.compose:compose-bom:${koltlibs.versions.composeBom.get()}"))
    api("androidx.compose.foundation:foundation")
    api("androidx.compose.ui:ui")
    api("androidx.compose.ui:ui-tooling-preview")
    api("androidx.compose.material:material")
    api("androidx.compose.material3:material3")
    api("androidx.compose.material:material-icons-core-android:1.7.8")
    api("androidx.compose.material:material-icons-extended-android:1.7.8")

    // Depend on our KMP compose library transitively
    api(project(":libs:compose-kmp"))

    // AsyncStateBox requires AsyncState from :utils
    implementation(project(":libs:utils"))

    // Coil is not part of the convention plugin's Compose stack — version from the catalog.
    implementation("io.coil-kt:coil-compose:${koltlibs.versions.coil.get()}")
}

mavenPublishing {
    coordinates(artifactId = "compose")
    pom {
        name = "Kolt Compose"
        description = "Simple Compose components and utility functions for Android development."
        url = "https://github.com/appspiriment/UtilsLibs"
    }
}

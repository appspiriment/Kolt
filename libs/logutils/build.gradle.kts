plugins {
    id("io.github.appspiriment.kolt.kmp.library")
    id("io.github.appspiriment.kolt.publish")
    alias(libs.plugins.dokka)
}

// Don't auto-add appspiriment utils/logutils to this module (it IS logutils).
kmp {
    enableUtils.set(false)
    enableDesktop.set(true)
    enableIos.set(true)
}

android {
    namespace = "io.github.appspiriment.kolt.logutils"
}

dependencies {
    // App Startup powers the auto-gating LogInitializer on Android.
    "androidMainImplementation"("androidx.startup:startup-runtime:1.2.0")
}

mavenPublishing {
    coordinates(artifactId = "logutils")
    pom {
        name = "Kolt LogUtils"
        description = "Lightweight Kotlin Multiplatform logging with automatic debug/release gating on Android."
        url = "https://github.com/appspiriment/UtilsLibs"
    }
}

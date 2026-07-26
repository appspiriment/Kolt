plugins {
    id("io.github.appspiriment.kolt.kmp.data")
    alias(kmplibs.plugins.kotlin.serialization)
    id("io.github.appspiriment.kolt.publish")
    alias(libs.plugins.dokka)
}

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

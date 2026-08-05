plugins {
    id("io.github.appspiriment.kolt.kmp.library-compose")
    id("io.github.appspiriment.kolt.publish")
    alias(libs.plugins.dokka)
}

// This module IS compose-kmp — don't auto-wire in-repo utils to avoid cycle.
kmp {
    enableUtils.set(false)
    enableDesktop.set(true)
    enableIos.set(true)
}

android {
    namespace = "io.github.appspiriment.kolt.composekmp"
}

dependencies {
    // Requires AsyncState from :utils for AsyncStateBox
    "commonMainImplementation"(project(":libs:utils"))
    "commonMainImplementation"("org.jetbrains.compose.material:material-icons-core:1.7.3")
    "commonMainImplementation"("org.jetbrains.compose.material:material-icons-extended:1.7.3")
    "commonMainImplementation"("org.jetbrains.compose.material:material:1.7.3")

    "androidMainImplementation"("io.coil-kt:coil-compose:${koltlibs.versions.coil.get()}")
    "androidMainImplementation"(platform("androidx.compose:compose-bom:${koltlibs.versions.composeBom.get()}"))
    "androidMainImplementation"("androidx.compose.ui:ui-text-google-fonts")
}

mavenPublishing {
    coordinates(artifactId = "compose-kmp")
    pom {
        name = "Kolt Compose KMP"
        description = "Core Compose Multiplatform UI components and design system tokens."
        url = "https://github.com/appspiriment/UtilsLibs"
    }
}

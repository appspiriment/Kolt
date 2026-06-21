plugins {
    id("io.github.appspiriment.kolt.kmp.library")
    alias(kmplibs.plugins.kotlin.serialization)
    id("io.github.appspiriment.kolt.publish")
    alias(libs.plugins.dokka)
}

// This module IS appspiriment-utils — don't auto-add the util libraries to itself.
kmp {
    enableUtils.set(false)
    enableDesktop.set(true)
    enableIos.set(true)
}

android {
    namespace = "io.github.appspiriment.kolt.utils"
}

dependencies {
    // Time extensions in androidMain use kotlinx.serialization custom serializers.
    "androidMainImplementation"(
        "org.jetbrains.kotlinx:kotlinx-serialization-json:${kmplibs.versions.kotlinxSerialization.get()}"
    )
    "androidUnitTestImplementation"("junit:junit:4.13.2")
}

mavenPublishing {
    coordinates(artifactId = "utils")
    pom {
        name = "Kolt Utils"
        description = "Common Kotlin Multiplatform util functions and extension methods."
        url = "https://github.com/appspiriment/UtilsLibs"
    }
}

plugins {
    id("io.github.appspiriment.kolt.library-compose")
    id("io.github.appspiriment.kolt.publish")
    alias(libs.plugins.dokka)
}

android {
    namespace = "io.github.appspiriment.kolt.updateutils"
}

kolt {
    enableMinify = false
    // utils is needed (launchPlayStorePage), and logutils for debug tracing
    enableUtils = true
}

dependencies {
    // Firebase coordinates declared explicitly (versions-only catalog); config-ktx is
    // pinned by the Firebase BOM platform.
    implementation(platform("com.google.firebase:firebase-bom:${koltlibs.versions.firebaseBom.get()}"))
    implementation("com.google.firebase:firebase-config-ktx")

    // compose-utils components (AppsPageScaffold, MessageDialog, buttons, wrappers, etc.)
    implementation(project(":libs:compose-utils"))
}

mavenPublishing {
    coordinates(artifactId = "update-utils")
    pom {
        name = "Kolt Update Utils"
        description = "Lightweight Android library that simplifies Firebase Remote Config-driven " +
            "update flows with Compose UI for immediate and flexible update prompts."
        url = "https://github.com/appspiriment/UtilsLibs"
    }
}

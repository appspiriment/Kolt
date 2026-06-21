plugins {
    `kotlin-dsl`
}

group = "io.github.appspiriment.kolt"

// This module deliberately depends ONLY on the vanniktech maven-publish plugin — NOT on KGP/AGP.
// Keeping its classpath lean means a module that applies io.github.appspiriment.kolt.publish (e.g. the
// BOM java-platform) does not pull Kotlin into its classloader scope, avoiding the
// KotlinNativeBundleBuildService classloader conflict in a whole-suite build.
dependencies {
    implementation(libs.vanniktech.maven.publish)
}

gradlePlugin {
    plugins {
        create("appspirimentPublish") {
            id = "io.github.appspiriment.kolt.publish"
            displayName = "Appspiriment Publish"
            description = "Applies the vanniktech maven-publish plugin from a shared, KGP-free classloader."
            implementationClass = "io.github.appspiriment.kolt.publish.AppspirimentPublishConventionPlugin"
        }
    }
}

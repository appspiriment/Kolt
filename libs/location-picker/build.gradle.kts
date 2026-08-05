plugins {
    id("io.github.appspiriment.kolt.kmp.library-compose")
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
    namespace = "io.github.appspiriment.kolt.locationpicker"
}

compose.resources {
    packageOfResClass = "io.github.appspiriment.kolt.locationpicker.generated.resources"
}

dependencies {
    "commonMainApi"(project(":libs:location"))
    "commonMainImplementation"("org.jetbrains.compose.material:material-icons-core:1.7.3")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-datetime:${kmplibs.versions.kotlinxDatetime.get()}")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")
    "commonMainImplementation"(compose.components.resources)

    // Steering-doc MviViewModel base (presentation-mvi.md) — real commonMain KMP artifact
    // since Lifecycle 2.8, not compose-utils' Android-only ViewModel bases. (Not the
    // `-compose` viewModel{} factory artifact — see LocationPickerScreen.kt for why.)
    "commonMainImplementation"("androidx.lifecycle:lifecycle-viewmodel:${koltlibs.versions.lifecycle.get()}")

    // compose-kmp's theme + component library (kolt-libs.md: mandatory reuse) — scoped to the
    // three targets it actually supports. NOT commonMainImplementation: compose-kmp has no
    // wasmJs target, and a commonMain dependency must resolve for every enabled target,
    // including wasmJs. See LocationPickerScreenContent.wasmJs.kt for the resulting split.
    "androidMainImplementation"(project(":libs:compose-kmp"))
    "desktopMainImplementation"(project(":libs:compose-kmp"))

    "androidMainImplementation"("androidx.activity:activity-compose:${koltlibs.versions.activityCompose.get()}")
    "androidMainImplementation"("org.osmdroid:osmdroid-android:${koltlibs.versions.osmdroid.get()}")

    // Desktop's MapPickerContent fetches OSM raster tiles directly (android uses OSMDroid's
    // own HTTP stack, iOS uses native MapKit — neither needs Ktor here).
    "desktopMainImplementation"("io.ktor:ktor-client-core:${kmplibs.versions.ktor.get()}")
    "desktopMainImplementation"("io.ktor:ktor-client-cio:${kmplibs.versions.ktor.get()}")
}

mavenPublishing {
    coordinates(artifactId = "location-picker")
    pom {
        name = "Kolt Location Picker"
        description = "All-in-one, configurable, themeable location-picker UI (search / map / " +
            "current-location / manual entry) for Android, iOS, Desktop and Web — built on top " +
            "of Kolt Location."
        url = "https://github.com/appspiriment/UtilsLibs"
    }
}

// Every publish to mavenLocal bumps LOCATION_PICKER_DEV afterwards, so the next build picks up
// a fresh version automatically instead of silently overwriting the same artifact in ~/.m2
// (consumers on mavenLocal won't re-resolve an unchanged version+timestamp).
tasks.named("publishToMavenLocal") {
    finalizedBy(rootProject.tasks.named("bumpLocationPickerVersion"))
}

// The iosMain source set is only created once the individual iOS targets are added in
// afterEvaluate (KmpBaseConventionPlugin), unlike desktop/android which this convention plugin
// creates eagerly — so this dependency is added via a lazy `matching { }.configureEach { }`
// listener (fires whenever the source set appears, whatever the ordering) instead of the eager
// `dependencies { }` block above, which fails with "Configuration 'iosMainImplementation' not
// found" / "KotlinSourceSet 'iosMain' not found" if looked up too early.
extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
    .sourceSets.matching { it.name == "iosMain" }.configureEach {
        dependencies {
            implementation(project(":libs:compose-kmp"))
        }
    }

// commonTest deps via the Kotlin extension's own source-set DSL (not the raw
// "commonTestImplementation"(...) string API) — needed for kotlin("test") to resolve to the
// correct per-target artifact (kotlin-test-junit for desktop/android, etc).
extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>().apply {
    sourceSets.getByName("commonTest").dependencies {
        implementation(kotlin("test"))
        implementation("app.cash.turbine:turbine:1.2.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${kmplibs.versions.coroutines.get()}")
    }
    // kotlin("test")'s expect annotations (Test/BeforeTest/AfterTest) need a JVM-specific actual
    // (kotlin-test-junit) — JS/Wasm/Native targets ship their actuals bundled in kotlin-test
    // itself, so this is only needed for the two JVM-backed test source sets.
    sourceSets.getByName("desktopTest").dependencies {
        implementation(kotlin("test-junit"))
    }
    sourceSets.matching { it.name == "androidUnitTest" }.configureEach {
        dependencies { implementation(kotlin("test-junit")) }
    }
}

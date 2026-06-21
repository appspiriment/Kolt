package io.github.appspiriment.kolt.conventions.extensions

import org.gradle.api.artifacts.dsl.DependencyHandler

const val IMPLEMENTATION_CONFIGURATION_NAME = "implementation"
const val TEST_IMPLEMENTATION_CONFIGURATION_NAME = "testImplementation"

val basePluginList: List<String> = listOf(
    "org.jetbrains.kotlin.android",
)

val composePluginList: List<String> = listOf(
    "org.jetbrains.kotlin.plugin.compose"
)

/**
 * Serialization plugin list — applied automatically by Hilt-bearing plugins (which always use
 * @Serializable nav args) and available separately for pure-compose modules that opt in via the
 * `appspiriment { enableSerialization.set(true) }` extension.
 */
val serializationPluginList: List<String> = listOf(
    "org.jetbrains.kotlin.plugin.serialization"
)

val hiltPluginList: List<String> = listOf(
    "com.google.dagger.hilt.android",
    "com.google.devtools.ksp",
    "org.jetbrains.kotlin.plugin.serialization"
)

/**
 * Hilt core dependencies.
 */
val hiltDependencies: List<Dependency> = listOf(
    Dependency(
        notation = "com.google.dagger:hilt-android",
        versionRef = "hilt"
    ),
    Dependency(
        notation = "org.jetbrains.kotlinx:kotlinx-serialization-json",
        versionRef = "kotlinserialize"
    ),
    Dependency(
        config = "ksp",
        notation = "com.google.dagger:hilt-android-compiler",
        versionRef = "hilt"
    )
)

/**
 * The Compose UI bundle (formerly the `android-compose` catalog bundle).
 *
 * Coordinates are hardcoded here; only versions are read from the consumer catalog
 * via [Dependency.versionRef]. Entries with no `versionRef` are pinned by the
 * Compose BOM platform added at the head of [composeDependencies].
 */
private val androidComposeBundle: List<Dependency> = listOf(
    Dependency(notation = "androidx.activity:activity-compose", versionRef = "activityCompose"),
    Dependency(notation = "androidx.compose.foundation:foundation"),
    Dependency(notation = "androidx.compose.ui:ui"),
    Dependency(notation = "androidx.compose.ui:ui-tooling-preview"),
    Dependency(notation = "androidx.compose.material:material"),
    Dependency(notation = "androidx.compose.material3:material3"),
    Dependency(notation = "androidx.compose.material:material-icons-core-android", versionRef = "material-icons"),
    Dependency(notation = "androidx.compose.material:material-icons-extended-android", versionRef = "material-icons"),
    Dependency(notation = "androidx.navigation:navigation-compose", versionRef = "composeNavigation"),
    Dependency(notation = "androidx.lifecycle:lifecycle-viewmodel-compose", versionRef = "lifecycle"),
    Dependency(notation = "androidx.lifecycle:lifecycle-runtime-compose", versionRef = "lifecycle"),
    Dependency(notation = "androidx.compose.ui:ui-text-google-fonts"),
    Dependency(notation = "org.jetbrains.kotlinx:kotlinx-serialization-json", versionRef = "kotlinserialize"),
    Dependency(notation = "com.airbnb.android:lottie-compose", versionRef = "lottie"),
)

/**
 * Compose dependencies.
 */
val composeDependencies: List<Dependency> = listOf(
    Dependency(
        type = ImplType.PLATFORM,
        notation = "androidx.compose:compose-bom",
        versionRef = "composeBom"
    ),
) + androidComposeBundle + listOf(
    // These androidx.compose.ui artifacts are pinned by the Compose BOM above — no versionRef.
    Dependency(
        config = "debugImplementation",
        notation = "androidx.compose.ui:ui-tooling"
    ),
    Dependency(
        config = "debugImplementation",
        notation = "androidx.compose.ui:ui-test-manifest"
    ),
    Dependency(
        type = ImplType.PLATFORM,
        config = "androidTestImplementation",
        notation = "androidx.compose:compose-bom",
        versionRef = "composeBom"
    ),
    Dependency(
        config = "androidTestImplementation",
        notation = "androidx.compose.ui:ui-test-junit4"
    ),
)

/**
 * Compose + Hilt glue. Added only when a module has BOTH the Hilt and Compose capabilities
 * (the application plugin and `library-hilt-compose`). Keeping it out of [composeDependencies]
 * means a non-Hilt Compose module (e.g. compose-utils) never pulls Hilt into its runtime graph.
 */
val hiltComposeDependencies: List<Dependency> = listOf(
    Dependency(
        notation = "androidx.hilt:hilt-navigation-compose",
        versionRef = "composeHiltNavigation"
    ),
)

/**
 * The core Android bundle (formerly the `android-base` catalog bundle).
 */
private val androidBaseBundle: List<Dependency> = listOf(
    Dependency(notation = "androidx.core:core-ktx", versionRef = "coreKtx"),
    Dependency(notation = "androidx.lifecycle:lifecycle-runtime-ktx", versionRef = "lifecycleRuntimeKtx"),
    Dependency(notation = "org.jetbrains.kotlinx:kotlinx-coroutines-android", versionRef = "kotlinxCoroutines"),
)

/**
 * The unit-test bundle (formerly the `unit-test` catalog bundle), added to `testImplementation`.
 */
private val unitTestBundle: List<Dependency> = listOf(
    Dependency(config = TEST_IMPLEMENTATION_CONFIGURATION_NAME, notation = "junit:junit", versionRef = "junit"),
    Dependency(config = TEST_IMPLEMENTATION_CONFIGURATION_NAME, notation = "org.mockito.kotlin:mockito-kotlin", versionRef = "mockito"),
    Dependency(config = TEST_IMPLEMENTATION_CONFIGURATION_NAME, notation = "org.jetbrains.kotlinx:kotlinx-coroutines-test", versionRef = "kotlinxCoroutines"),
    Dependency(config = TEST_IMPLEMENTATION_CONFIGURATION_NAME, notation = "app.cash.turbine:turbine", versionRef = "turbine"),
)

/**
 * Base dependencies added to every module.
 */
val baseDependencies: List<Dependency> = androidBaseBundle + unitTestBundle

/**
 * The Kolt BOM platform constraint.
 * Injected before individual util dependencies so Gradle's BOM resolution pins every
 * `io.github.appspiriment.kolt:*` artifact to the correct version automatically.
 * The BOM artifact coordinates are hardcoded; the version comes from [bomVersion]
 * baked into [Constants.kt] at build time.
 */
val bomDependency: List<Dependency> = listOf(
    Dependency(
        type = ImplType.PLATFORM,
        notation = "io.github.appspiriment.kolt:kolt-bom:$bomVersion"
    )
)

/**
 * Optional Kolt utility dependencies.
 * No explicit version — the BOM (added via [bomDependency]) constrains the version.
 */
val utilDependencies: List<Dependency> = listOf(
    Dependency(notation = "io.github.appspiriment.kolt:utils"),
    // Single logutils artifact (KMP). Debug/release behaviour is auto-gated at runtime
    // by logutils' own App Startup initializer (reads the app's debuggable flag); the
    // consumer can override via Log.enabled / Log.init(...).
    Dependency(notation = "io.github.appspiriment.kolt:logutils")
)

/**
 * Optional Compose utility dependencies.
 * No explicit version on Kolt artifacts — version comes from the BOM.
 */
val composeUtilDependencies: List<Dependency> = listOf(
    Dependency(notation = "io.github.appspiriment.kolt:compose"),
    Dependency(
        notation = "com.airbnb.android:lottie-compose",
        versionRef = "lottie"
    )
)

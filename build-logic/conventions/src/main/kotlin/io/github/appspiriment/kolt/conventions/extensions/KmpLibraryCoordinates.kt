package io.github.appspiriment.kolt.conventions.extensions

import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.DependencyHandlerScope

/**
 * All KMP library group:artifact coordinates are hardcoded here.
 * Versions are read from the kmplibs catalog via [getVersion].
 *
 * This mirrors the Android plugin suite's design: the plugin owns coordinates,
 * the consumer catalog owns only version strings.
 */

// ─────────────────────────────────────────────────────────────────────────────
// Coordinate constants
// ─────────────────────────────────────────────────────────────────────────────

private const val GROUP_KOTLINX = "org.jetbrains.kotlinx"
private const val GROUP_KOTLIN = "org.jetbrains.kotlin"
private const val GROUP_KOIN = "io.insert-koin"
private const val GROUP_KTOR = "io.ktor"
private const val GROUP_KTORFIT = "de.jensklingenberg.ktorfit"
private const val GROUP_SQLDELIGHT = "app.cash.sqldelight"
private const val GROUP_ROOM = "androidx.room"
private const val GROUP_SQLITE = "androidx.sqlite"
private const val GROUP_RETROFIT = "com.squareup.retrofit2"
private const val GROUP_OKHTTP = "com.squareup.okhttp3"
private const val GROUP_CHUCKER = "com.github.chuckerteam.chucker"
private const val GROUP_DATASTORE = "androidx.datastore"
private const val GROUP_NAVIGATION = "org.jetbrains.androidx.navigation"
private const val GROUP_APPSPIRIMENT = "io.github.appspiriment.kolt"
private const val GROUP_TURBINE = "app.cash.turbine"

// ─────────────────────────────────────────────────────────────────────────────
// Dependency builder helpers
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Adds a dependency using a hardcoded group:artifact and a version read from the catalog.
 */
internal fun DependencyHandlerScope.addKmpDep(
    config: String,
    group: String,
    artifact: String,
    version: String
) {
    add(config, "$group:$artifact:$version")
}

/**
 * Adds a dependency to all three canonical iOS target configurations.
 *
 * In KGP 2.x the intermediate `iosMainImplementation` configuration is created lazily
 * by the default hierarchy template and may not exist yet when convention plugin
 * afterEvaluate hooks run. The per-target configs (iosArm64MainImplementation etc.)
 * are created synchronously when the targets are declared and are always safe to use.
 */
internal fun DependencyHandlerScope.addKmpIosMainDep(
    group: String,
    artifact: String,
    version: String
) {
    val coord = "$group:$artifact:$version"
    add("iosArm64MainImplementation", coord)
    add("iosSimulatorArm64MainImplementation", coord)
    add("iosX64MainImplementation", coord)
}

/**
 * Adds multiple dependencies to the same configuration.
 */
internal fun DependencyHandlerScope.addKmpDeps(
    config: String,
    version: String,
    vararg coords: Pair<String, String>  // group to artifact
) {
    coords.forEach { (group, artifact) -> addKmpDep(config, group, artifact, version) }
}

// ─────────────────────────────────────────────────────────────────────────────
// Core dependencies
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpCoreDeps(libs: VersionCatalog) {
    val coroutinesVersion = libs.getVersion("coroutines")
    addKmpDep("commonMainImplementation", GROUP_KOTLINX, "kotlinx-coroutines-core", coroutinesVersion)
}

internal fun DependencyHandlerScope.addKmpTestDeps(libs: VersionCatalog) {
    val kotlinVersion = libs.getVersion("kotlin")
    val coroutinesVersion = libs.getVersion("coroutines")
    val turbineVersion = libs.getVersion("turbine")
    addKmpDep("commonTestImplementation", GROUP_KOTLIN, "kotlin-test", kotlinVersion)
    addKmpDep("commonTestImplementation", GROUP_KOTLINX, "kotlinx-coroutines-test", coroutinesVersion)
    addKmpDep("commonTestImplementation", GROUP_TURBINE, "turbine", turbineVersion)
}

// ─────────────────────────────────────────────────────────────────────────────
// Compose Multiplatform dependencies
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpComposeDeps(libs: VersionCatalog) {
    val cmpVersion = libs.getVersion("composeMultiplatform")
    val navVersion = libs.getVersion("navigationCompose")
    // CMP UI artifacts
    addKmpDeps(
        "commonMainImplementation", cmpVersion,
        "org.jetbrains.compose.runtime" to "runtime",
        "org.jetbrains.compose.foundation" to "foundation",
        "org.jetbrains.compose.material3" to "material3",
        "org.jetbrains.compose.ui" to "ui",
        "org.jetbrains.compose.components" to "components-resources"
    )
    // ui-tooling enables Compose Preview in Android Studio; added to androidMainImplementation
    // because KGP 2.3.10 does not create androidMainDebugImplementation for KMP libraries.
    addKmpDep("androidMainImplementation", "org.jetbrains.compose.ui", "ui-tooling", cmpVersion)
    // Official CMP navigation
    addKmpDep("commonMainImplementation", GROUP_NAVIGATION, "navigation-compose", navVersion)
}

// ─────────────────────────────────────────────────────────────────────────────
// Koin dependencies
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpKoinCommonDeps(libs: VersionCatalog) {
    val koinVersion = libs.getVersion("koin")
    val annotationsVersion = libs.getVersion("koinAnnotations")
    addKmpDeps("commonMainImplementation", koinVersion, GROUP_KOIN to "koin-core")
    addKmpDep("commonMainImplementation", GROUP_KOIN, "koin-annotations", annotationsVersion)
    addKmpDep("commonTestImplementation", GROUP_KOIN, "koin-test", koinVersion)
    addKmpDep("androidMainImplementation", GROUP_KOIN, "koin-android", koinVersion)
}

internal fun DependencyHandlerScope.addKmpKoinComposeDeps(libs: VersionCatalog) {
    val koinVersion = libs.getVersion("koin")
    addKmpDeps(
        "commonMainImplementation", koinVersion,
        GROUP_KOIN to "koin-compose",
        GROUP_KOIN to "koin-compose-viewmodel"
    )
}

internal fun DependencyHandlerScope.addKmpKoinKspCompiler(
    libs: VersionCatalog,
    enableIos: Boolean,
    enableDesktop: Boolean,
    enableWasm: Boolean = false
) {
    val version = libs.getVersion("koinAnnotations")
    val coord = "$GROUP_KOIN:koin-ksp-compiler:$version"
    add("kspAndroid", coord)
    if (enableIos) {
        add("kspIosArm64", coord)
        add("kspIosSimulatorArm64", coord)
        add("kspIosX64", coord)
    }
    if (enableDesktop) add("kspDesktop", coord)
    // WASM: Koin annotations KSP support for wasmJs is experimental; opt-in via enableWasm
    if (enableWasm) add("kspWasmJs", coord)
}

// ─────────────────────────────────────────────────────────────────────────────
// Ktor dependencies
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpKtorCommonDeps(libs: VersionCatalog) {
    val version = libs.getVersion("ktor")
    addKmpDeps(
        "commonMainImplementation", version,
        GROUP_KTOR to "ktor-client-core",
        GROUP_KTOR to "ktor-client-content-negotiation",
        GROUP_KTOR to "ktor-serialization-kotlinx-json"
    )
}

internal fun DependencyHandlerScope.addKmpKtorPlatformDeps(
    libs: VersionCatalog,
    enableIos: Boolean,
    enableDesktop: Boolean,
    enableWasm: Boolean,
    useLogging: Boolean
) {
    val version = libs.getVersion("ktor")
    addKmpDep("androidMainImplementation", GROUP_KTOR, "ktor-client-okhttp", version)
    if (enableIos) addKmpIosMainDep(GROUP_KTOR, "ktor-client-darwin", version)
    if (enableDesktop) addKmpDep("desktopMainImplementation", GROUP_KTOR, "ktor-client-cio", version)
    if (enableWasm) addKmpDep("wasmJsMainImplementation", GROUP_KTOR, "ktor-client-js", version)
    if (useLogging) addKmpDep("commonMainImplementation", GROUP_KTOR, "ktor-client-logging", version)
}

// ─────────────────────────────────────────────────────────────────────────────
// Ktorfit dependencies
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpKtorfitDeps(libs: VersionCatalog) {
    val version = libs.getVersion("ktorfit")
    addKmpDep("commonMainImplementation", GROUP_KTORFIT, "ktorfit-lib", version)
}

internal fun DependencyHandlerScope.addKmpKtorfitKspCompiler(
    libs: VersionCatalog,
    enableIos: Boolean,
    enableDesktop: Boolean,
    enableWasm: Boolean = false
) {
    val version = libs.getVersion("ktorfit")
    val coord = "$GROUP_KTORFIT:ktorfit-ksp:$version"
    add("kspAndroid", coord)
    if (enableIos) {
        add("kspIosArm64", coord)
        add("kspIosSimulatorArm64", coord)
        add("kspIosX64", coord)
    }
    if (enableDesktop) add("kspDesktop", coord)
    if (enableWasm) add("kspWasmJs", coord)
}

// ─────────────────────────────────────────────────────────────────────────────
// SQLDelight dependencies
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpSqlDelightDeps(
    libs: VersionCatalog,
    enableIos: Boolean,
    enableDesktop: Boolean
) {
    val version = libs.getVersion("sqlDelight")
    addKmpDep("commonMainImplementation", GROUP_SQLDELIGHT, "runtime", version)
    addKmpDep("androidMainImplementation", GROUP_SQLDELIGHT, "android-driver", version)
    if (enableIos) addKmpIosMainDep(GROUP_SQLDELIGHT, "native-driver", version)
    if (enableDesktop) addKmpDep("desktopMainImplementation", GROUP_SQLDELIGHT, "sqlite-driver", version)
}

// ─────────────────────────────────────────────────────────────────────────────
// Room 3.0 KMP dependencies
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpRoom3Deps(
    libs: VersionCatalog,
    enableIos: Boolean,
    enableDesktop: Boolean
    // Room 3 KMP does NOT support wasmJs — no kspWasmJs config added here.
) {
    val roomVersion = libs.getVersion("room3")
    val sqliteVersion = libs.getVersion("sqliteBundled")
    addKmpDep("commonMainImplementation", GROUP_ROOM, "room-runtime", roomVersion)
    // BundledSQLiteDriver is the recommended cross-platform driver for KMP Room.
    // Its version is independent of Room (tracked by sqliteBundled in kmplibs).
    addKmpDep("commonMainImplementation", GROUP_SQLITE, "sqlite-bundled", sqliteVersion)
    // KSP compiler per enabled target — no WASM (Room KMP does not support wasmJs)
    val compilerCoord = "$GROUP_ROOM:room-compiler:$roomVersion"
    add("kspAndroid", compilerCoord)
    if (enableIos) {
        add("kspIosArm64", compilerCoord)
        add("kspIosSimulatorArm64", compilerCoord)
        add("kspIosX64", compilerCoord)
    }
    if (enableDesktop) add("kspDesktop", compilerCoord)
}

// ─────────────────────────────────────────────────────────────────────────────
// DataStore dependencies
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpDataStoreDeps(libs: VersionCatalog) {
    // datastore-preferences-core is the KMP-compatible artifact.
    // Do NOT add the Android-only datastore-preferences artifact.
    addKmpDep("commonMainImplementation", GROUP_DATASTORE, "datastore-preferences-core", libs.getVersion("datastore"))
}

// ─────────────────────────────────────────────────────────────────────────────
// Serialization dependencies
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpSerializationDeps(libs: VersionCatalog) {
    addKmpDep("commonMainImplementation", GROUP_KOTLINX, "kotlinx-serialization-json", libs.getVersion("kotlinxSerialization"))
}

// ─────────────────────────────────────────────────────────────────────────────
// Kolt BOM platform constraint
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Adds the Kolt BOM as a platform constraint.
 * The BOM version is baked into the plugin JAR via [bomVersion] in Constants.kt.
 * After this, all `io.github.appspiriment.kolt:*` artifacts can be declared without
 * an explicit version — the BOM resolves them.
 */
internal fun DependencyHandlerScope.addAppspirimentBom(config: String = "implementation") {
    add(config, platform("$GROUP_APPSPIRIMENT:kolt-bom:$bomVersion"))
}

// ─────────────────────────────────────────────────────────────────────────────
// Kolt utility dependencies (no explicit versions — resolved via BOM)
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpUtilDeps(@Suppress("UNUSED_PARAMETER") libs: VersionCatalog) {
    // Explicit versions baked in so KMP desktop/iOS targets resolve without needing
    // the BOM constraint to propagate through KGP's metadata config hierarchy.
    add("commonMainImplementation", "$GROUP_APPSPIRIMENT:utils:$utilsVersion")
    add("commonMainImplementation", "$GROUP_APPSPIRIMENT:logutils:$logutilsVersion")
}

// ─────────────────────────────────────────────────────────────────────────────
// Application module utility dependencies (standard configs, not KMP source sets)
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpAppUtilDeps(@Suppress("UNUSED_PARAMETER") libs: VersionCatalog) {
    // Versions intentionally omitted — the BOM (added in KmpBaseConventionPlugin) pins them.
    add("implementation", "$GROUP_APPSPIRIMENT:utils")
    add("implementation", "$GROUP_APPSPIRIMENT:logutils")
}

// ─────────────────────────────────────────────────────────────────────────────
// Retrofit (Android-only) — for the androidMain source set of KMP modules
//
// Retrofit runs on Android/JVM only. In KMP data-layer modules use Ktor/Ktorfit
// for shared networking, and this only for Android-side-only overrides or when
// migrating an existing Android layer into a KMP module incrementally.
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpRetrofitDeps(
    libs: VersionCatalog,
    useChucker: Boolean,
    useKotlinSerialization: Boolean
) {
    val retrofitVersion = libs.getVersion("retrofit")
    val okhttpLoggingVersion = libs.getVersion("okhttpLogging")
    addKmpDep("androidMainImplementation", GROUP_RETROFIT, "retrofit", retrofitVersion)
    addKmpDep("androidMainImplementation", GROUP_OKHTTP, "logging-interceptor", okhttpLoggingVersion)

    if (useKotlinSerialization) {
        // retrofit2-kotlinx-serialization-converter shares the retrofit version
        addKmpDep("androidMainImplementation", GROUP_RETROFIT, "converter-kotlinx-serialization", retrofitVersion)
    } else {
        addKmpDep("androidMainImplementation", GROUP_RETROFIT, "converter-gson", retrofitVersion)
    }

    if (useChucker) {
        val chuckerVersion = libs.getVersion("chucker")
        add("debugImplementation", "$GROUP_CHUCKER:library:$chuckerVersion")
        add("releaseImplementation", "$GROUP_CHUCKER:library-no-op:$chuckerVersion")
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Kolt KMP Compose dependencies
// ─────────────────────────────────────────────────────────────────────────────

internal fun DependencyHandlerScope.addKmpComposeUtilsDeps(@Suppress("UNUSED_PARAMETER") libs: VersionCatalog) {
    add("commonMainImplementation", "$GROUP_APPSPIRIMENT:compose-kmp:$composeKmpVersion")
}


package io.github.appspiriment.kolt.conventions.extensions

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

internal const val KMP_EXTENSION_NAME = "kmp"
internal const val KMP_DATA_LAYER_EXTENSION_NAME = "kmpDataLayer"

/**
 * Extension for KMP convention plugins.
 * Exposed as the `kmp { }` block in module build scripts.
 *
 * Example usage:
 * ```kotlin
 * kmp {
 *     enableIos.set(true)
 *     enableDesktop.set(true)
 *     enableWasm.set(false)
 *     enableUtils.set(true)   // default: true
 *     enableMinify.set(false) // default: false (application modules only)
 * }
 * ```
 */
abstract class KmpExtension @Inject constructor(objects: ObjectFactory) {
    /** Activate iosArm64 + iosSimulatorArm64 + iosX64 targets. Default: false. */
    abstract val enableIos: Property<Boolean>

    /** Activate jvm target named "desktop". Default: false. */
    abstract val enableDesktop: Property<Boolean>

    /** Activate wasmJs target with browser execution. Default: false. */
    abstract val enableWasm: Property<Boolean>

    /** Enable R8 minification on release build type (application modules only). Default: false. */
    abstract val enableMinify: Property<Boolean>

    /** Add appspiriment-utils and logutils to the module. Default: true. */
    abstract val enableUtils: Property<Boolean>

    /**
     * Master switch for the debug `applicationId` suffix (KMP application module only).
     * When `true` (default), [debugApplicationIdSuffix] is appended to `applicationId` in
     * debug builds so the debug build installs alongside release. Independent of the
     * versionName timestamp ([appendTimestampToDebugVersion]).
     */
    abstract val addDevSuffixToDebug: Property<Boolean>

    /**
     * Suffix appended to `applicationId` in debug builds when [addDevSuffixToDebug] is
     * enabled. Default: `.dev`. Override with your own, e.g. `.qa`. KMP application module only.
     */
    abstract val debugApplicationIdSuffix: Property<String>

    /**
     * Whether to append a build timestamp to `versionName` in debug builds. Default: true.
     * Format controlled by [debugVersionTimestampPattern]. KMP application module only.
     */
    abstract val appendTimestampToDebugVersion: Property<Boolean>

    /**
     * [java.time.format.DateTimeFormatter] pattern for the debug `versionName` timestamp
     * when [appendTimestampToDebugVersion] is enabled. Default: `yyyyMMdd-HHmm`. An invalid
     * or blank pattern falls back to the default. KMP application module only.
     */
    abstract val debugVersionTimestampPattern: Property<String>
}

/**
 * Configuration extension for the `io.github.appspiriment.kolt.kmp.data` plugin.
 *
 * All features are opt-in — nothing is added unless explicitly enabled.
 *
 * Persistence options (choose one):
 * - `sqlDelight { }` — SQLDelight 2.x (stable, SQL-first, recommended for new KMP projects)
 * - `room { }` — Room 3.0 KMP (alpha, ORM-style, familiar to Android developers;
 *   does NOT support wasmJs target)
 *
 * Networking options (choose one or both):
 * - `ktor { }` — Raw Ktor Client (full KMP support — Android/iOS/Desktop/WASM)
 * - `ktorfit { }` — Ktorfit over Ktor (Retrofit-style annotations, KSP-generated; full KMP)
 * - `retrofit { }` — Retrofit + OkHttp added to **androidMainImplementation only**.
 *   Use when migrating an existing Android layer into KMP incrementally, or when the
 *   Android side needs a different client from shared targets.
 *
 * Other:
 * - `dataStore { }` — KMP DataStore preferences
 * - `serialization { }` — kotlinx.serialization standalone (without Ktor)
 *
 * Example usage:
 * ```kotlin
 * kmpDataLayer {
 *     sqlDelight { enabled.set(true) }
 *     ktor { enabled.set(true); useLogging.set(true) }
 *     // OR Retrofit-style KMP API client:
 *     ktorfit { enabled.set(true); useLogging.set(true) }
 *     // OR Android-only Retrofit (androidMainImplementation only):
 *     retrofit { enabled.set(true); useChucker.set(true) }
 *     dataStore { enabled.set(true) }
 *     serialization { enabled.set(true) }
 * }
 * ```
 */
abstract class KmpDataLayerExtension @Inject constructor(objects: ObjectFactory) {
    // ── Persistence ──────────────────────────────────────────────────────────

    /** SQLDelight 2.x — stable, SQL-first KMP persistence. */
    val sqlDelight: SqlDelightConfig = objects.newInstance(SqlDelightConfig::class.java)
    fun sqlDelight(action: Action<SqlDelightConfig>) = action.execute(sqlDelight)

    /**
     * Room 3.0 KMP — alpha, ORM-style, familiar to Android developers.
     * Does NOT support wasmJs. Use [sqlDelight] for full KMP persistence coverage.
     */
    val room: KmpRoomConfig = objects.newInstance(KmpRoomConfig::class.java)
    fun room(action: Action<KmpRoomConfig>) = action.execute(room)

    // ── Networking ───────────────────────────────────────────────────────────

    /** Raw Ktor Client — full KMP support (Android/iOS/Desktop/WASM), DSL-based. */
    val ktor: KtorConfig = objects.newInstance(KtorConfig::class.java)
    fun ktor(action: Action<KtorConfig>) = action.execute(ktor)

    /**
     * Ktorfit — Retrofit-style interface annotations over Ktor.
     * Requires KSP. Full KMP support. Implies Ktor is also added.
     */
    val ktorfit: KtorfitConfig = objects.newInstance(KtorfitConfig::class.java)
    fun ktorfit(action: Action<KtorfitConfig>) = action.execute(ktorfit)

    /**
     * Retrofit + OkHttp — added to **androidMainImplementation only**.
     * Retrofit is JVM/Android-only and cannot be used in commonMain or iOS/Desktop source sets.
     * For fully shared networking across all KMP targets, use [ktor] or [ktorfit] instead.
     */
    val retrofit: KmpRetrofitConfig = objects.newInstance(KmpRetrofitConfig::class.java)
    fun retrofit(action: Action<KmpRetrofitConfig>) = action.execute(retrofit)

    // ── Other ─────────────────────────────────────────────────────────────────

    /** KMP DataStore preferences (datastore-preferences-core only — not Android-only artifact). */
    val dataStore: SimpleConfig = objects.newInstance(SimpleConfig::class.java)
    fun dataStore(action: Action<SimpleConfig>) = action.execute(dataStore)

    /** kotlinx.serialization standalone (without Ktor/Ktorfit). */
    val serialization: SimpleConfig = objects.newInstance(SimpleConfig::class.java)
    fun serialization(action: Action<SimpleConfig>) = action.execute(serialization)
}

/**
 * Configuration for SQLDelight persistence.
 * Stable, SQL-first, recommended for greenfield KMP projects.
 */
abstract class SqlDelightConfig @Inject constructor(objects: ObjectFactory) {
    /** Enable SQLDelight. Adds runtime + platform-specific drivers. Default: false. */
    abstract val enabled: Property<Boolean>

    /** SQLDelight schema version. Default: 1. */
    abstract val schemaVersion: Property<Int>
}

/**
 * Configuration for Room 3.0 KMP persistence (androidx.room:room-runtime:3.x-alpha).
 * Alpha — ORM-style, familiar to Android developers. Does NOT support wasmJs.
 */
abstract class KmpRoomConfig @Inject constructor(objects: ObjectFactory) {
    /** Enable Room 3.0 KMP. Adds room-runtime, sqlite-bundled and room-compiler (KSP). Default: false. */
    abstract val enabled: Property<Boolean>

    /**
     * Schema version for the Room database. Passed to the Room KSP processor via
     * the `room.schemaVersion` argument. Default: 1.
     */
    abstract val schemaVersion: Property<Int>

    /**
     * Directory where Room exports schema JSON files. Relative paths are resolved
     * against the module's project directory. Set to empty string to disable export.
     * Default: "schemas" (projectDir/schemas).
     */
    abstract val schemaDirectory: Property<String>
}

/**
 * Configuration for Retrofit + OkHttp in the **androidMain** source set of a KMP module.
 *
 * Retrofit is JVM/Android-only and cannot be placed in commonMain. For full KMP networking
 * across all platforms use [KtorConfig] or [KtorfitConfig] instead.
 */
abstract class KmpRetrofitConfig @Inject constructor(objects: ObjectFactory) {
    /** Enable Retrofit. Adds retrofit, okhttp logging-interceptor to androidMainImplementation. Default: false. */
    abstract val enabled: Property<Boolean>

    /** Add Chucker as debugImplementation / library-no-op as releaseImplementation. Default: false. */
    abstract val useChucker: Property<Boolean>

    /**
     * Use kotlinx.serialization converter (retrofit2-kotlinx-serialization-converter)
     * instead of the default Gson converter. Also applies the serialization plugin.
     * Default: false.
     */
    abstract val useKotlinSerialization: Property<Boolean>
}

/**
 * Configuration for raw Ktor Client networking.
 */
abstract class KtorConfig @Inject constructor(objects: ObjectFactory) {
    /** Enable Ktor. Adds ktor-client-core + platform-specific engines. Default: false. */
    abstract val enabled: Property<Boolean>

    /** Also add ktor-client-logging to commonMain. Default: false. */
    abstract val useLogging: Property<Boolean>
}

/**
 * Configuration for Ktorfit — Retrofit-style interface annotations over Ktor.
 * Requires KSP. Automatically includes Ktor core dependencies.
 */
abstract class KtorfitConfig @Inject constructor(objects: ObjectFactory) {
    /**
     * Enable Ktorfit. Adds ktorfit-lib to commonMain, ktorfit-ksp to KSP configs,
     * and applies the Ktorfit Gradle plugin. Also adds Ktor core deps automatically.
     * Default: false.
     */
    abstract val enabled: Property<Boolean>

    /** Also add ktor-client-logging to commonMain. Default: false. */
    abstract val useLogging: Property<Boolean>
}

/**
 * Convenience extension function to configure the [KmpDataLayerExtension] from a build script.
 */
fun Project.kmpDataLayer(configure: Action<KmpDataLayerExtension>) {
    extensions.configure(KMP_DATA_LAYER_EXTENSION_NAME, configure)
}

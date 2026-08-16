package io.github.appspiriment.kolt.conventions.extensions

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

internal const val KOLT_EXTENSION_NAME = "kolt"
internal const val DATA_LAYER_EXTENSION_NAME = "dataLayer"

/**
 * Extension for Kolt convention plugins.
 * Exposed as the `appspiriment { }` block in module build scripts.
 *
 * Uses Gradle's Property API for lazy evaluation and configuration-cache compatibility.
 * Declared as an abstract class so Gradle can instantiate it via `extensions.create`
 * with full managed-property support (consistent with [DataLayerExtension]).
 *
 * Example usage:
 * ```kotlin
 * appspiriment {
 *     enableUtils.set(false)            // default: true
 *     enableMinify.set(true)            // default: false
 *     addDevSuffixToDebug.set(false)    // default: true (app modules only)
 *     debugApplicationIdSuffix.set(".qa")          // default: ".dev"
 *     appendTimestampToDebugVersion.set(false)     // default: true
 *     debugVersionTimestampPattern.set("ddMMyy-HHmm") // default: "yyyyMMdd.HHmm"
 *     scaffoldThemeResources.set(false) // default: true (app + compose modules only)
 * }
 * ```
 */
abstract class KoltExtension @Inject constructor(objects: ObjectFactory) {
    /** Whether to add appspiriment-utils, logutils-dev/prod. Default: true. */
    abstract val enableUtils: Property<Boolean>
    /** Whether to enable R8 minification in the release build type. Default: false. */
    abstract val enableMinify: Property<Boolean>
    /**
     * Master switch for the debug `applicationId` suffix. When `true` (default), the
     * value of [debugApplicationIdSuffix] is appended to `applicationId` in debug builds
     * (so the debug build installs alongside release). Application modules only.
     *
     * Note: this only governs the `applicationId` suffix. The `versionName` timestamp is
     * controlled independently by [appendTimestampToDebugVersion].
     */
    abstract val addDevSuffixToDebug: Property<Boolean>
    /**
     * The suffix appended to `applicationId` in debug builds when [addDevSuffixToDebug]
     * is enabled. Default: `.dev` (→ `com.example.app.dev`). Override with your own, e.g.
     * `.qa` or `.internal`. A leading dot is recommended but not required.
     * Application modules only.
     */
    abstract val debugApplicationIdSuffix: Property<String>
    /**
     * Whether to append a build timestamp to `versionName` in debug builds.
     * Default: true. Format is controlled by [debugVersionTimestampPattern].
     * Example: versionName `1.0.0.20260613.1430`. Application modules only.
     */
    abstract val appendTimestampToDebugVersion: Property<Boolean>
    /**
     * The [java.time.format.DateTimeFormatter] pattern used for the debug `versionName`
     * timestamp when [appendTimestampToDebugVersion] is enabled. Default: `yyyyMMdd.HHmm`.
     * An invalid or blank pattern falls back to the default. Application modules only.
     */
    abstract val debugVersionTimestampPattern: Property<String>

    /**
     * When `true` (default), the `scaffoldKoltResources` task is registered and
     * wired to `preBuild` for app modules that use Compose.  On first build it writes:
     *
     * - `src/main/res/values/appspiriment_colors.xml`         — day theme colours
     * - `src/main/res/values-night/appspiriment_colors.xml`   — night theme colours
     * - `src/main/res/values/appspiriment_dimens.xml`         — font sizes & spacing
     *
     * These files use the same resource names as the compose-utils AAR, so editing them
     * overrides the library's defaults app-wide (no Kotlin code changes required).
     *
     * **Files are never overwritten once they exist** — your edits are permanent.
     * Set to `false` to opt out entirely and manage theme resources by hand.
     */
    abstract val scaffoldThemeResources: Property<Boolean>
}

/**
 * Convenience extension function to configure the [DataLayerExtension] from a build script.
 *
 * Example usage:
 * ```kotlin
 * dataLayer {
 *     room { enabled.set(true) }
 *     retrofit { enabled.set(true); useChucker.set(true) }
 * }
 * ```
 */
fun Project.dataLayer(configure: Action<DataLayerExtension>) {
    extensions.configure(DATA_LAYER_EXTENSION_NAME, configure)
}

/**
 * Configuration extension for the `io.github.appspiriment.kolt.data` plugin.
 * Each nested block is opt-in — nothing is added unless explicitly enabled.
 */
abstract class DataLayerExtension @Inject constructor(objects: ObjectFactory) {
    val room: RoomConfig = objects.newInstance(RoomConfig::class.java)
    fun room(action: Action<RoomConfig>) = action.execute(room)

    val retrofit: RetrofitConfig = objects.newInstance(RetrofitConfig::class.java)
    fun retrofit(action: Action<RetrofitConfig>) = action.execute(retrofit)

    val security: SimpleConfig = objects.newInstance(SimpleConfig::class.java)
    fun security(action: Action<SimpleConfig>) = action.execute(security)

    val dataStore: SimpleConfig = objects.newInstance(SimpleConfig::class.java)
    fun dataStore(action: Action<SimpleConfig>) = action.execute(dataStore)

    val workManager: SimpleConfig = objects.newInstance(SimpleConfig::class.java)
    fun workManager(action: Action<SimpleConfig>) = action.execute(workManager)
}

/**
 * Configuration for Room persistence.
 * Declared as an abstract class so Gradle can instantiate it via [ObjectFactory.newInstance].
 */
abstract class RoomConfig @Inject constructor(objects: ObjectFactory) {
    /** Enable Room. Adds room-runtime, room-ktx (implementation) and room-compiler (ksp). */
    abstract val enabled: Property<Boolean>
    /** Also add room-paging for Paging 3 integration. */
    abstract val usePaging: Property<Boolean>
}

/**
 * Configuration for Retrofit networking.
 * Declared as an abstract class so Gradle can instantiate it via [ObjectFactory.newInstance].
 */
abstract class RetrofitConfig @Inject constructor(objects: ObjectFactory) {
    /** Enable Retrofit. Adds retrofit-core, okhttp, okhttp-logging, converter-gson. */
    abstract val enabled: Property<Boolean>
    /** Add Chucker as debugImplementation / no-op as releaseImplementation. */
    abstract val useChucker: Property<Boolean>
    /** Use kotlinx.serialization converter instead of Gson. Applies the serialization plugin. */
    abstract val useKotlinSerialization: Property<Boolean>
}

/**
 * Simple on/off configuration for a single feature.
 * Declared as an abstract class so Gradle can instantiate it via [ObjectFactory.newInstance].
 */
abstract class SimpleConfig @Inject constructor(objects: ObjectFactory) {
    abstract val enabled: Property<Boolean>
}

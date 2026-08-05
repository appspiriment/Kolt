package io.github.appspiriment.kolt.conventions.plugins

import io.github.appspiriment.kolt.conventions.extensions.applyKmpPluginFromLibs
import io.github.appspiriment.kolt.conventions.extensions.configureDesktopTarget
import io.github.appspiriment.kolt.conventions.extensions.configureKmpEarly
import io.github.appspiriment.kolt.conventions.extensions.kmpBasePluginList
import io.github.appspiriment.kolt.conventions.extensions.kmpLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Base convention plugin for KMP shared library modules.
 *
 * Subclasses opt in to Compose and/or Koin capabilities by overriding
 * [setupCompose] and [setupKoin] flags. This inheritance-based capability
 * pattern keeps each concrete plugin class minimal.
 *
 * The Android target is always enabled. iOS, Desktop, and WASM are opt-in
 * via the `kmp { }` DSL block.
 */
open class KmpLibraryConventionPlugin : KmpBaseConventionPlugin() {
    open val setupCompose: Boolean = false
    open val setupKoin: Boolean = false

    override fun apply(target: Project) {
        target.run {
            if (setupCompose) setupCompose()
            if (setupKoin) setupKoin()
            super.apply(this)
        }
    }
}

/**
 * KMP shared library module with Compose Multiplatform UI.
 * Applies `org.jetbrains.compose` and `org.jetbrains.kotlin.plugin.compose`.
 */
class KmpLibraryComposeConventionPlugin : KmpLibraryConventionPlugin() {
    override val setupCompose: Boolean = true

    override fun apply(target: Project) {
        target.run {
            // 1. Apply KMP + AGP first so the KotlinMultiplatformExtension exists.
            pluginManager.applyKmpPluginFromLibs(kmpLibs to kmpBasePluginList)

            // 2. Configure the android target and create the desktop target NOW.
            //    KGP registers desktopJar lazily as part of jvm("desktop").
            extensions.configure<KotlinMultiplatformExtension> {
                configureKmpEarly(this)
                configureDesktopTarget(this)
            }
        }
        super.apply(target)
    }
}

/**
 * KMP shared library module with Koin dependency injection.
 * Applies `com.google.devtools.ksp` and adds Koin artifacts.
 */
class KmpLibraryKoinConventionPlugin : KmpLibraryConventionPlugin() {
    override val setupKoin: Boolean = true
}

/**
 * KMP shared library module with both Koin DI and Compose Multiplatform UI.
 *
 * Compose Desktop Preview registers a `withType(KotlinJvmTarget)` container callback
 * (inside its `plugins.withId(KMP)` handler) that synchronously calls
 * `tasks.named("desktopJar")` when any KotlinJvmTarget is added.  KGP registers
 * `desktopJar` *after* the target-added notification fires, so the lookup fails when
 * `jvm("desktop")` is deferred to afterEvaluate.
 *
 * Fix: apply KMP+AGP and create the desktop target here — *before* Compose is applied.
 * When `setupCompose()` below applies `org.jetbrains.compose`, its `withPlugin(KMP)`
 * callback fires immediately (KMP already loaded) and `withType` sees the already-complete
 * "desktop" target whose `desktopJar` task KGP has already registered.
 *
 * [configureKmpEarly] and [configureDesktopTarget] are idempotent, so the duplicate
 * calls from [KmpBaseConventionPlugin.apply] are harmless no-ops.
 */
class KmpLibraryKoinComposeConventionPlugin : KmpLibraryConventionPlugin() {
    override val setupCompose: Boolean = true
    override val setupKoin: Boolean = true

    override fun apply(target: Project) {
        target.run {
            // 1. Apply KMP + AGP first so the KotlinMultiplatformExtension exists.
            pluginManager.applyKmpPluginFromLibs(kmpLibs to kmpBasePluginList)

            // 2. Configure the android target and create the desktop target NOW.
            //    KGP registers desktopJar lazily as part of jvm("desktop").
            extensions.configure<KotlinMultiplatformExtension> {
                configureKmpEarly(this)
                configureDesktopTarget(this)
            }
        }

        // 3. Parent apply: setupCompose fires immediately (KMP already applied), so
        //    Compose's withType(KotlinJvmTarget) sees the existing "desktop" target and
        //    tasks.named("desktopJar") succeeds.  setupKoin + KmpBaseConventionPlugin
        //    handle the rest; their calls to configureKmpEarly / configureDesktopTarget
        //    are no-ops because both targets already exist.
        super.apply(target)
    }
}

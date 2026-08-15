package io.github.appspiriment.kolt.conventions.plugins

import io.github.appspiriment.kolt.conventions.extensions.KMP_EXTENSION_NAME
import io.github.appspiriment.kolt.conventions.extensions.KmpExtension
import io.github.appspiriment.kolt.conventions.extensions.addAppspirimentBom
import io.github.appspiriment.kolt.conventions.extensions.addKmpComposeDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpComposeUtilsDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpCoreDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpKoinCommonDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpKoinComposeDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpKoinKspCompiler
import io.github.appspiriment.kolt.conventions.extensions.addKmpTestDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpUtilDeps
import io.github.appspiriment.kolt.conventions.extensions.applyKmpPluginFromLibs
import io.github.appspiriment.kolt.conventions.extensions.configureKmpEarly
import io.github.appspiriment.kolt.conventions.extensions.configureKmpLate
import io.github.appspiriment.kolt.conventions.extensions.kmpBasePluginList
import io.github.appspiriment.kolt.conventions.extensions.kmpComposePluginList
import io.github.appspiriment.kolt.conventions.extensions.kmpKoinPluginList
import io.github.appspiriment.kolt.conventions.extensions.kmpLibs
import io.github.appspiriment.kolt.conventions.extensions.minAgpVersion
import io.github.appspiriment.kolt.conventions.extensions.minComposeMultiplatformVersion
import io.github.appspiriment.kolt.conventions.extensions.minJavaVersion
import io.github.appspiriment.kolt.conventions.extensions.minKotlinVersion
import io.github.appspiriment.kolt.conventions.extensions.requireAtLeast
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// ─────────────────────────────────────────────────────────────────────────────
// Capability enum — tracks which optional features a plugin has activated.
// ─────────────────────────────────────────────────────────────────────────────
enum class KmpPluginCapability { KOIN, COMPOSE }

abstract class KmpBaseConventionPlugin : Plugin<Project> {

    private val capabilities = mutableSetOf<KmpPluginCapability>()

    override fun apply(target: Project) {
        with(target) {
            // 0. Hard-fail fast if this project's catalog has been downgraded below what
            // Kolt's convention plugins are built and tested against. Consumers are always
            // free to raise these — only downgrading is rejected. See VersionFloor.kt.
            requireAtLeast(kmpLibs, "agp", minAgpVersion, target.name)
            requireAtLeast(kmpLibs, "kotlin", minKotlinVersion, target.name)
            requireAtLeast(kmpLibs, "javaVersion", minJavaVersion, target.name)
            if (KmpPluginCapability.COMPOSE in capabilities) {
                requireAtLeast(
                    kmpLibs, "composeMultiplatform", minComposeMultiplatformVersion, target.name
                )
            }

            // 1. Register kmp { } extension early
            extensions.create(KMP_EXTENSION_NAME, KmpExtension::class.java)

            // 2. Apply mandatory plugins (kotlin-multiplatform, android-library)
            pluginManager.applyKmpPluginFromLibs(kmpLibs to kmpBasePluginList)

            // 3. Configure Android target early (compileSdk, minSdk, jvmTarget)
            extensions.configure<KotlinMultiplatformExtension> {
                configureKmpEarly(this)
            }

            // 4. Add the Kolt BOM so all io.github.appspiriment.kolt:* deps resolve without
            //    explicit versions. Added to `implementation` config which is visible across
            //    all KMP source sets when resolved via the android target.
            dependencies {
                addAppspirimentBom()
                addKmpCoreDeps(kmpLibs)
                addKmpTestDeps(kmpLibs)
            }

            // 4b. Scaffold / update both Kolt + KMP version catalogs in gradle/.
            // includeKmp = true ensures kmplibs.versions.toml is also scaffolded/updated.
            registerCatalogScaffold(includeKmp = true)

            // 5. Defer everything that depends on user config to afterEvaluate
            afterEvaluate {
                val kmpExt = extensions.getByType<KmpExtension>()

                // Activate optional targets (iOS, Desktop, WASM)
                extensions.configure<KotlinMultiplatformExtension> {
                    configureKmpLate(this, kmpExt)
                }

                val enableIos = kmpExt.enableIos.getOrElse(false)
                val enableDesktop = kmpExt.enableDesktop.getOrElse(false)

                dependencies {
                    // Compose Multiplatform UI + navigation
                    if (KmpPluginCapability.COMPOSE in capabilities) {
                        addKmpComposeDeps(kmpLibs)
                    }

                    // Koin DI + per-target KSP
                    if (KmpPluginCapability.KOIN in capabilities) {
                        addKmpKoinCommonDeps(kmpLibs)
                        addKmpKoinKspCompiler(kmpLibs, enableIos, enableDesktop)

                        // Koin Compose extras when both capabilities are active
                        if (KmpPluginCapability.COMPOSE in capabilities) {
                            addKmpKoinComposeDeps(kmpLibs)
                        }
                    }

                    // Kolt utility libraries
                    if (kmpExt.enableUtils.getOrElse(true)) {
                        addKmpUtilDeps(kmpLibs)
                        if (KmpPluginCapability.COMPOSE in capabilities) {
                            addKmpComposeUtilsDeps(kmpLibs)
                        }
                    }
                }
            }
        }
    }

    protected fun Project.setupCompose() {
        capabilities += KmpPluginCapability.COMPOSE
        pluginManager.applyKmpPluginFromLibs(kmpLibs to kmpComposePluginList)
    }

    protected fun Project.setupKoin() {
        capabilities += KmpPluginCapability.KOIN
        pluginManager.applyKmpPluginFromLibs(kmpLibs to kmpKoinPluginList)
    }

    internal fun hasCapability(cap: KmpPluginCapability) = cap in capabilities
}

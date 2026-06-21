package io.github.appspiriment.kolt.conventions.plugins.feature

import io.github.appspiriment.kolt.conventions.extensions.KMP_DATA_LAYER_EXTENSION_NAME
import io.github.appspiriment.kolt.conventions.extensions.KmpDataLayerExtension
import io.github.appspiriment.kolt.conventions.extensions.KmpExtension
import io.github.appspiriment.kolt.conventions.extensions.addKmpDataStoreDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpKtorCommonDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpKtorPlatformDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpKtorfitDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpKtorfitKspCompiler
import io.github.appspiriment.kolt.conventions.extensions.addKmpRetrofitDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpRoom3Deps
import io.github.appspiriment.kolt.conventions.extensions.addKmpSerializationDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpSqlDelightDeps
import io.github.appspiriment.kolt.conventions.extensions.applyKmpPluginFromLibs
import io.github.appspiriment.kolt.conventions.extensions.kmpKtorfitPluginList
import io.github.appspiriment.kolt.conventions.extensions.kmpLibs
import io.github.appspiriment.kolt.conventions.extensions.kmpSerializationPluginList
import io.github.appspiriment.kolt.conventions.extensions.kmpSqlDelightPluginList
import io.github.appspiriment.kolt.conventions.plugins.KmpBaseConventionPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Convention plugin for KMP data layer modules.
 *
 * All features are opt-in via `kmpDataLayer { }`. Nothing is added unless explicitly enabled.
 *
 * **Persistence** (choose one):
 * - `sqlDelight { enabled.set(true) }` — SQLDelight 2.x (stable, SQL-first; full KMP)
 * - `room { enabled.set(true); schemaVersion.set(1) }` — Room 3.0 KMP (alpha, ORM-style;
 *   no wasmJs support; schemaDirectory defaults to "schemas")
 *
 * **Networking** (choose one or more):
 * - `ktor { enabled.set(true) }` — Raw Ktor Client (DSL-based; full KMP)
 * - `ktorfit { enabled.set(true) }` — Ktorfit over Ktor (Retrofit-style annotations + KSP; full KMP)
 * - `retrofit { enabled.set(true) }` — Retrofit + OkHttp (**androidMainImplementation only**)
 *
 * **Other**:
 * - `dataStore { enabled.set(true) }` — KMP DataStore (datastore-preferences-core only)
 * - `serialization { enabled.set(true) }` — kotlinx.serialization standalone
 */
class KmpDataLayerConventionPlugin : KmpBaseConventionPlugin() {

    override fun apply(target: Project) {
        with(target) {
            extensions.create<KmpDataLayerExtension>(KMP_DATA_LAYER_EXTENSION_NAME)
            super.apply(this)

            afterEvaluate {
                val kmpExt = extensions.getByType<KmpExtension>()
                val dataConfig = extensions.getByType<KmpDataLayerExtension>()
                val libs = kmpLibs

                val enableIos = kmpExt.enableIos.getOrElse(false)
                val enableDesktop = kmpExt.enableDesktop.getOrElse(false)
                val enableWasm = kmpExt.enableWasm.getOrElse(false)

                var serializationPluginApplied = false

                dependencies {

                    // ── SQLDelight ────────────────────────────────────────────
                    if (dataConfig.sqlDelight.enabled.getOrElse(false)) {
                        pluginManager.applyKmpPluginFromLibs(libs to kmpSqlDelightPluginList)
                        addKmpSqlDelightDeps(libs, enableIos, enableDesktop)
                        extensions.findByName("sqldelight")?.let { ext ->
                            try {
                                ext.javaClass.getMethod("linkSqlite", Boolean::class.javaPrimitiveType)
                                    .invoke(ext, true)
                            } catch (_: Exception) { /* version-dependent API */ }
                        }
                    }

                    // ── Room 3.0 KMP ──────────────────────────────────────────
                    // Note: Room 3 KMP does NOT support wasmJs. WASM target is intentionally
                    // excluded from KSP config inside addKmpRoom3Deps.
                    if (dataConfig.room.enabled.getOrElse(false)) {
                        if (!pluginManager.hasPlugin("com.google.devtools.ksp")) {
                            pluginManager.apply("com.google.devtools.ksp")
                        }
                        addKmpRoom3Deps(libs, enableIos, enableDesktop)

                        // Configure Room schema export via KSP arguments
                        val schemaDir = dataConfig.room.schemaDirectory.getOrElse("schemas")
                        if (schemaDir.isNotEmpty()) {
                            extensions.findByName("ksp")?.let { kspExt ->
                                try {
                                    val argMethod = kspExt.javaClass.getMethod("arg", String::class.java, String::class.java)
                                    argMethod.invoke(kspExt, "room.schemaLocation", "${projectDir}/$schemaDir")
                                } catch (_: Exception) { /* ksp extension API may vary */ }
                            }
                        }
                    }

                    // ── Ktor ──────────────────────────────────────────────────
                    if (dataConfig.ktor.enabled.getOrElse(false)) {
                        if (!serializationPluginApplied) {
                            pluginManager.applyKmpPluginFromLibs(libs to kmpSerializationPluginList)
                            serializationPluginApplied = true
                        }
                        addKmpKtorCommonDeps(libs)
                        addKmpKtorPlatformDeps(
                            libs, enableIos, enableDesktop, enableWasm,
                            dataConfig.ktor.useLogging.getOrElse(false)
                        )
                    }

                    // ── Ktorfit ───────────────────────────────────────────────
                    if (dataConfig.ktorfit.enabled.getOrElse(false)) {
                        pluginManager.applyKmpPluginFromLibs(libs to kmpKtorfitPluginList)
                        if (!serializationPluginApplied) {
                            pluginManager.applyKmpPluginFromLibs(libs to kmpSerializationPluginList)
                            serializationPluginApplied = true
                        }
                        // Ktor core is required by Ktorfit
                        addKmpKtorCommonDeps(libs)
                        addKmpKtorPlatformDeps(
                            libs, enableIos, enableDesktop, enableWasm,
                            dataConfig.ktorfit.useLogging.getOrElse(false)
                        )
                        addKmpKtorfitDeps(libs)
                        addKmpKtorfitKspCompiler(libs, enableIos, enableDesktop, enableWasm)
                    }

                    // ── Retrofit (Android-only) ───────────────────────────────
                    if (dataConfig.retrofit.enabled.getOrElse(false)) {
                        if (dataConfig.retrofit.useKotlinSerialization.getOrElse(false) && !serializationPluginApplied) {
                            pluginManager.applyKmpPluginFromLibs(libs to kmpSerializationPluginList)
                            serializationPluginApplied = true
                        }
                        addKmpRetrofitDeps(
                            libs,
                            useChucker = dataConfig.retrofit.useChucker.getOrElse(false),
                            useKotlinSerialization = dataConfig.retrofit.useKotlinSerialization.getOrElse(false)
                        )
                    }

                    // ── DataStore ─────────────────────────────────────────────
                    if (dataConfig.dataStore.enabled.getOrElse(false)) {
                        addKmpDataStoreDeps(libs)
                    }

                    // ── Serialization (standalone) ────────────────────────────
                    if (dataConfig.serialization.enabled.getOrElse(false)) {
                        if (!serializationPluginApplied) {
                            pluginManager.applyKmpPluginFromLibs(libs to kmpSerializationPluginList)
                            serializationPluginApplied = true
                        }
                        addKmpSerializationDeps(libs)
                    }
                }
            }
        }
    }
}

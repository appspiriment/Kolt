package io.github.appspiriment.kolt.conventions.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import io.github.appspiriment.kolt.conventions.extensions.DEFAULT_DEBUG_VERSION_TIMESTAMP_PATTERN
import io.github.appspiriment.kolt.conventions.extensions.KMP_EXTENSION_NAME
import io.github.appspiriment.kolt.conventions.extensions.KmpExtension
import io.github.appspiriment.kolt.conventions.extensions.addAppspirimentBom
import io.github.appspiriment.kolt.conventions.extensions.addKmpAppUtilDeps
import io.github.appspiriment.kolt.conventions.extensions.addKmpDep
import io.github.appspiriment.kolt.conventions.extensions.applyKmpPluginFromLibs
import io.github.appspiriment.kolt.conventions.extensions.buildDateSuffix
import io.github.appspiriment.kolt.conventions.extensions.getVersion
import io.github.appspiriment.kolt.conventions.extensions.kmpLibs
import io.github.appspiriment.kolt.conventions.extensions.kmpProjectConfigs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Convention plugin for the Android host application module of a KMP project.
 *
 * Standalone — does NOT extend [KmpBaseConventionPlugin].
 *
 * Always applies:
 * - com.android.application
 * - org.jetbrains.kotlin.android
 * - org.jetbrains.compose (Compose Multiplatform)
 * - org.jetbrains.kotlin.plugin.compose
 * - kotlinx-coroutines-android
 * - Compose Multiplatform runtime/ui/material3/foundation
 *
 * Opt-in via `kmp { }`:
 * - enableMinify: enables R8 on release (default: false)
 * - enableUtils: adds appspiriment-utils/logutils (default: true)
 * - addDevSuffixToDebug + debugApplicationIdSuffix: appends a suffix (default `.dev`)
 *   to the debug applicationId so it installs alongside release (default: true)
 * - appendTimestampToDebugVersion + debugVersionTimestampPattern: appends a build
 *   timestamp (default `yyyyMMdd.HHmm`) to the debug versionName (default: true)
 */
class KmpApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            extensions.create(KMP_EXTENSION_NAME, KmpExtension::class.java)

            pluginManager.applyKmpPluginFromLibs(
                kmpLibs to listOf("android-application"),
                kmpLibs to listOf("kotlin-android"),
                kmpLibs to listOf("compose-multiplatform", "kotlin-compose-compiler")
            )

            extensions.configure<ApplicationExtension> {
                compileSdk = kmpProjectConfigs.compileSdk
                defaultConfig {
                    minSdk = kmpProjectConfigs.minSdk
                    targetSdk = kmpProjectConfigs.compileSdk
                    versionCode = kmpLibs.getVersion("versionCode").toInt()
                    versionName = kmpLibs.getVersion("versionName")
                }
                compileOptions {
                    sourceCompatibility = kmpProjectConfigs.javaVersion
                    targetCompatibility = kmpProjectConfigs.javaVersion
                }
            }

            // Debug applicationId suffix + versionName timestamp via the Variant API.
            // Each is independently toggleable through the `kmp { }` extension.
            extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
                ?.onVariants { variant ->
                    if (variant.buildType == "debug") {
                        val kmpExt = extensions.getByType<KmpExtension>()

                        if (kmpExt.addDevSuffixToDebug.getOrElse(true)) {
                            val suffix = kmpExt.debugApplicationIdSuffix.getOrElse(".dev")
                            if (suffix.isNotEmpty()) {
                                // Read base applicationId from defaultConfig (eagerly set by the
                                // module's android {} block) instead of mapping the variant property
                                // back to itself, which causes a CircularEvaluationException in
                                // Gradle 8.13+ where all providers are fully lazy.
                                val baseId = extensions.getByType<ApplicationExtension>()
                                    .defaultConfig.applicationId ?: ""
                                variant.applicationId.set("$baseId$suffix")
                            }
                        }

                        if (kmpExt.appendTimestampToDebugVersion.getOrElse(true)) {
                            val pattern = kmpExt.debugVersionTimestampPattern
                                .getOrElse(DEFAULT_DEBUG_VERSION_TIMESTAMP_PATTERN)
                            val baseVersion = extensions.getByType<ApplicationExtension>()
                                .defaultConfig.versionName ?: ""
                            variant.outputs.forEach { output ->
                                output.versionName.set("$baseVersion${buildDateSuffix(pattern)}")
                            }
                        }
                    }
                }

            dependencies {
                // BOM pins all io.github.appspiriment.kolt:* versions so utils/logutils can be
                // declared without explicit version strings (resolved via addKmpAppUtilDeps).
                addAppspirimentBom("implementation")

                val coroutinesVersion = kmpLibs.getVersion("coroutines")
                addKmpDep("implementation", "org.jetbrains.kotlinx", "kotlinx-coroutines-android", coroutinesVersion)

                // Compose Multiplatform — individual artifacts (no standalone CMP BOM)
                val cmpVersion = kmpLibs.getVersion("composeMultiplatform")
                addKmpDep("implementation", "org.jetbrains.compose.runtime", "runtime", cmpVersion)
                addKmpDep("implementation", "org.jetbrains.compose.ui", "ui", cmpVersion)
                addKmpDep("implementation", "org.jetbrains.compose.material3", "material3", cmpVersion)
                addKmpDep("implementation", "org.jetbrains.compose.foundation", "foundation", cmpVersion)
                addKmpDep("debugImplementation", "org.jetbrains.compose.ui", "ui-tooling", cmpVersion)
            }

            afterEvaluate {
                val kmpExt = extensions.getByType<KmpExtension>()

                if (kmpExt.enableMinify.getOrElse(false)) {
                    extensions.configure<ApplicationExtension> {
                        buildTypes.getByName("release") { isMinifyEnabled = true }
                    }
                }

                if (kmpExt.enableUtils.getOrElse(true)) {
                    dependencies { addKmpAppUtilDeps(kmpLibs) }
                }
            }
        }
    }
}

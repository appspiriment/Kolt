package io.github.appspiriment.kolt.conventions.extensions

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Accessor for the `kmplibs` version catalog.
 * The catalog name `kmpTomlName` is defined in the generated KmpConstants.kt.
 */
val Project.kmpLibs
    get() = extensions.getByType<VersionCatalogsExtension>().named(kmpTomlName)

/**
 * Returns the [KmpProjectConfiguration] for this project, computing it once and caching
 * the result in the project's extra properties. This avoids repeated catalog lookups
 * on every access.
 */
val Project.kmpProjectConfigs: KmpProjectConfiguration
    get() {
        val key = "kmp.projectConfigs"
        return if (extensions.extraProperties.has(key)) {
            @Suppress("UNCHECKED_CAST")
            extensions.extraProperties.get(key) as KmpProjectConfiguration
        } else {
            val config = kmpLibs.run {
                KmpProjectConfiguration(
                    minSdk = getVersion("minSdk").toInt(),
                    compileSdk = getVersion("compileSdk").toInt(),
                    javaVersion = JavaVersion.toVersion(getVersion("javaVersion").toInt())
                )
            }
            extensions.extraProperties.set(key, config)
            config
        }
    }

/**
 * SDK and tooling configuration read from the KMP version catalog.
 * Note: no targetSdk field — KMP library modules don't set targetSdk.
 * The application plugin reads targetSdk directly from the catalog.
 */
data class KmpProjectConfiguration(
    val minSdk: Int,
    val compileSdk: Int,
    val javaVersion: JavaVersion
)

package io.github.appspiriment.kolt.conventions.extensions

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.koltLibs
    get() = extensions.getByType<VersionCatalogsExtension>().named(koltTomlName)

/**
 * Returns the [ProjectConfiguration] for this project, computing it once and caching
 * the result in the project's extra properties. This avoids repeated catalog lookups
 * on every access — [koltLibs] itself calls [VersionCatalogsExtension.named]
 * each time, so caching here is meaningful for plugins that read configs frequently.
 */
val Project.projectConfigs: ProjectConfiguration
    get() {
        val key = "kolt.projectConfigs"
        return if (extensions.extraProperties.has(key)) {
            @Suppress("UNCHECKED_CAST")
            extensions.extraProperties.get(key) as ProjectConfiguration
        } else {
            val config = koltLibs.run {
                ProjectConfiguration(
                    minSdk = getVersion("minSdk").toInt(),
                    targetSdk = getVersion("targetSdk").toInt(),
                    compileSdk = getVersion("compileSdk").toInt(),
                    javaVersion = JavaVersion.toVersion(getVersion("javaVersion").toInt()),
                    versionCode = getVersion("versionCode").toInt(),
                    versionName = getVersion("versionName"),
                )
            }
            extensions.extraProperties.set(key, config)
            config
        }
    }

data class ProjectConfiguration(
    val minSdk: Int,
    val targetSdk: Int,
    val compileSdk: Int,
    val javaVersion: JavaVersion,
    /** Application modules only — read from `versionCode` in the catalog. */
    val versionCode: Int,
    /** Application modules only — read from `versionName` in the catalog. */
    val versionName: String,
)

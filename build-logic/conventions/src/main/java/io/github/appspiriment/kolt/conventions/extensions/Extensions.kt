package io.github.appspiriment.kolt.conventions.extensions

import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.plugins.PluginManager
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project
import kotlin.jvm.optionals.getOrElse

/**
 * Safely retrieves a version from the catalog or throws a descriptive error.
 */
internal fun VersionCatalog.getVersion(alias: String): String {
    return findVersion(alias).getOrElse {
        throw IllegalStateException("Version alias '$alias' not found in catalog '${this.name}'")
    }.toString()
}

/**
 * Applies plugins from the catalog. Throws if any plugin is missing.
 */
internal fun PluginManager.applyPluginFromLibs(vararg pluginGroups: Pair<VersionCatalog, List<String>>) {
    pluginGroups.forEach { (catalog, ids) ->
        ids.forEach { id ->
            // Try to find the version for this id if it's a known plugin
            val version = catalog.findVersion(id.substringAfterLast(".")).map { it.toString() }
                .orElseGet {
                    // Fallback to searching by alias if the ID matches an alias
                    catalog.findPlugin(id).map { it.get().version.displayName }.orElse(null)
                }

            if (!hasPlugin(id)) {
                apply(id)
            }
        }
    }
}

/**
 * Applies a list of dependencies.
 * If versionRef is provided, it constructs "group:name:version" using the catalog.
 * Otherwise, it looks up the notation as an alias in the catalog.
 */
internal fun DependencyHandlerScope.implementDependency(
    libs: VersionCatalog,
    dependencyList: List<Dependency>
) {
    dependencyList.forEach { dep ->
        val notation = dep.notation
        val versionRef = dep.versionRef

        when (dep.type) {
            ImplType.PROJECT -> {
                if (notation is String) {
                    add(dep.config, project(notation))
                }
            }
            ImplType.BUNDLE -> {
                if (notation is String) {
                    val bundle = libs.findBundle(notation).getOrElse {
                        throw IllegalStateException("Bundle '$notation' not found in catalog '${libs.name}'")
                    }
                    add(dep.config, bundle)
                }
            }
            else -> {
                val dependency: Any = if (versionRef != null && notation is String) {
                    val version = libs.findVersion(versionRef).getOrElse {
                        throw IllegalStateException("Version '$versionRef' not found in catalog '${libs.name}'")
                    }.toString()
                    "$notation:$version"
                } else if (notation is String) {
                    // Try alias lookup
                    libs.findLibrary(notation).map { it.get() as Any }.orElse(notation)
                } else {
                    notation
                }

                val finalDep = if (dep.type == ImplType.PLATFORM) platform(dependency) else dependency
                add(dep.config, finalDep)
            }
        }
    }
}

data class Dependency(
    val type: ImplType = ImplType.DEPENDENCY,
    val config: String = IMPLEMENTATION_CONFIGURATION_NAME,
    /**
     * The notation for the dependency.
     * Can be:
     * 1. A string in "group:name" format (version will be appended from catalog).
     * 2. A string alias (lookup in catalog).
     * 3. A Project object (for ImplType.PROJECT).
     */
    val notation: Any,
    /**
     * The key in the [versions] section of the TOML file.
     * If provided, the dependency is assumed to be "group:name" and the version is appended.
     */
    val versionRef: String? = null
)

enum class ImplType { BUNDLE, DEPENDENCY, PROJECT, PLATFORM }

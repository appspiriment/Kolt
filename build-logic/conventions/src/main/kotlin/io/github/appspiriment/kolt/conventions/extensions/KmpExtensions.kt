package io.github.appspiriment.kolt.conventions.extensions

import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.plugins.PluginManager

/**
 * Applies plugins from a KMP version catalog by alias. Throws if any alias is missing.
 * Guards against duplicate application via [PluginManager.hasPlugin].
 *
 * Distinct from the Android [applyPluginFromLibs] (which resolves by plugin id):
 * KMP convention plugins pass catalog aliases, so this resolves each alias to its
 * plugin id before applying.
 */
internal fun PluginManager.applyKmpPluginFromLibs(vararg pluginGroups: Pair<VersionCatalog, List<String>>) {
    pluginGroups.forEach { (catalog, aliases) ->
        aliases.forEach { alias ->
            val plugin = catalog.findPlugin(alias).orElseThrow {
                IllegalStateException("Plugin alias '$alias' not found in Version Catalog '${catalog.name}'")
            }.get()

            if (!hasPlugin(plugin.pluginId)) {
                apply(plugin.pluginId)
            }
        }
    }
}

package io.github.appspiriment.kolt.conventions.plugins

import io.github.appspiriment.kolt.conventions.extensions.koltTomlContents
import io.github.appspiriment.kolt.conventions.extensions.koltTomlName
import io.github.appspiriment.kolt.conventions.extensions.kmpTomlContents
import io.github.appspiriment.kolt.conventions.extensions.kmpTomlName
import io.github.appspiriment.kolt.conventions.extensions.libVersion
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Gradle task that scaffolds and incrementally updates Kolt version catalogs
 * (`koltlibs.versions.toml` and optionally `kmplibs.versions.toml`) into the
 * consumer project's `gradle/` directory.
 *
 * ## Behaviour
 *
 * | Situation                              | What the task does                              |
 * |----------------------------------------|-------------------------------------------------|
 * | File absent                            | Writes the full bundled TOML (initial scaffold) |
 * | File exists, plugin version unchanged  | Nothing — Gradle UP-TO-DATE skips the task      |
 * | File exists, plugin version changed    | 1. Updates all `appspiriment*` version values   |
 * |                                        | 2. Adds new keys present in bundled TOML but    |
 * |                                        |    not yet in consumer file (per section)        |
 * |                                        | 3. Never removes or modifies consumer entries   |
 *
 * ## Owned version keys
 *
 * A version key is "owned" by this plugin (and therefore updated on every version change)
 * if its name starts with `appspiriment` (case-insensitive). All other version keys — SDK
 * levels, third-party library versions, etc. — are left entirely to the consumer.
 *
 * ## New entries
 *
 * When we add a new library, plugin, or bundle to the catalog, its key will be present in
 * the bundled TOML but absent in the consumer's file. The task inserts those new lines at
 * the end of the relevant section, preceded by a comment marking them as new:
 * ```toml
 * # Added by kolt plugin v0.0.15
 * new-library = { group = "...", name = "...", version.ref = "..." }
 * ```
 *
 * ## KMP support
 *
 * When [includeKmp] is `true` (set by [KmpBaseConventionPlugin]) the task also scaffolds /
 * merges `kmplibs.versions.toml` with the same rules.
 *
 * ## Registration
 *
 * This task is registered **once on the root project** so it runs a single time per build
 * regardless of how many modules apply an Kolt plugin.  It is wired before
 * `preBuild` of whichever module first triggers registration.
 */
abstract class ScaffoldVersionCatalogTask : DefaultTask() {

    /** Current plugin version — drives the UP-TO-DATE check and the owned version values. */
    @get:Input
    abstract val pluginVersion: Property<String>

    /** Absolute path to the root project directory (where `gradle/` lives). */
    @get:Input
    abstract val rootProjectDir: Property<String>

    /**
     * When `true`, also scaffold / merge `kmplibs.versions.toml`.
     * Set to `true` by [KmpBaseConventionPlugin], left `false` for Android-only projects.
     */
    @get:Input
    abstract val includeKmp: Property<Boolean>

    /**
     * Marker file at `build/appspiriment/catalog-scaffolded.txt`.
     * Its content is the last applied plugin version, which drives the UP-TO-DATE check.
     */
    @get:OutputFile
    abstract val markerFile: RegularFileProperty

    @TaskAction
    fun scaffold() {
        val rootDir = File(rootProjectDir.get())
        val gradleDir = File(rootDir, "gradle").also { it.mkdirs() }
        val version = pluginVersion.get()

        scaffoldCatalog(
            destination    = File(gradleDir, "$koltTomlName.versions.toml"),
            bundledContent = koltTomlContents,
            version        = version,
        )

        if (includeKmp.getOrElse(false)) {
            scaffoldCatalog(
                destination    = File(gradleDir, "$kmpTomlName.versions.toml"),
                bundledContent = kmpTomlContents,
                version        = version,
            )
        }

        markerFile.get().asFile.also {
            it.parentFile.mkdirs()
            it.writeText(version)
        }
    }

    // ── Core scaffold/merge logic ─────────────────────────────────────────────

    private fun scaffoldCatalog(destination: File, bundledContent: String, version: String) {
        if (!destination.exists()) {
            // Initial scaffold — write complete TOML with LIBVERSION resolved
            destination.writeText(bundledContent.resolveVersion(version))
            logger.lifecycle("[appspiriment] ✓ Scaffolded: gradle/${destination.name}")
            return
        }

        // Incremental update — preserve consumer content, push plugin changes
        val existing = destination.readText()
        val merged   = merge(existing = existing, bundled = bundledContent, newVersion = version)

        if (merged != existing) {
            destination.writeText(merged)
            logger.lifecycle("[appspiriment] ✓ Updated gradle/${destination.name} → plugin $version")
        } else {
            logger.debug("[appspiriment] gradle/${destination.name} already up-to-date")
        }
    }

    /**
     * Merges the consumer's existing TOML with the bundled (canonical) TOML:
     * - Updates all *owned* (`appspiriment*`) version values to [newVersion].
     * - Inserts any keys present in [bundled] but absent in [existing] into the
     *   correct section, annotated with a "added by plugin" comment.
     * - Never removes or alters any consumer-added entries.
     */
    private fun merge(existing: String, bundled: String, newVersion: String): String {
        val resolvedBundled = bundled.resolveVersion(newVersion)

        val bundledBySection = parseSections(resolvedBundled)
        val existingKeys     = parseAllKeys(existing)

        // Pass 1 — update owned version keys in-place
        val updatedLines = existing.lines().toMutableList()
        val inVersionsSection = BooleanArray(updatedLines.size)
        var section = ""
        for (i in updatedLines.indices) {
            val trimmed = updatedLines[i].trim()
            if (trimmed.isSectionHeader()) { section = trimmed; continue }
            if (section != "[versions]") continue
            inVersionsSection[i] = true
            val key = trimmed.extractKey() ?: continue
            if (key.isOwnedByPlugin()) {
                // Find this key's line in the bundled TOML and use it verbatim
                bundledBySection["[versions]"]
                    ?.firstOrNull { it.extractKey() == key }
                    ?.let { updatedLines[i] = it }
            }
        }

        // Pass 2 — insert new entries (present in bundled, absent in existing)
        val resultLines = updatedLines.toMutableList()
        val allSections = listOf("[versions]", "[libraries]", "[plugins]", "[bundles]")

        for (sectionHeader in allSections) {
            val bundledEntries = bundledBySection[sectionHeader] ?: continue
            val newEntries = bundledEntries.filter { line ->
                val key = line.trim().extractKey() ?: return@filter false
                key !in existingKeys
            }
            if (newEntries.isEmpty()) continue

            // Find insertion point: just before the NEXT section header (or EOF)
            val sectionIdx = resultLines.indexOfFirst { it.trim() == sectionHeader }
            if (sectionIdx == -1) {
                // Section doesn't exist yet — append it
                resultLines += ""
                resultLines += sectionHeader
                resultLines += "# Added by kolt plugin $newVersion"
                resultLines += newEntries
            } else {
                val nextSectionIdx = (sectionIdx + 1 until resultLines.size)
                    .firstOrNull { resultLines[it].trim().isSectionHeader() }
                    ?: resultLines.size

                val toInsert = buildList {
                    add("# Added by kolt plugin $newVersion")
                    addAll(newEntries)
                    add("")   // blank separator
                }
                resultLines.addAll(nextSectionIdx, toInsert)
            }
        }

        return resultLines.joinToString("\n")
    }

    // ── TOML helpers ─────────────────────────────────────────────────────────

    /**
     * Parses a TOML string into a map of `[section header]` → list of non-blank,
     * non-comment content lines belonging to that section.
     */
    private fun parseSections(toml: String): Map<String, List<String>> {
        val result = mutableMapOf<String, MutableList<String>>()
        var current = "[versions]"   // implicit first section
        for (line in toml.lines()) {
            val t = line.trim()
            when {
                t.isSectionHeader() -> current = t
                t.isEmpty() || t.startsWith("#") -> { /* skip */ }
                else -> result.getOrPut(current) { mutableListOf() }.add(line)
            }
        }
        return result
    }

    /** Returns the set of all entry keys present anywhere in a TOML string. */
    private fun parseAllKeys(toml: String): Set<String> =
        toml.lines().mapNotNull { it.trim().extractKey() }.toSet()

    /** Extracts the key from a TOML entry line, or null if the line isn't an entry. */
    private fun String.extractKey(): String? {
        if (isEmpty() || startsWith("#") || isSectionHeader() || !contains("=")) return null
        return substringBefore("=").trim().takeIf { it.isNotEmpty() }
    }

    private fun String.isSectionHeader() = startsWith("[") && endsWith("]") && !contains(".")

    /** A version key is "owned" by the plugin if it tracks an Kolt artifact version. */
    private fun String.isOwnedByPlugin() = startsWith("kolt", ignoreCase = true)

    /** Replaces the `LIBVERSION` placeholder with the real version string. */
    private fun String.resolveVersion(version: String) = replace("LIBVERSION", version)
}

// ── Registration helper ────────────────────────────────────────────────────────

/**
 * Registers [ScaffoldVersionCatalogTask] on the **root project** exactly once.
 *
 * Safe to call from multiple subproject plugins — subsequent calls are no-ops because
 * the task name is stable and `findByName` returns the existing registration.
 *
 * @param includeKmp Set to `true` from KMP plugins to also scaffold `kmplibs.versions.toml`.
 */
internal fun org.gradle.api.Project.registerCatalogScaffold(includeKmp: Boolean = false) {
    val taskName = "scaffoldKoltCatalogs"

    val existing = rootProject.tasks.findByName(taskName)
    if (existing != null) {
        // Already registered — if this call is from a KMP plugin, enable the KMP flag
        if (includeKmp) {
            (existing as? ScaffoldVersionCatalogTask)?.includeKmp?.set(true)
        }
        return
    }

    val task = rootProject.tasks.register(taskName, ScaffoldVersionCatalogTask::class.java) {
        group       = "appspiriment"
        description = "Scaffolds / updates Kolt version catalog TOML files in gradle/."

        pluginVersion.set(libVersion)
        rootProjectDir.set(rootProject.rootDir.absolutePath)
        this.includeKmp.set(includeKmp)
        markerFile.set(
            rootProject.layout.buildDirectory.file("appspiriment/catalog-scaffolded.txt")
        )
    }

    // Wire before preBuild of the project that triggered registration (runs on every build)
    tasks.named("preBuild").configure { dependsOn(task) }
}

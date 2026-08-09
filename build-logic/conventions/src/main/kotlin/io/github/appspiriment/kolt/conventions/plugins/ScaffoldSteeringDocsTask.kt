package io.github.appspiriment.kolt.conventions.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Gradle task that writes AI-agent steering docs into the consumer project's **root directory**.
 *
 * ## What gets scaffolded
 *
 * ```
 * <rootProject>/
 *   CLAUDE.md                  ← Claude Code context stub              (written once, user-owned)
 *   AGENTS.md                  ← Gemini / Antigravity stub             (written once, user-owned)
 *   docs/CODING_STANDARDS.md   ← binding coding rules                  (plugin-owned, auto-updates)
 *   docs/ARCHITECTURE.md       ← layering, DI, offline-first patterns  (plugin-owned, auto-updates)
 *   docs/TESTING.md            ← test patterns, fakes, coverage        (plugin-owned, auto-updates)
 * ```
 *
 * ## Ownership model
 *
 * - **`CLAUDE.md` / `AGENTS.md`** are project stubs teams fill in with project-specific facts.
 *   They are written **once** (only when absent) and never overwritten by the plugin.
 * - **`docs/`** files are authoritative plugin-owned references. They are **overwritten whenever
 *   [pluginVersion] changes**, ensuring AI agents in consumer projects always read current
 *   standards. Gradle's `@Input` on [pluginVersion] marks the task OUT-OF-DATE on every plugin
 *   version bump, so the overwrite happens automatically on the next build.
 *
 * ## Source
 *
 * Files are bundled inside the plugin JAR as classpath resources under `appspiriment/steering/`.
 * The canonical source is `Standards/` — edit there, then
 * publish a new plugin version. Standards and plugin code always ship together.
 */
abstract class ScaffoldSteeringDocsTask : DefaultTask() {

    /** Absolute path to the root project directory where docs land. */
    @get:Input
    abstract val rootProjectDir: Property<String>

    /**
     * Convention plugin version baked in at compile time (from generated `Constants.kt`).
     * Declared as `@Input` so Gradle marks this task OUT-OF-DATE whenever the plugin version
     * changes — triggering an automatic re-run that overwrites the plugin-owned `docs/` files.
     */
    @get:Input
    abstract val pluginVersion: Property<String>

    /**
     * Marker file at `build/appspiriment/steering-scaffolded.txt`.
     * Used for Gradle UP-TO-DATE checks — the actual doc files are NOT declared as outputs
     * so that `./gradlew clean` never removes them.
     */
    @get:OutputFile
    abstract val markerFile: RegularFileProperty

    @TaskAction
    fun scaffold() {
        val rootDir = File(rootProjectDir.get())
        val version = pluginVersion.get()

        // User-owned stubs — merged/updated on every version change
        mergeDoc(
            resource    = "appspiriment/steering/CLAUDE.md",
            destination = File(rootDir, "CLAUDE.md"),
        )
        mergeDoc(
            resource    = "appspiriment/steering/AGENTS.md",
            destination = File(rootDir, "AGENTS.md"),
        )

        // Plugin-owned reference docs from Standards/ — overwritten on every plugin version change
        copyDoc(
            resource    = "appspiriment/steering/steering/kmp/presentation-mvi.md",
            destination = File(rootDir, "docs/CODING_STANDARDS.md"),
            overwrite   = true,
        )
        copyDoc(
            resource    = "appspiriment/steering/steering/kmp/architecture.md",
            destination = File(rootDir, "docs/ARCHITECTURE.md"),
            overwrite   = true,
        )
        copyDoc(
            resource    = "appspiriment/steering/steering/kmp/testing.md",
            destination = File(rootDir, "docs/TESTING.md"),
            overwrite   = true,
        )
        copyDoc(
            resource    = "appspiriment/steering/KOLT.md",
            destination = File(rootDir, "docs/KOLT.md"),
            overwrite   = true,
        )

        markerFile.get().asFile.also {
            it.parentFile.mkdirs()
            it.writeText("Steering docs scaffolded by appspiriment convention plugin v$version. Safe to delete.")
        }
    }

    private fun mergeDoc(resource: String, destination: File) {
        val stream = javaClass.classLoader.getResourceAsStream(resource)
            ?: throw GradleException(
                "[appspiriment] Could not find bundled steering doc '$resource' inside the " +
                    "convention plugin JAR. This is a plugin packaging bug — please report it."
            )
        val templateContent = stream.use { it.bufferedReader().readText() }

        val beginMarker = "<!-- KOLT:BEGIN -->"
        val endMarker = "<!-- KOLT:END -->"

        if (!destination.exists()) {
            destination.parentFile.mkdirs()
            destination.writeText(templateContent)
            logger.lifecycle(
                """
                [appspiriment] ✓ Scaffolded: ${destination.path}
                [appspiriment]   → ${destination.name.note()}
                """.trimIndent()
            )
            return
        }

        val templateBeginIdx = templateContent.indexOf(beginMarker)
        val templateEndIdx = templateContent.indexOf(endMarker)
        if (templateBeginIdx == -1 || templateEndIdx == -1 || templateEndIdx < templateBeginIdx) {
            throw GradleException(
                "[appspiriment] Template resource '$resource' is missing markers $beginMarker / $endMarker."
            )
        }
        val templateSection = templateContent.substring(templateBeginIdx, templateEndIdx + endMarker.length)

        val existingContent = destination.readText()
        val destBeginIdx = existingContent.indexOf(beginMarker)
        val destEndIdx = existingContent.indexOf(endMarker)

        val mergedContent = if (destBeginIdx != -1 && destEndIdx != -1 && destEndIdx > destBeginIdx) {
            val prefix = existingContent.substring(0, destBeginIdx)
            val suffix = existingContent.substring(destEndIdx + endMarker.length)
            prefix + templateSection + suffix
        } else {
            // Legacy file check: preserve user's facts by looking for "## Project facts"
            val projectFactsIndex = existingContent.indexOf("## Project facts")
            if (projectFactsIndex != -1) {
                val suffix = existingContent.substring(projectFactsIndex)
                templateSection + "\n\n" + suffix
            } else {
                templateSection + "\n\n" + existingContent
            }
        }

        if (existingContent != mergedContent) {
            destination.writeText(mergedContent)
            logger.lifecycle(
                """
                [appspiriment] ✓ Merged/Updated: ${destination.path}
                [appspiriment]   → ${destination.name.note()}
                """.trimIndent()
            )
        } else {
            logger.debug("[appspiriment] skipping ${destination.name} — no changes in merged content")
        }
    }

    private fun copyDoc(resource: String, destination: File, overwrite: Boolean) {
        if (destination.exists() && !overwrite) {
            logger.debug("[appspiriment] skipping ${destination.name} — user-owned, already present")
            return
        }

        val stream = javaClass.classLoader.getResourceAsStream(resource)
            ?: throw GradleException(
                "[appspiriment] Could not find bundled steering doc '$resource' inside the " +
                    "convention plugin JAR. This is a plugin packaging bug — please report it."
            )

        val wasPresent = destination.exists()
        destination.parentFile.mkdirs()
        stream.use { input -> destination.outputStream().use { input.copyTo(it) } }

        val action = if (wasPresent) "Updated" else "Scaffolded"
        logger.lifecycle(
            """
            [appspiriment] ✓ $action: ${destination.path}
            [appspiriment]   → ${destination.name.note()}
            """.trimIndent()
        )
    }

    private fun String.note() = when (this) {
        "CLAUDE.md"           -> "Fill in project facts — Claude Code reads this before coding."
        "AGENTS.md"           -> "Fill in project facts — Gemini/Antigravity reads this before coding."
        "CODING_STANDARDS.md" -> "Binding coding rules — auto-updated on plugin version change."
        "ARCHITECTURE.md"     -> "Read on demand: layering, DI, offline-first — auto-updated on plugin version change."
        "TESTING.md"          -> "Read on demand: test patterns, fakes, coverage — auto-updated on plugin version change."
        "KOLT.md"             -> "Read on demand: plugin catalog, DSL options, library APIs — auto-updated on plugin version change."
        else                  -> "Edit as needed."
    }
}

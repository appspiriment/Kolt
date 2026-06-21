package io.github.appspiriment.kolt.intellij

import com.intellij.facet.ui.ValidationResult
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.ProjectGeneratorPeer
import java.io.File
import java.io.InputStream
import javax.swing.Icon

/**
 * Registers an "Kolt" entry in the **New Project** wizard and generates a
 * fully-configured Android or KMP project when the user clicks Finish.
 *
 * ## What gets created
 *
 * ```
 * <project-dir>/
 *   settings.gradle.kts      ← catalog wired, modules declared
 *   build.gradle.kts          ← plugins declared apply false
 *   gradle.properties
 *   gradlew + gradle/wrapper/ ← bundled Gradle 8.x wrapper
 *   app/build.gradle.kts      ← convention plugin applied, namespace substituted
 *   shared/build.gradle.kts   ← (KMP only)
 *   CLAUDE.md / AGENTS.md     ← AI-agent steering stubs
 *   docs/                     ← CODING_STANDARDS + ARCHITECTURE + TESTING + APPSPIRIMENT
 *   src/<sourceSet>/kotlin/<package-path>/   ← source dirs
 * ```
 *
 * Template files are bundled in the plugin JAR under `templates/`.
 * Placeholders substituted: `<AppName>`, `<company>`, `<appname>`, `com.example.myapp`.
 */
class KoltProjectGenerator : DirectoryProjectGenerator<KoltProjectSettings> {

    private val log = logger<KoltProjectGenerator>()

    // ── DirectoryProjectGenerator contract ───────────────────────────────────

    override fun getName(): String = "Kolt"

    override fun getDescription(): String =
        "Android (Compose + Hilt) or Kotlin Multiplatform project using Kolt convention plugins"

    /**
     * Icon shown next to the generator name in the New Project list.
     * Falls back to null if the resource isn't bundled (IDE handles null gracefully).
     */
    override fun getLogo(): Icon? = runCatching {
        IconLoader.getIcon("/icons/appspiriment.svg", javaClass)
    }.getOrNull()

    /**
     * Validates the chosen project location before the wizard lets the user proceed.
     * Return [ValidationResult.OK] for a valid location; return an error result otherwise.
     */
    override fun validate(baseDirPath: String): ValidationResult {
        if (baseDirPath.isBlank()) return ValidationResult("Please choose a project location.")
        val dir = File(baseDirPath)
        if (dir.exists() && dir.listFiles()?.isNotEmpty() == true) {
            return ValidationResult("Directory is not empty: $baseDirPath")
        }
        return ValidationResult.OK
    }

    override fun createPeer(): ProjectGeneratorPeer<KoltProjectSettings> =
        KoltProjectPeer()

    override fun generateProject(
        project: Project,
        baseDir: VirtualFile,
        settings: KoltProjectSettings,
        module: Module,
    ) {
        val projectName = project.name
        val packageName = settings.packageName
        val templateRoot = settings.projectType.templateDir  // e.g. "templates/android-project"
        val isKmp = settings.projectType == KoltProjectSettings.ProjectType.KMP

        log.info("[Kolt] Generating ${settings.projectType} project '$projectName' ($packageName)")

        ApplicationManager.getApplication().runWriteAction {
            val rootFile = File(baseDir.path)

            // 1. Copy template files with placeholder substitution
            copyTemplateDir(templateRoot, rootFile, projectName, packageName)

            // 2. Copy steering docs into docs/
            listOf("CODING_STANDARDS.md", "ARCHITECTURE.md", "TESTING.md", "KOLT.md")
                .forEach { doc ->
                    copyResource("templates/docs/$doc", File(rootFile, "docs/$doc"),
                        projectName, packageName)
                }

            // 3. Copy root CLAUDE.md / AGENTS.md stubs (if not already in template)
            for (stub in listOf("CLAUDE.md", "AGENTS.md")) {
                val dst = File(rootFile, stub)
                if (!dst.exists()) {
                    copyResource("templates/$stub", dst, projectName, packageName)
                }
            }

            // 4. Create source directory tree
            val packagePath = packageName.replace('.', '/')
            val srcDirs = if (isKmp) {
                listOf(
                    "app/src/main/kotlin/$packagePath",
                    "app/src/test/kotlin/$packagePath",
                    "shared/src/commonMain/kotlin/$packagePath",
                    "shared/src/androidMain/kotlin/$packagePath",
                    "shared/src/commonTest/kotlin/$packagePath",
                    "data/src/commonMain/kotlin/$packagePath",
                    "data/src/androidMain/kotlin/$packagePath",
                )
            } else {
                listOf(
                    "app/src/main/kotlin/$packagePath",
                    "app/src/test/kotlin/$packagePath",
                    "app/src/main/res/values",
                    "app/src/main/res/values-night",
                    "app/src/main/res/drawable",
                    "app/src/main/res/layout",
                )
            }
            srcDirs.forEach { File(rootFile, it).mkdirs() }

            // 5. Refresh VFS so the IDE sees the new files immediately
            VfsUtil.markDirtyAndRefresh(false, true, true, baseDir)
            LocalFileSystem.getInstance().refresh(false)
        }

        log.info("[Kolt] Project created at ${baseDir.path}")
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private fun copyTemplateDir(
        resourceDir: String,
        destDir: File,
        projectName: String,
        packageName: String,
    ) {
        // List all bundled resources under resourceDir/ by reading the directory index
        // resource. Fall back to walking known file lists if index not found.
        val index = loadResource("$resourceDir/.index")
        if (index != null) {
            index.bufferedReader().lineSequence()
                .filter { it.isNotBlank() }
                .forEach { relativePath ->
                    copyResource(
                        "$resourceDir/$relativePath",
                        File(destDir, relativePath),
                        projectName,
                        packageName,
                    )
                }
        } else {
            // Fallback: copy the well-known file list for each template type
            knownFiles(resourceDir).forEach { relativePath ->
                copyResource(
                    "$resourceDir/$relativePath",
                    File(destDir, relativePath),
                    projectName,
                    packageName,
                )
            }
        }
    }

    private fun copyResource(
        resource: String,
        dest: File,
        projectName: String,
        packageName: String,
    ) {
        val stream: InputStream = javaClass.classLoader.getResourceAsStream(resource) ?: run {
            log.warn("[Kolt] Resource not found: $resource — skipping")
            return
        }
        dest.parentFile.mkdirs()
        val raw = stream.use { it.readBytes().toString(Charsets.UTF_8) }
        val substituted = substitute(raw, projectName, packageName)
        dest.writeText(substituted, Charsets.UTF_8)
        log.info("[Kolt]   ✓ ${dest.name}")
    }

    private fun loadResource(resource: String): InputStream? =
        javaClass.classLoader.getResourceAsStream(resource)

    /**
     * Substitutes all Kolt template placeholders.
     *
     * Placeholders used in the template files:
     *   `<AppName>`               → project name (e.g. "MyApp")
     *   `<company>` / `<appname>` → derived from package segments
     *   `com.<company>.<appname>` → full package name
     *   `com.example.myapp`       → full package name (legacy)
     */
    private fun substitute(content: String, projectName: String, packageName: String): String {
        val parts = packageName.split(".")
        val companyPart = if (parts.size >= 2) parts[parts.size - 2] else "example"
        val appPart     = if (parts.isNotEmpty()) parts.last() else "app"

        return content
            .replace("com.<company>.<appname>", packageName)
            .replace("com.example.myapp", packageName)
            .replace("<company>", companyPart)
            .replace("<appname>", appPart)
            .replace("<AppName>", projectName)
            // Strip trailing "← replace" IDE hints once substitution has happened
            .replace(Regex(""""$projectName"\s*//\s*←.*""")) { "\"$projectName\"" }
    }

    /**
     * Returns the list of relative file paths for each known template.
     * Used as a fallback when no `.index` resource is present.
     */
    private fun knownFiles(templateRoot: String): List<String> = when {
        templateRoot.endsWith("android-project") -> listOf(
            "settings.gradle.kts",
            "build.gradle.kts",
            "gradle.properties",
            "app/build.gradle.kts",
            "CLAUDE.md",
            "AGENTS.md",
        )
        templateRoot.endsWith("kmp-project") -> listOf(
            "settings.gradle.kts",
            "build.gradle.kts",
            "gradle.properties",
            "app/build.gradle.kts",
            "shared/build.gradle.kts",
            "CLAUDE.md",
            "AGENTS.md",
        )
        else -> emptyList()
    }
}

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
 * Gradle task that writes editable compose-utils theme resource files into the app module's
 * `src/main/res` directory **only when they don't already exist**.
 *
 * ## How it works
 *
 * The three XML template files are bundled inside the plugin JAR as classpath resources
 * under `appspiriment/templates/`.  When this task runs for the first time it copies them
 * into the consumer app's `src/main/res/`, where they become ordinary Android resource
 * files that AAPT2 merges with library resources at build time.
 *
 * Android's resource-merge priority is:
 * ```
 *   App module  >  Feature modules  >  Library AARs (compose-utils)
 * ```
 * So any `<color name="primary">` declared in the app's `src/main/res/` silently overrides
 * the same name that is baked into the compose-utils AAR — no Kotlin code changes needed.
 *
 * ## Template source
 *
 * Templates live in the plugin's own `src/main/resources/appspiriment/templates/` and are
 * packed into the plugin JAR at build time.  To update the templates (e.g. when compose-utils
 * adds a new colour token), edit the XML files in that directory and publish a new plugin
 * version.  Consumers re-run `./gradlew scaffoldKoltResources` after deleting the
 * relevant `src/main/res` file to receive the updated defaults.
 *
 * ## Generated files
 * ```
 * src/main/res/values/appspiriment_colors.xml        – day-theme colours
 * src/main/res/values-night/appspiriment_colors.xml  – night-theme colours
 * src/main/res/values/appspiriment_dimens.xml        – font sizes & spacing
 * ```
 *
 * **Safe to re-run**: existing files are never overwritten.
 * Delete a file to restore library defaults on the next build.
 *
 * Gradle up-to-date tracking uses a lightweight marker file in `build/appspiriment/`.
 * `./gradlew clean` resets the marker but leaves your `src/main/res` files intact.
 */
abstract class ScaffoldComposeThemeTask : DefaultTask() {

    /** Absolute path to the module's root directory (i.e. where `src/` lives). */
    @get:Input
    abstract val moduleDir: Property<String>

    /**
     * Marker file at `build/appspiriment/theme-scaffolded.txt`.
     * Gradle uses this for UP-TO-DATE checks — the `src/main/res` files are intentionally
     * NOT declared as outputs so that `./gradlew clean` never touches them.
     */
    @get:OutputFile
    abstract val markerFile: RegularFileProperty

    @TaskAction
    fun scaffold() {
        val resDir = File(moduleDir.get(), "src/main/res")

        copyTemplate(
            classpathResource = "appspiriment/templates/values/appspiriment_colors.xml",
            destination       = File(resDir, "values/appspiriment_colors.xml"),
        )
        copyTemplate(
            classpathResource = "appspiriment/templates/values-night/appspiriment_colors.xml",
            destination       = File(resDir, "values-night/appspiriment_colors.xml"),
        )
        copyTemplate(
            classpathResource = "appspiriment/templates/values/appspiriment_dimens.xml",
            destination       = File(resDir, "values/appspiriment_dimens.xml"),
        )

        // Write marker so Gradle skips this task on the next build (UP-TO-DATE).
        markerFile.get().asFile.also {
            it.parentFile.mkdirs()
            it.writeText("Scaffolded by appspiriment convention plugin. Safe to delete.")
        }
    }

    /**
     * Copies [classpathResource] from the plugin JAR to [destination], but only if
     * [destination] does not already exist.
     *
     * The resource is loaded via [ClassLoader.getResourceAsStream] so it works whether
     * the plugin is running from a JAR (production) or from a directory (tests / IDE).
     */
    private fun copyTemplate(classpathResource: String, destination: File) {
        if (destination.exists()) {
            logger.debug("[compose-utils] skipping ${destination.name} — already present")
            return
        }

        val stream = javaClass.classLoader.getResourceAsStream(classpathResource)
            ?: throw GradleException(
                "[compose-utils] Could not find bundled template '$classpathResource' " +
                    "inside the convention plugin JAR. " +
                    "This is a plugin packaging bug — please report it."
            )

        destination.parentFile.mkdirs()
        stream.use { input -> destination.outputStream().use { input.copyTo(it) } }

        logger.lifecycle(
            """
            [compose-utils] ✓ Generated: ${destination.path}
            [compose-utils]   → Edit this file to customise your app's theme.
            [compose-utils]   → It won't be regenerated while it exists.
            """.trimIndent()
        )
    }
}

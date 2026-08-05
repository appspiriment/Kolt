package io.github.appspiriment.kolt.conventions.plugins

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import io.github.appspiriment.kolt.conventions.extensions.KOLT_EXTENSION_NAME
import io.github.appspiriment.kolt.conventions.extensions.KoltExtension
import io.github.appspiriment.kolt.conventions.extensions.applyPluginFromLibs
import io.github.appspiriment.kolt.conventions.extensions.koltLibs
import io.github.appspiriment.kolt.conventions.extensions.baseDependencies
import io.github.appspiriment.kolt.conventions.extensions.basePluginList
import io.github.appspiriment.kolt.conventions.extensions.bomDependency
import io.github.appspiriment.kolt.conventions.extensions.composeDependencies
import io.github.appspiriment.kolt.conventions.extensions.composePluginList
import io.github.appspiriment.kolt.conventions.extensions.composeUtilDependencies
import io.github.appspiriment.kolt.conventions.extensions.configureAndroidEarly
import io.github.appspiriment.kolt.conventions.extensions.configureAndroidLate
import io.github.appspiriment.kolt.conventions.extensions.hiltComposeDependencies
import io.github.appspiriment.kolt.conventions.extensions.hiltDependencies
import io.github.appspiriment.kolt.conventions.extensions.hiltPluginList
import io.github.appspiriment.kolt.conventions.extensions.implementDependency
import io.github.appspiriment.kolt.conventions.extensions.utilDependencies
import io.github.appspiriment.kolt.conventions.extensions.DEFAULT_DEBUG_VERSION_TIMESTAMP_PATTERN
import io.github.appspiriment.kolt.conventions.extensions.buildDateSuffix
import io.github.appspiriment.kolt.conventions.extensions.libVersion
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

// ────────────────────────────────────────────────────────────────────────────────
// Capability enum — tracks which optional features a plugin has activated.
// Subclasses call setupHilt() / setupCompose() before super.apply(), which
// registers the capability. afterEvaluate then reads the set to decide which
// optional util dependencies to add. This replaces the previous mutable boolean
// flag approach, making capability tracking explicit and side-effect-free.
// ────────────────────────────────────────────────────────────────────────────────
enum class PluginCapability { HILT, COMPOSE }

// ────────────────────────────────────────────────────────────────────────────────
// Abstract Base Convention Plugin
// ────────────────────────────────────────────────────────────────────────────────

abstract class AndroidBaseConventionPlugin : Plugin<Project> {

    abstract val Project.commonExtension: CommonExtension<*, *, *, *, *, *>

    /**
     * Capabilities registered by subclasses before calling [apply].
     * Using a mutable set here is safe because plugin instances are created
     * fresh per project by Gradle — there is no cross-project sharing.
     */
    private val capabilities = mutableSetOf<PluginCapability>()

    override fun apply(target: Project) {
        with(target) {
            // 1. Create extension early so user can configure it immediately
            extensions.create(KOLT_EXTENSION_NAME, KoltExtension::class.java)

            // 2. Apply mandatory plugins early (kotlin-android etc.)
            applyMandatoryPlugins()

            // 3. Set SDKs and other EARLY immutable properties
            configureAndroidEarly(commonExtension)

            // 4. Configure debug suffixes via Variant API.
            //    The applicationId suffix and the versionName timestamp are independent:
            //    each has its own toggle so consumers can use one without the other.
            extensions.findByType<ApplicationAndroidComponentsExtension>()?.onVariants { variant ->
                if (variant.buildType == "debug") {
                    val config = extensions.getByType<KoltExtension>()

                    // applicationId suffix (overridable; default ".dev")
                    if (config.addDevSuffixToDebug.getOrElse(true)) {
                        val suffix = config.debugApplicationIdSuffix.getOrElse(".dev")
                        if (suffix.isNotEmpty()) {
                            variant.applicationId.set(variant.applicationId.map { "$it$suffix" })
                        }
                    }

                    // versionName timestamp (independent toggle; configurable pattern)
                    if (config.appendTimestampToDebugVersion.getOrElse(true)) {
                        val pattern = config.debugVersionTimestampPattern
                            .getOrElse(DEFAULT_DEBUG_VERSION_TIMESTAMP_PATTERN)
                        variant.outputs.forEach { output ->
                            output.versionName.set(output.versionName.map { current ->
                                "$current${buildDateSuffix(pattern)}"
                            })
                        }
                    }
                }
            }

            // 5. Add the Kolt BOM so all io.github.appspiriment.kolt:* deps resolve without
            //    explicit versions. Must be before util/compose-util dependencies.
            applyAppspirimentBom()

            // 5b. Apply core dependencies (always needed)
            applyCoreDependencies()

            // 5c. Scaffold / update Kolt version catalog in consumer gradle/ dir.
            // Registered once on rootProject — safe to call from every module.
            registerCatalogScaffold(includeKmp = false)

            // 6. Defer everything that depends on user config or can be set late.
            // afterEvaluate is required here to read user-configured extension values
            // after the consuming build script has been evaluated.
            afterEvaluate {
                val config = extensions.getByType<KoltExtension>()

                // Late Android configuration (build features, etc.)
                configureAndroidLate(
                    commonExtension = commonExtension,
                    addDevSuffixToDebug = config.addDevSuffixToDebug.orNull ?: true
                )

                // Optional utility dependencies
                if (config.enableUtils.orNull ?: true) {
                    applyUtilsDependencies()
                    // Add compose-specific utils only when Compose capability is active
                    if (PluginCapability.COMPOSE in capabilities) {
                        applyComposeUtilsDependencies()
                    }
                }

                // Scaffold editable theme resources (colours + dimens) for app modules
                // that use Compose. Only runs when the target files are absent.
                val shouldScaffold = config.scaffoldThemeResources.orNull ?: true
                if (shouldScaffold
                    && PluginCapability.COMPOSE in capabilities
                    && plugins.hasPlugin("com.android.application")
                ) {
                    registerScaffoldTask()
                    registerSteeringDocsTask()
                }

                // Finalize build types
                finalizeAndroidConfiguration(config.enableMinify.orNull ?: false)
            }
        }
    }

    private fun Project.applyMandatoryPlugins() {
        pluginManager.applyPluginFromLibs(koltLibs to basePluginList)
    }

    private fun Project.applyAppspirimentBom() {
        dependencies {
            implementDependency(libs = koltLibs, dependencyList = bomDependency)
        }
    }

    private fun Project.applyCoreDependencies() {
        dependencies {
            implementDependency(libs = koltLibs, dependencyList = baseDependencies)
        }
    }

    // ────────────────────────────────────────────────
    // Capability setup helpers — called by concrete subclasses before super.apply()
    // ────────────────────────────────────────────────

    protected fun Project.setupHilt() {
        capabilities += PluginCapability.HILT
        pluginManager.applyPluginFromLibs(koltLibs to hiltPluginList)
        dependencies {
            implementDependency(libs = koltLibs, dependencyList = hiltDependencies)
        }
    }

    protected fun Project.setupCompose() {
        capabilities += PluginCapability.COMPOSE
        pluginManager.applyPluginFromLibs(koltLibs to composePluginList)
        dependencies {
            implementDependency(libs = koltLibs, dependencyList = composeDependencies)
            // Hilt+Compose glue only when this module also has the Hilt capability. setupHilt()
            // always runs before setupCompose() (see the apply() ordering), so the capability
            // is already registered here for app / library-hilt-compose modules.
            if (PluginCapability.HILT in capabilities) {
                implementDependency(libs = koltLibs, dependencyList = hiltComposeDependencies)
            }
        }
        // AGP 8.7.3 + Kotlin 2.x Analysis API has a known IncompatibleClassChangeError in
        // several compose-runtime lint detectors (RememberInCompositionDetector,
        // FrequentlyChangingValueDetector, etc.). Disable all lint analysis tasks for Compose
        // modules in this repo to avoid the crash. These are utility libraries — cosmetic lint
        // is not a gate for the build.
        tasks.configureEach {
            if (name.startsWith("lint")) enabled = false
        }
    }

    private fun Project.applyUtilsDependencies() {
        dependencies {
            implementDependency(libs = koltLibs, dependencyList = utilDependencies)
        }
    }

    private fun Project.applyComposeUtilsDependencies() {
        dependencies {
            implementDependency(libs = koltLibs, dependencyList = composeUtilDependencies)
        }
    }

    private fun Project.finalizeAndroidConfiguration(enableMinify: Boolean) {
        commonExtension.buildTypes.getByName("release") {
            isMinifyEnabled = enableMinify
        }

        if (plugins.hasPlugin("com.google.devtools.ksp")) {
            extensions.configure<KspExtension> {
                // Global KSP args can be added here if needed (e.g. Room schema export path)
            }
        }
    }

    // ────────────────────────────────────────────────
    // compose-utils theme scaffold
    // ────────────────────────────────────────────────

    /**
     * Registers the [ScaffoldComposeThemeTask] and wires it before `preBuild`.
     *
     * The task is idempotent — it only writes files that are absent. Gradle's
     * UP-TO-DATE check uses a lightweight marker file in `build/appspiriment/` so
     * the task is skipped entirely on subsequent builds. Running `./gradlew clean`
     * removes the marker but leaves the already-generated `src/main/res` files
     * untouched, so the task re-runs once and immediately becomes UP-TO-DATE again.
     *
     * The task can also be invoked manually at any time:
     * ```
     * ./gradlew scaffoldKoltResources
     * ```
     */
    private fun Project.registerScaffoldTask() {
        val scaffoldTask = tasks.register<ScaffoldComposeThemeTask>(
            "scaffoldKoltResources"
        ) {
            group = "kolt"
            description = "Generates editable compose-utils theme XML files " +
                "(colours + dimens) into src/main/res. " +
                "Existing files are never overwritten."

            moduleDir.set(projectDir.absolutePath)
            markerFile.set(
                layout.buildDirectory.file("appspiriment/theme-scaffolded.txt")
            )
        }

        // Wire before preBuild so the files exist before AAPT runs.
        tasks.named("preBuild").configure { dependsOn(scaffoldTask) }
    }

    /**
     * Registers [ScaffoldSteeringDocsTask] as `scaffoldKoltDocs`.
     *
     * Copies `docs/CODING_STANDARDS.md`, `CLAUDE.md`, and `AGENTS.md` into the **root
     * project directory** the first time the consumer project is built.  Existing files
     * are never overwritten so the team can freely customise the stubs.
     *
     * The task runs automatically before `preBuild` (same as the theme-scaffold task).
     * It can also be invoked explicitly:
     * ```
     * ./gradlew scaffoldKoltDocs
     * ```
     */
    private fun Project.registerSteeringDocsTask() {
        val steeringTask = tasks.register<ScaffoldSteeringDocsTask>(
            "scaffoldKoltDocs"
        ) {
            group = "kolt"
            description = "Scaffolds AI-agent steering docs into the root project directory. " +
                "CLAUDE.md/AGENTS.md are written once (user-owned). " +
                "docs/ files are auto-updated on each plugin version change."

            rootProjectDir.set(rootProject.projectDir.absolutePath)
            pluginVersion.set(libVersion)
            markerFile.set(
                layout.buildDirectory.file("appspiriment/steering-scaffolded.txt")
            )
        }

        tasks.named("preBuild").configure { dependsOn(steeringTask) }
    }
}

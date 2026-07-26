import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.util.Properties

plugins {
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.vanniktech.maven.publish)
    `kotlin-dsl`
    `version-catalog`
    `maven-publish`
}


// ────────────────────────────────────────────────
// 1. VERSIONING (per-artifact — separate version tracks per lib + plugin + BOM)
//
// Note: this is an included build, so `rootDir` is build-logic/.
// The shared version.properties lives one level up, at the repo root.
// ────────────────────────────────────────────────
val versionPropsFile = rootDir.parentFile.resolve("version.properties")

fun loadVersionProps(): Properties = Properties().apply {
    if (versionPropsFile.exists()) versionPropsFile.inputStream().use { load(it) }
}

/** Compute a dev/release version string for one artifact. */
fun computeVersion(props: Properties, majorKey: String, devKey: String): String {
    val major = props.getProperty(majorKey, "1.0.0")
    val dev   = props.getProperty(devKey, "0").toInt()
    return if (project.hasProperty("isRelease")) major
    else "$major.dev-${dev.toString().padStart(2, '0')}"
}

val versionProps = loadVersionProps()

val currentPluginVersion   = computeVersion(versionProps, "PLUGIN_MAJOR",       "PLUGIN_DEV")
val currentUtilsVersion    = computeVersion(versionProps, "UTILS_MAJOR",        "UTILS_DEV")
val currentLogutilsVersion = computeVersion(versionProps, "LOGUTILS_MAJOR",     "LOGUTILS_DEV")
val currentComposeVersion  = computeVersion(versionProps, "COMPOSE_UTILS_MAJOR","COMPOSE_UTILS_DEV")
val currentComposeKmpVersion = computeVersion(versionProps, "COMPOSE_KMP_MAJOR", "COMPOSE_KMP_DEV")
val currentUpdateVersion   = computeVersion(versionProps, "UPDATE_UTILS_MAJOR", "UPDATE_UTILS_DEV")
val currentBomVersion      = versionProps.getProperty("BOM_VERSION", "2025.06.1")

group = "io.github.appspiriment.kolt"
version = currentPluginVersion

// ────────────────────────────────────────────────
// 2. PER-ARTIFACT BUMP TASKS & README UPDATER
// ────────────────────────────────────────────────

fun updateReadmeVersions() {
    val readmeFile = rootDir.parentFile.resolve("README.md")
    if (!readmeFile.exists()) {
        println("⚠️ README.md not found at ${readmeFile.absolutePath}")
        return
    }
    val props = loadVersionProps()
    val pluginVer       = computeVersion(props, "PLUGIN_MAJOR",        "PLUGIN_DEV")
    val utilsVer        = computeVersion(props, "UTILS_MAJOR",         "UTILS_DEV")
    val logutilsVer     = computeVersion(props, "LOGUTILS_MAJOR",      "LOGUTILS_DEV")
    val composeUtilsVer = computeVersion(props, "COMPOSE_UTILS_MAJOR",  "COMPOSE_UTILS_DEV")
    val composeKmpVer   = computeVersion(props, "COMPOSE_KMP_MAJOR",    "COMPOSE_KMP_DEV")
    val updateUtilsVer  = computeVersion(props, "UPDATE_UTILS_MAJOR",   "UPDATE_UTILS_DEV")
    val locationVer     = computeVersion(props, "LOCATION_MAJOR",       "LOCATION_DEV")
    val bomVer          = props.getProperty("BOM_VERSION", "2026.06.0")

    val pluginVerBadge       = pluginVer.replace("-", "--")
    val utilsVerBadge        = utilsVer.replace("-", "--")
    val logutilsVerBadge     = logutilsVer.replace("-", "--")
    val composeUtilsVerBadge = composeUtilsVer.replace("-", "--")
    val composeKmpVerBadge   = composeKmpVer.replace("-", "--")
    val updateUtilsVerBadge  = updateUtilsVer.replace("-", "--")
    val locationVerBadge     = locationVer.replace("-", "--")
    val bomVerBadge          = bomVer.replace("-", "--")

    var content = readmeFile.readText()

    // 1. Header Badges
    content = content.replace(Regex("Plugin%20Version-[^?)]+"), "Plugin%20Version-$pluginVerBadge-1a73e8")
    content = content.replace(Regex("Library%20BOM-[^?)]+"), "Library%20BOM-$bomVerBadge-0d47a1")

    // 2. Table Badges
    content = content.replace(Regex("/badge/BOM-[^?)]+"), "/badge/BOM-$bomVerBadge-0d47a1")
    content = content.replace(Regex("/badge/utils-[^?)]+"), "/badge/utils-$utilsVerBadge-43a047")
    content = content.replace(Regex("/badge/logutils-[^?)]+"), "/badge/logutils-$logutilsVerBadge-43a047")
    content = content.replace(Regex("/badge/compose%20utils-[^?)]+"), "/badge/compose%20utils-$composeUtilsVerBadge-6200ea")
    content = content.replace(Regex("/badge/compose%20kmp-[^?)]+"), "/badge/compose%20kmp-$composeKmpVerBadge-6200ea")
    content = content.replace(Regex("/badge/update%20utils-[^?)]+"), "/badge/update%20utils-$updateUtilsVerBadge-f57c00")
    content = content.replace(Regex("/badge/location-[^?)]+"), "/badge/location-$locationVerBadge-00838f")

    // 3. Quick Start Catalog/BOM References
    content = content.replace(Regex("kolt-catalog:[0-9a-zA-Z.-]+"), "kolt-catalog:$pluginVer")
    content = content.replace(Regex("kmp-catalog:[0-9a-zA-Z.-]+"), "kmp-catalog:$pluginVer")
    content = content.replace(Regex("kolt-bom:[0-9a-zA-Z.-]+"), "kolt-bom:$bomVer")

    readmeFile.writeText(content)
    println("📝 README.md version badges and code snippets updated to match version.properties.")
}

/** Generic bump-task factory — increments the <prefix>_DEV counter. */
fun registerBumpTask(
    taskName: String,
    prefixDev: String,
    prefixMajor: String,
    displayName: String
) = tasks.register(taskName) {
    group = "versioning"
    description = "Bumps the DEV counter for $displayName in version.properties."
    outputs.file(versionPropsFile)
    outputs.upToDateWhen { false } // this is a stateful command, not a pure build step — always run when invoked
    doLast {
        val props = loadVersionProps()
        val major = props.getProperty(prefixMajor, "1.0.0")
        val dev   = props.getProperty(prefixDev, "0").toInt() + 1
        props.setProperty(prefixDev, dev.toString())
        versionPropsFile.outputStream().use { props.store(it, null) }
        val newVersion = "$major.dev-${dev.toString().padStart(2, '0')}"
        logger.lifecycle("🚀 $displayName bumped to: $newVersion")
        updateReadmeVersions()
    }
}

val bumpPluginVersion       = registerBumpTask("bumpPluginVersion",       "PLUGIN_DEV",       "PLUGIN_MAJOR",       "convention-plugins")
val bumpUtilsVersion        = registerBumpTask("bumpUtilsVersion",        "UTILS_DEV",        "UTILS_MAJOR",        "utils")
val bumpLogutilsVersion     = registerBumpTask("bumpLogutilsVersion",     "LOGUTILS_DEV",     "LOGUTILS_MAJOR",     "logutils")
val bumpComposeUtilsVersion = registerBumpTask("bumpComposeUtilsVersion", "COMPOSE_UTILS_DEV","COMPOSE_UTILS_MAJOR","compose-utils")
val bumpComposeKmpVersion   = registerBumpTask("bumpComposeKmpVersion",   "COMPOSE_KMP_DEV",  "COMPOSE_KMP_MAJOR",  "compose-kmp")
val bumpUpdateUtilsVersion  = registerBumpTask("bumpUpdateUtilsVersion",  "UPDATE_UTILS_DEV", "UPDATE_UTILS_MAJOR", "update-utils")
val bumpLocationVersion     = registerBumpTask("bumpLocationVersion",     "LOCATION_DEV",     "LOCATION_MAJOR",     "location")
val bumpLocationPickerVersion = registerBumpTask("bumpLocationPickerVersion", "LOCATION_PICKER_DEV", "LOCATION_PICKER_MAJOR", "location-picker")

/** Bumps the BOM calendar patch counter (e.g. 2025.06.1 → 2025.06.2). */
val bumpBomVersion = tasks.register("bumpBomVersion") {
    group = "versioning"
    description = "Increments the BOM_VERSION patch counter in version.properties."
    outputs.file(versionPropsFile)
    outputs.upToDateWhen { false } // this is a stateful command, not a pure build step — always run when invoked
    doLast {
        val props = loadVersionProps()
        val current = props.getProperty("BOM_VERSION", "2025.06.1")
        val parts   = current.split(".")
        val patch   = parts.getOrElse(2) { "1" }.toIntOrNull() ?: 1
        val newBom  = "${parts.getOrElse(0) { "2025" }}.${parts.getOrElse(1) { "06" }}.${patch + 1}"
        props.setProperty("BOM_VERSION", newBom)
        versionPropsFile.outputStream().use { props.store(it, null) }
        logger.lifecycle("🚀 BOM version bumped to: $newBom")
        updateReadmeVersions()
    }
}


/** Convenience: bump ALL DEV counters + BOM. Useful for initial releases or resets. */
val bumpAllVersions = tasks.register("bumpAllVersions") {
    group = "versioning"
    description = "Bumps ALL per-artifact DEV counters and the BOM version."
    dependsOn(bumpPluginVersion, bumpUtilsVersion, bumpLogutilsVersion,
              bumpComposeUtilsVersion, bumpComposeKmpVersion, bumpUpdateUtilsVersion,
              bumpLocationVersion, bumpLocationPickerVersion, bumpBomVersion)
}

// Backward-compat alias
tasks.register("bumpDevVersion") {
    group = "versioning"
    description = "Deprecated alias for bumpAllVersions — prefer per-artifact bump tasks."
    dependsOn(bumpAllVersions)
}

val updateReadme = tasks.register("updateReadme") {
    group = "documentation"
    description = "Updates README.md version badges and code snippets to match version.properties."
    doLast {
        updateReadmeVersions()
    }
}


// ────────────────────────────────────────────────
// 3. CONSTANTS.KT GENERATOR (generic — used for both Android and KMP catalogs)
//
// Reads a consumer TOML, replaces all per-artifact version placeholders, parses
// version/plugin/library refs, and generates a Constants source baked into the
// plugin JAR. This is how each plugin knows what TOML content to write into
// consumer projects.
//
// TOML placeholder tokens:
//   PLUGIN_VERSION        → currentPluginVersion
//   UTILS_VERSION         → currentUtilsVersion
//   LOGUTILS_VERSION      → currentLogutilsVersion
//   COMPOSE_UTILS_VERSION → currentComposeVersion
//   UPDATE_UTILS_VERSION  → currentUpdateVersion
//   BOM_VERSION           → currentBomVersion
//   LIBVERSION            → currentPluginVersion  (backward-compat alias)
// ────────────────────────────────────────────────
val generatedSourceDir = layout.buildDirectory.dir("generated/appspiriment/kotlin")

fun registerConstantsGenerator(
    taskName: String,
    tomlFile: File,
    outputRelPath: String,
    tomlNameConst: String,
    tomlNameValue: String,
    versionConst: String,
    contentsConst: String,
    refsConst: String,
    /** Extra top-level `internal const val` entries to emit (name → value). */
    extraConstants: Map<String, String> = emptyMap(),
) = tasks.register(taskName) {
    group = "versioning"
    description = "Generates $outputRelPath with the baked-in TOML catalog and per-artifact versions."

    inputs.file(tomlFile)
    inputs.property("pluginVersion",       currentPluginVersion)
    inputs.property("utilsVersion",        currentUtilsVersion)
    inputs.property("logutilsVersion",     currentLogutilsVersion)
    inputs.property("composeUtilsVersion", currentComposeVersion)
    inputs.property("composeKmpVersion",   currentComposeKmpVersion)
    inputs.property("updateUtilsVersion",  currentUpdateVersion)
    inputs.property("bomVersion",          currentBomVersion)
    outputs.dir(generatedSourceDir)

    val outFile = generatedSourceDir.map { it.file(outputRelPath) }

    doLast {
        if (!tomlFile.exists()) {
            logger.warn("⚠️ ${tomlFile.name} not found — skipping $outputRelPath generation")
            return@doLast
        }

        val rawToml = tomlFile.readText()
        // Replace per-artifact version placeholders — ORDER MATTERS: longer/more-specific
        // tokens must be replaced before shorter ones that are substrings of them:
        //   LOGUTILS_VERSION    ends with UTILS_VERSION
        //   COMPOSE_UTILS_VERSION  ends with UTILS_VERSION
        //   UPDATE_UTILS_VERSION   ends with UTILS_VERSION
        // If UTILS_VERSION were replaced first it would corrupt the others.
        val processedToml = rawToml
            .replace("COMPOSE_UTILS_VERSION", currentComposeVersion)  // 21 chars — longest
            .replace("COMPOSE_KMP_VERSION",   currentComposeKmpVersion)
            .replace("UPDATE_UTILS_VERSION",  currentUpdateVersion)   // 20 chars
            .replace("LOGUTILS_VERSION",      currentLogutilsVersion) // 16 chars
            .replace("UTILS_VERSION",         currentUtilsVersion)    // 13 chars — last of the *_UTILS set
            .replace("PLUGIN_VERSION",        currentPluginVersion)
            .replace("BOM_VERSION",           currentBomVersion)
            .replace("LIBVERSION",            currentPluginVersion)   // backward-compat alias

        val tomlLines = processedToml.lines()

        var currentSection = ""
        val versionRefs  = mutableListOf<String>()
        val pluginRefs   = mutableListOf<String>()
        val libraryRefs  = mutableListOf<List<String>>()

        tomlLines.forEach { line: String ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("[") && trimmed.endsWith("]") -> currentSection = trimmed
                trimmed.isEmpty() || trimmed.startsWith("#") -> { /* skip */ }
                else -> when (currentSection) {
                    "[versions]" -> {
                        val key = trimmed.substringBefore("=").trim()
                        if (key.isNotEmpty()) versionRefs.add("\"$key\"")
                    }
                    "[plugins]" -> {
                        val idMatch = Regex("""id\s*=\s*"([^"]+)"""").find(trimmed)
                        val id = idMatch?.groupValues?.get(1) ?: trimmed.substringBefore("=").trim()
                        if (id.isNotEmpty() && !id.contains("{")) pluginRefs.add("\"$id\"")
                    }
                    "[libraries]" -> {
                        val g = Regex("""group\s*=\s*"([^"]+)"""").find(trimmed)?.groupValues?.get(1)
                        val n = Regex("""name\s*=\s*"([^"]+)"""").find(trimmed)?.groupValues?.get(1)
                        if (g != null && n != null) libraryRefs.add(listOf(g, n))
                    }
                }
            }
        }

        val escapedToml = processedToml
            .replace("\\", "\\\\")
            .replace("\$", "\${'$'}")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")

        val libRefsList = libraryRefs.joinToString(", ") { (g, n) -> "Pair(\"$g\", \"$n\")" }

        val extraConstLines = extraConstants.entries.joinToString("\n") { (name, value) ->
            "internal const val $name = \"$value\""
        }

        val file = outFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package io.github.appspiriment.kolt.conventions.extensions

            import io.github.appspiriment.kolt.conventions.extensions.KoltLibRef

            internal const val $tomlNameConst = "$tomlNameValue"
            internal const val $versionConst = "$currentPluginVersion"
            internal const val $contentsConst = "$escapedToml"
            $extraConstLines
            internal val $refsConst = KoltLibRef(
                versions = listOf(${versionRefs.joinToString(", ")}),
                plugins = listOf(${pluginRefs.joinToString(", ")}),
                libraries = listOf($libRefsList)
            )
            """.trimIndent()
        )

        logger.lifecycle("✅ $outputRelPath generated (plugin=$currentPluginVersion, bom=$currentBomVersion)")
    }
}

val catalogDir = rootDir.parentFile.resolve("gradle")

val updateLibFileVersion = registerConstantsGenerator(
    taskName = "updateLibFileVersion",
    tomlFile = catalogDir.resolve("koltlibs.versions.toml"),
    outputRelPath = "io/github/appspiriment/kolt/conventions/extensions/Constants.kt",
    tomlNameConst = "koltTomlName",
    tomlNameValue = "koltlibs",
    versionConst = "libVersion",
    contentsConst = "koltTomlContents",
    refsConst = "koltLibRefs",
    extraConstants = mapOf(
        "pluginVersion"        to currentPluginVersion,
        "utilsVersion"         to currentUtilsVersion,
        "logutilsVersion"      to currentLogutilsVersion,
        "composeUtilsVersion"  to currentComposeVersion,
        "composeKmpVersion"    to currentComposeKmpVersion,
        "updateUtilsVersion"   to currentUpdateVersion,
        "bomVersion"           to currentBomVersion,
    ),
)

val updateKmpLibFileVersion = registerConstantsGenerator(
    taskName = "updateKmpLibFileVersion",
    tomlFile = catalogDir.resolve("kmplibs.versions.toml"),
    outputRelPath = "io/github/appspiriment/kolt/conventions/extensions/KmpConstants.kt",
    tomlNameConst = "kmpTomlName",
    tomlNameValue = "kmplibs",
    versionConst = "kmpLibVersion",
    contentsConst = "kmpTomlContents",
    refsConst = "kmpLibRefs",
)

// ────────────────────────────────────────────────
// 4. THEME TEMPLATE SYNC
// Packs the compose-utils theme XMLs into the plugin JAR as classpath resources.
// Source: the in-repo compose-utils module when available, committed defaults otherwise.
// ────────────────────────────────────────────────
val composeUtilsResDir = rootDir.parentFile.resolve("libs/compose-utils/src/main/res")
val generatedResourcesDir = layout.buildDirectory.dir("generated-resources")

val generateThemeTemplates = tasks.register<Sync>("generateThemeTemplates") {
    group = "appspiriment"
    description = "Packs compose-utils theme templates into the plugin JAR."

    if (composeUtilsResDir.isDirectory) {
        from(composeUtilsResDir) {
            include("values/colors.xml")
            include("values-night/colors.xml")
            include("values/dimens.xml")
            rename("colors.xml", "appspiriment_colors.xml")
            rename("dimens.xml", "appspiriment_dimens.xml")
            into("appspiriment/templates")
        }
        doLast { logger.lifecycle("✅ Theme templates synced from compose-utils source.") }
    } else {
        from(layout.projectDirectory.dir("src/theme-templates"))
        doLast {
            logger.lifecycle(
                "ℹ️  compose-utils source not found at ${composeUtilsResDir.path}; " +
                    "using committed theme-template defaults."
            )
        }
    }

    into(generatedResourcesDir)
}

// ────────────────────────────────────────────────
// 4b. STEERING DOCS
// Packs AI-agent steering docs into the plugin JAR as classpath resources.
// ────────────────────────────────────────────────
val generatedSteeringResourcesDir = layout.buildDirectory.dir("generated-steering-resources")

val generateSteeringTemplates = tasks.register<Sync>("generateSteeringTemplates") {
    group = "appspiriment"
    description = "Packs AI-agent steering docs from project-templates/ into the plugin JAR."

    from(rootProject.projectDir.parentFile.resolve("project-templates")) {
        include("CLAUDE.md")
        include("AGENTS.md")
        include("docs/CODING_STANDARDS.md")
        include("docs/ARCHITECTURE.md")
        include("docs/TESTING.md")
        include("templates/android-project/**")
        include("templates/kmp-project/**")
        into("appspiriment/steering")
    }

    into(generatedSteeringResourcesDir)

    doLast { logger.lifecycle("✅ Steering docs packed from project-templates/.") }
}

// ────────────────────────────────────────────────
// 5. PLUGIN DEFINITIONS (Android + KMP, one module)
// ────────────────────────────────────────────────
gradlePlugin {
    plugins {
        // ----- Android -----
        create("androidApplication") {
            id = "io.github.appspiriment.kolt.application"
            displayName = "Appspiriment Application"
            description = "Standardized setup for Android application modules (Compose + Hilt by default)."
            implementationClass = "io.github.appspiriment.kolt.conventions.plugins.AndroidApplicationConventionPlugin"
        }
        create("androidLibrary") {
            id = "io.github.appspiriment.kolt.library"
            displayName = "Appspiriment Library"
            description = "Minimal Android library module setup."
            implementationClass = "io.github.appspiriment.kolt.conventions.plugins.AndroidLibraryConventionPlugin"
        }
        create("androidHiltLibrary") {
            id = "io.github.appspiriment.kolt.library-hilt"
            displayName = "Appspiriment Library (Hilt)"
            description = "Android library module with Hilt dependency injection."
            implementationClass = "io.github.appspiriment.kolt.conventions.plugins.AndroidLibraryHiltConventionPlugin"
        }
        create("androidComposeLibrary") {
            id = "io.github.appspiriment.kolt.library-compose"
            displayName = "Appspiriment Library (Compose)"
            description = "Android library module with Jetpack Compose UI."
            implementationClass = "io.github.appspiriment.kolt.conventions.plugins.AndroidLibraryComposeConventionPlugin"
        }
        create("androidHiltComposeLibrary") {
            id = "io.github.appspiriment.kolt.library-hilt-compose"
            displayName = "Appspiriment Library (Hilt + Compose)"
            description = "Android library module with both Hilt and Jetpack Compose."
            implementationClass = "io.github.appspiriment.kolt.conventions.plugins.AndroidLibraryHiltComposeConventionPlugin"
        }
        create("androidDataLayerLibrary") {
            id = "io.github.appspiriment.kolt.data"
            displayName = "Appspiriment Data Layer"
            description = "Data layer setup with opt-in Room, Retrofit, DataStore, Security, and WorkManager."
            implementationClass = "io.github.appspiriment.kolt.conventions.plugins.feature.AndroidDataLayerConventionPlugin"
        }
        // ----- KMP -----
        create("kmpLibrary") {
            id = "io.github.appspiriment.kolt.kmp.library"
            displayName = "Appspiriment KMP Library"
            description = "Base KMP shared module setup (Android target always on, iOS/Desktop/WASM opt-in)."
            implementationClass = "io.github.appspiriment.kolt.conventions.plugins.KmpLibraryConventionPlugin"
        }
        create("kmpLibraryCompose") {
            id = "io.github.appspiriment.kolt.kmp.library-compose"
            displayName = "Appspiriment KMP Library (Compose)"
            description = "KMP shared module with Compose Multiplatform UI."
            implementationClass = "io.github.appspiriment.kolt.conventions.plugins.KmpLibraryComposeConventionPlugin"
        }
        create("kmpLibraryKoin") {
            id = "io.github.appspiriment.kolt.kmp.library-koin"
            displayName = "Appspiriment KMP Library (Koin)"
            description = "KMP shared module with Koin dependency injection."
            implementationClass = "io.github.appspiriment.kolt.conventions.plugins.KmpLibraryKoinConventionPlugin"
        }
        create("kmpLibraryKoinCompose") {
            id = "io.github.appspiriment.kolt.kmp.library-koin-compose"
            displayName = "Appspiriment KMP Library (Koin + Compose)"
            description = "KMP shared module with Koin DI and Compose Multiplatform UI."
            implementationClass = "io.github.appspiriment.kolt.conventions.plugins.KmpLibraryKoinComposeConventionPlugin"
        }
        create("kmpData") {
            id = "io.github.appspiriment.kolt.kmp.data"
            displayName = "Appspiriment KMP Data Layer"
            description = "KMP data layer with opt-in SQLDelight, Ktor, DataStore, and Serialization."
            implementationClass = "io.github.appspiriment.kolt.conventions.plugins.feature.KmpDataLayerConventionPlugin"
        }
        create("kmpApplication") {
            id = "io.github.appspiriment.kolt.kmp.application"
            displayName = "Appspiriment KMP Application"
            description = "Android host app module for a KMP project (Compose + coroutines by default)."
            implementationClass = "io.github.appspiriment.kolt.conventions.plugins.KmpApplicationConventionPlugin"
        }
    }
}

// ────────────────────────────────────────────────
// 6. KOTLIN & COMPILATION
// ────────────────────────────────────────────────
kotlin {
    sourceSets.main {
        kotlin.srcDir(updateLibFileVersion)
        kotlin.srcDir(updateKmpLibFileVersion)
        resources.srcDir(generateThemeTemplates.map { it.destinationDir })
        resources.srcDir(generateSteeringTemplates.map { it.destinationDir })
    }
}

val javaVersion = libs.versions.javaVersion.get().toInt()
java { toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion)) } }

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn")
    }
}

// ────────────────────────────────────────────────
// 7. DEPENDENCIES
// ────────────────────────────────────────────────
// Separate resolvable configuration for TestKit plugin classpath.
// Plugin deps are compileOnly (not runtime) so withPluginClasspath() misses them.
// This configuration resolves them with full transitive closure (including
// runtime-only deps like kotlin-build-tools-impl) for the TestKit metadata task.
val pluginTestClasspath by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
    }
}

dependencies {
    compileOnly(gradleApi())
    // compileOnly — NOT implementation — so these are NOT exported into each consumer
    // project's per-project classloader scope. The root buildscript puts them on a single
    // shared classpath; every module then gets the same ClassLoader instance, preventing
    // KotlinNativeBundleBuildService / LibraryExtension type-identity conflicts.
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
    compileOnly(libs.compose.multiplatform.gradle.plugin)
    testImplementation(gradleTestKit())
    testImplementation(libs.junit.test)

    // Same deps on the resolvable test configuration so TestKit gets full runtime classpath
    pluginTestClasspath(libs.android.gradle.plugin)
    pluginTestClasspath(libs.kotlin.gradle.plugin)
    pluginTestClasspath(libs.ksp.gradle.plugin)
    pluginTestClasspath(libs.compose.compiler.gradle.plugin)
    pluginTestClasspath(libs.compose.multiplatform.gradle.plugin)
}

// Include full transitive runtime classpath of plugin deps in TestKit metadata.
// withPluginClasspath() only reads runtimeClasspath; adding pluginTestClasspath here
// makes KGP/AGP/KSP/Compose and all their runtime-only deps (e.g. kotlin-build-tools-impl)
// available when TestKit test projects apply convention plugins.
tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
    pluginClasspath.from(pluginTestClasspath)
}

// ────────────────────────────────────────────────
// 8. PUBLISHING
//
// Artifacts published from this module:
//   • pluginMaven                  — the convention-plugin JAR + sources + javadoc
//   • <plugin>PluginMarkerMaven    — one marker POM per plugin ID (12 total, auto-created)
//   • appspirimentLibsCatalog      — koltlibs TOML version catalog
//   • kmpLibsCatalog               — kmplibs TOML version catalog
//
// Publishing targets:
//   Local:   ./gradlew -p build-logic :conventions:publishToMavenLocal
//   Release: ./gradlew -p build-logic :conventions:publishAllPublicationsToMavenCentralRepository -PisRelease
//
// Credentials are read from Gradle properties — never hardcoded here:
//   mavenCentralUsername / mavenCentralPassword  — Central Portal user token
//   signingInMemoryKey / signingInMemoryKeyPassword  — GPG key (base64-armored)
// Set in ~/.gradle/gradle.properties locally, or via ORG_GRADLE_PROJECT_* env vars in CI.
// ────────────────────────────────────────────────

publishing {
    publications {
        create<MavenPublication>("appspirimentLibsCatalog") {
            artifactId = "koltlibs"
            artifact(rootDir.parentFile.resolve("gradle/koltlibs.versions.toml")) {
                extension = "toml"
            }
        }
        create<MavenPublication>("kmpLibsCatalog") {
            artifactId = "kmplibs"
            artifact(rootDir.parentFile.resolve("gradle/kmplibs.versions.toml")) {
                extension = "toml"
            }
        }
    }

    publications.withType<MavenPublication> {
        version = currentPluginVersion

        if (!name.endsWith("PluginMarkerMaven")) {
            pom {
                name.set("Appspiriment Convention Plugins")
                description.set(
                    "Convention Gradle plugins (Android + KMP) and version catalogs " +
                        "for Appspiriment projects."
                )
                url.set("https://github.com/appspiriment/UtilsLibs")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("appspiriment")
                        name.set("Appspiriment")
                        url.set("https://github.com/appspiriment")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/appspiriment/UtilsLibs.git")
                    developerConnection.set("scm:git:ssh://github.com/appspiriment/UtilsLibs.git")
                    url.set("https://github.com/appspiriment/UtilsLibs")
                }
            }
        }
    }
}

// Wire up vanniktech Central Portal publishing and in-memory signing — gated on -PisRelease
// so that `publishToMavenLocal` works without any credentials.
extensions.configure<MavenPublishBaseExtension> {
    if (providers.gradleProperty("isRelease").isPresent) {
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
        signAllPublications()
    }
}

tasks.withType<Jar>().matching { it.name.contains("sourcesJar", ignoreCase = true) }.configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(updateLibFileVersion, updateKmpLibFileVersion)
}

// publishDev: bump plugin version counter, then publishToMavenLocal
tasks.register("publishDev") {
    group = "publishing"
    description = "Bumps the plugin DEV counter then publishes plugin + catalogs to MavenLocal."
    dependsOn(bumpPluginVersion)
    finalizedBy("publishToMavenLocal")
    doLast {
        logger.lifecycle("✅ Plugin version bumped → publishing to ~/.m2")
    }
}

// publishToMavenCentral convenience alias (consistent naming with libs modules).
// Requires -PisRelease + credentials in ~/.gradle/gradle.properties or ORG_GRADLE_PROJECT_* env vars.
tasks.register("publishToSonatype") {
    group = "publishing"
    description = "Alias for publishAllPublicationsToMavenCentralRepository. Requires -PisRelease."
    dependsOn("publishAllPublicationsToMavenCentralRepository")
}

// ────────────────────────────────────────────────
// 9. SIGNING
// Signing is handled by vanniktech via in-memory GPG key.
// signAllPublications() is called inside the mavenPublishing {} block above,
// gated on -PisRelease. No separate signing {} block is needed.
//
// Required Gradle properties (in ~/.gradle/gradle.properties or ORG_GRADLE_PROJECT_* in CI):
//   signingInMemoryKey          — base64-armored GPG private key
//   signingInMemoryKeyPassword  — GPG key passphrase
// ────────────────────────────────────────────────

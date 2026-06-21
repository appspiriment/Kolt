import java.util.Properties

// Shared plugin classpath — must appear early in the root build script.
//
// Convention plugins in build-logic/conventions mark KGP, AGP, KSP, and Compose as
// `compileOnly` so they are NOT exported into each lib module's per-project classloader
// scope. Placing them here instead means every module gets these from ONE shared
// ClassLoader, eliminating KotlinNativeBundleBuildService / LibraryExtension
// type-identity conflicts in multi-module builds.
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.10")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.6")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.3.10")
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.8.0")
    }
}

plugins {
    alias(libs.plugins.dokka)
}

// Root aggregator for the UtilsLibs composite build.
// Convention plugins live in the included build `build-logic` and are applied
// directly by the modules under `libs/`. This root script stays thin.

// ── Per-artifact versioning (single source of truth: version.properties) ──────
val versionPropsFile = rootProject.file("version.properties")
val versionProps: Properties = Properties().apply {
    if (versionPropsFile.exists()) versionPropsFile.inputStream().use { load(it) }
}

fun computeVersion(majorKey: String, devKey: String): String {
    val major = versionProps.getProperty(majorKey, "1.0.0")
    val dev = versionProps.getProperty(devKey, "0").toInt()
    return if (project.hasProperty("isRelease")) major
    else "$major.dev-${dev.toString().padStart(2, '0')}"
}

val pluginVersion       by lazy { computeVersion("PLUGIN_MAJOR",        "PLUGIN_DEV")        }
val utilsVersion        by lazy { computeVersion("UTILS_MAJOR",         "UTILS_DEV")         }
val logutilsVersion     by lazy { computeVersion("LOGUTILS_MAJOR",      "LOGUTILS_DEV")      }
val composeUtilsVersion by lazy { computeVersion("COMPOSE_UTILS_MAJOR",  "COMPOSE_UTILS_DEV") }
val composeKmpVersion   by lazy { computeVersion("COMPOSE_KMP_MAJOR",    "COMPOSE_KMP_DEV")   }
val updateUtilsVersion  by lazy { computeVersion("UPDATE_UTILS_MAJOR",   "UPDATE_UTILS_DEV")  }
val locationVersion     by lazy { computeVersion("LOCATION_MAJOR",       "LOCATION_DEV")      }
val bomVersion          by lazy { versionProps.getProperty("BOM_VERSION", "2025.06.1")       }

allprojects {
    group = "io.github.appspiriment.kolt"
}

// Expose per-lib versions as root extra properties so submodule build scripts
// (e.g. libs/bom/build.gradle.kts) can read them without re-parsing the file.
extra["utilsVersion"]        = utilsVersion
extra["logutilsVersion"]     = logutilsVersion
extra["composeUtilsVersion"] = composeUtilsVersion
extra["composeKmpVersion"]   = composeKmpVersion
extra["updateUtilsVersion"]  = updateUtilsVersion
extra["locationVersion"]     = locationVersion
extra["bomVersion"]          = bomVersion
extra["pluginVersion"]       = pluginVersion

subprojects {
    version = when (name) {
        "utils"         -> utilsVersion
        "logutils"      -> logutilsVersion
        "compose-utils" -> composeUtilsVersion
        "compose-kmp"   -> composeKmpVersion
        "update-utils"  -> updateUtilsVersion
        "location"      -> locationVersion
        "bom"           -> bomVersion
        else            -> pluginVersion
    }
}

// ── Publishing aggregates ─────────────────────────────────────────────────────
//
// MavenLocal (no signing required — safe to run non-interactively):
//   ./gradlew publishAllToMavenLocal -PisRelease
//
// Maven Central / Sonatype (MUST add -PsignRelease; run each project separately due to a known
// vanniktech SonatypeRepositoryBuildService classloader conflict in multi-module builds):
//   ./gradlew :libs:bom:publishAllPublicationsToMavenCentralRepository -PisRelease -PsignRelease
//   ./gradlew :libs:utils:publishAllPublicationsToMavenCentralRepository -PisRelease -PsignRelease
//   ./gradlew :libs:logutils:publishAllPublicationsToMavenCentralRepository -PisRelease -PsignRelease
//   ./gradlew :libs:compose-utils:publishAllPublicationsToMavenCentralRepository -PisRelease -PsignRelease
//   ./gradlew :libs:update-utils:publishAllPublicationsToMavenCentralRepository -PisRelease -PsignRelease
//   ./gradlew -p build-logic :conventions:publishToSonatype -PisRelease -PsignRelease
//
// Or use the convenience script which does all of the above in sequence:
//   ./scripts/publish-release.sh
//
// Required credentials (~/.gradle/gradle.properties or env vars):
//   ossrhUsername / OSSRH_USERNAME   — Sonatype OSSRH user token
//   ossrhPassword / OSSRH_PASSWORD   — Sonatype OSSRH user token password
//   signing requires GPG: run from an interactive terminal so GPG can prompt for passphrase

tasks.register("publishAllToMavenLocal") {
    group = "publishing"
    description = "Publishes ALL libs + BOM + convention plugins to the local Maven repository (~/.m2)."
    // Convention plugins + catalogs (runs in the included build's context)
    dependsOn(gradle.includedBuild("build-logic").task(":conventions:publishToMavenLocal"))
    // BOM first so libs can resolve constraints when published together
    dependsOn(":libs:bom:publishToMavenLocal")
    // Runtime libraries
    dependsOn(":libs:logutils:publishToMavenLocal")
    dependsOn(":libs:utils:publishToMavenLocal")
    dependsOn(":libs:compose-kmp:publishToMavenLocal")
    dependsOn(":libs:compose-utils:publishToMavenLocal")
    dependsOn(":libs:update-utils:publishToMavenLocal")
    dependsOn(":libs:location:publishToMavenLocal")
}

// ── Version bump tasks (delegate to build-logic) ──────────────────────────────
// For bumping specific lib versions run the task directly on the build-logic
// included build — see `docs/PLUGIN_DEV.md` for the full workflow.

tasks.register("bumpPluginVersion") {
    group = "versioning"
    description = "Bumps the PLUGIN_DEV counter — convention plugins + version catalogs."
    dependsOn(gradle.includedBuild("build-logic").task(":conventions:bumpPluginVersion"))
}

tasks.register("bumpUtilsVersion") {
    group = "versioning"
    description = "Bumps the UTILS_DEV counter — libs:utils."
    dependsOn(gradle.includedBuild("build-logic").task(":conventions:bumpUtilsVersion"))
}

tasks.register("bumpLogutilsVersion") {
    group = "versioning"
    description = "Bumps the LOGUTILS_DEV counter — libs:logutils."
    dependsOn(gradle.includedBuild("build-logic").task(":conventions:bumpLogutilsVersion"))
}

tasks.register("bumpComposeUtilsVersion") {
    group = "versioning"
    description = "Bumps the COMPOSE_UTILS_DEV counter — libs:compose-utils."
    dependsOn(gradle.includedBuild("build-logic").task(":conventions:bumpComposeUtilsVersion"))
}

tasks.register("bumpUpdateUtilsVersion") {
    group = "versioning"
    description = "Bumps the UPDATE_UTILS_DEV counter — libs:update-utils."
    dependsOn(gradle.includedBuild("build-logic").task(":conventions:bumpUpdateUtilsVersion"))
}

tasks.register("bumpLocationVersion") {
    group = "versioning"
    description = "Bumps the LOCATION_DEV counter — libs:location."
    dependsOn(gradle.includedBuild("build-logic").task(":conventions:bumpLocationVersion"))
}

tasks.register("bumpBomVersion") {
    group = "versioning"
    description = "Bumps the BOM calendar patch counter — run after any lib version change."
    dependsOn(gradle.includedBuild("build-logic").task(":conventions:bumpBomVersion"))
}

// Convenience: bump everything (useful for first release or major resets)
tasks.register("bumpDevVersion") {
    group = "versioning"
    description = "Bumps ALL DEV counters and the BOM version. Prefer per-artifact tasks in normal workflow."
    dependsOn(gradle.includedBuild("build-logic").task(":conventions:bumpAllVersions"))
}

// ── Dependency substitution ───────────────────────────────────────────────────
// When convention plugins add utils/logutils/compose-utils as Maven coordinates
// (io.github.appspiriment.kolt:*), substitute them with the local project during
// development so consumers build against the current source, not a published artifact.
//
// The BOM platform dependency must use `platform(...)` on both sides of the substitution
// so Gradle preserves the platform semantics correctly (Gradle 6+ feature).
subprojects {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            // Platform (BOM) substitution — must use platform() on both sides
            substitute(platform(module("io.github.appspiriment.kolt:kolt-bom")))
                .using(platform(project(":libs:bom")))
            // Library substitutions
            substitute(module("io.github.appspiriment.kolt:utils"))
                .using(project(":libs:utils"))
            substitute(module("io.github.appspiriment.kolt:logutils"))
                .using(project(":libs:logutils"))
            substitute(module("io.github.appspiriment.kolt:compose-kmp"))
                .using(project(":libs:compose-kmp"))
            substitute(module("io.github.appspiriment.kolt:compose"))
                .using(project(":libs:compose-utils"))
        }
    }
}

// ── API reference (Dokka) ──────────────────────────────────────────────────────
// Each lib module applies `org.jetbrains.dokka` individually; the root aggregates
// them into one browsable HTML tree via `dokkaHtmlMultiModule`. The demo site's
// docs/api/ section embeds this output — see demo-web/docs/api/index.html.
tasks.register<Copy>("copyDokkaToDocs") {
    group = "documentation"
    description = "Copies the aggregated Dokka HTML output into demo-web/docs/api/."
    dependsOn("dokkaHtmlMultiModule")
    from(layout.buildDirectory.dir("dokka/htmlMultiModule"))
    into(rootProject.file("demo-web/docs/api"))
}

package io.github.appspiriment.kolt.conventions.extensions

// ────────────────────────────────────────────────
// Plugin alias lists — passed to applyPluginFromLibs(kmpLibs to <list>)
// These reference plugin aliases in kmplibs.versions.toml [plugins] section.
// All library coordinates are hardcoded in KmpLibraryCoordinates.kt.
// ────────────────────────────────────────────────

/** Mandatory plugins for every KMP shared module. */
val kmpBasePluginList: List<String> = listOf(
    "kotlin-multiplatform",
    "android-library"
)

/** Compose Multiplatform plugins. */
val kmpComposePluginList: List<String> = listOf(
    "compose-multiplatform",
    "kotlin-compose-compiler"
)

/** KSP plugin (required for Koin annotations, Ktorfit, Room 3.0). */
val kmpKoinPluginList: List<String> = listOf(
    "devtools-ksp"
)

/** kotlinx.serialization plugin (used by Ktor, Ktorfit, and standalone serialization). */
val kmpSerializationPluginList: List<String> = listOf(
    "kotlin-serialization"
)

/** SQLDelight plugin. */
val kmpSqlDelightPluginList: List<String> = listOf(
    "sqldelight"
)

/** Ktorfit plugin (applies KSP + Ktorfit code generator). */
val kmpKtorfitPluginList: List<String> = listOf(
    "devtools-ksp",
    "ktorfit"
)

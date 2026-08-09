// Plugin catalog data — source: Standards/KOLT.md §2-§5
export const PLUGINS = [
    {
        id: 'android-application',
        pluginId: 'io.github.appspiriment.kolt.application',
        catalogAlias: 'kolt-application',
        family: 'Android',
        title: 'Android Application',
        summary: 'AGP application host module with Hilt, Compose, and Kolt libs wired by default.',
        wires: 'AGP application + Kotlin Android + Hilt + Compose + Compose compiler + stdlib + utils + logutils + Room-optional + debug/release build types with a `.dev` suffix on debug.',
        usedFor: 'The Android app module (host of your app — :app).',
        dsl: `kolt {
    enableUtils.set(true)                          // default true — adds utils + logutils
    enableMinify.set(false)                        // default false — enables R8 on release

    addDevSuffixToDebug.set(true)                  // default true — applicationId.dev suffix on debug
    debugApplicationIdSuffix.set(".dev")

    appendTimestampToDebugVersion.set(true)        // default true — versionName timestamp on debug
    debugVersionTimestampPattern.set("yyyyMMdd-HHmm")

    scaffoldThemeResources.set(true)               // default true — writes theme XMLs on first build
}`,
        snippet: `plugins { id("io.github.appspiriment.kolt.application") }

android { namespace = "com.example.myapp" }`,
    },
    {
        id: 'android-library',
        pluginId: 'io.github.appspiriment.kolt.library',
        catalogAlias: 'kolt-library',
        family: 'Android',
        title: 'Android Library',
        summary: 'Bare Android library module — no Compose, no DI. Good for pure domain/data modules.',
        wires: 'AGP library + Kotlin Android + stdlib + utils + logutils.',
        usedFor: 'Android domain modules with no UI and no dependency injection.',
        dsl: `kolt {
    enableUtils.set(true)
    enableMinify.set(false)
}`,
        snippet: `plugins { id("io.github.appspiriment.kolt.library") }`,
    },
    {
        id: 'android-library-compose',
        pluginId: 'io.github.appspiriment.kolt.library-compose',
        catalogAlias: 'kolt-library-compose',
        family: 'Android',
        title: 'Android Library + Compose',
        summary: 'Android library with the Compose UI stack wired in, but no Hilt.',
        wires: '`library` baseline + Compose + Compose compiler.',
        usedFor: 'UI-only Android modules that don’t need dependency injection.',
        dsl: `kolt {
    enableUtils.set(true)
    scaffoldThemeResources.set(true)   // default true for compose-enabled modules
}`,
        snippet: `plugins { id("io.github.appspiriment.kolt.library-compose") }`,
    },
    {
        id: 'android-library-hilt',
        pluginId: 'io.github.appspiriment.kolt.library-hilt',
        catalogAlias: 'kolt-library-hilt',
        family: 'Android',
        title: 'Android Library + Hilt',
        summary: 'Android library with Hilt + KSP wired in, no Compose.',
        wires: '`library` baseline (no Compose) + Hilt + KSP.',
        usedFor: 'Non-UI Android modules that need dependency injection (e.g. repositories, use-case modules).',
        dsl: `kolt {
    enableUtils.set(true)
}`,
        snippet: `plugins { id("io.github.appspiriment.kolt.library-hilt") }`,
    },
    {
        id: 'android-library-hilt-compose',
        pluginId: 'io.github.appspiriment.kolt.library-hilt-compose',
        catalogAlias: 'kolt-library-hilt-compose',
        family: 'Android',
        title: 'Android Library + Hilt + Compose',
        summary: 'The full Android feature-module plugin — Hilt + Compose together.',
        wires: 'AGP library + Kotlin + Hilt + KSP + Compose + Compose compiler.',
        usedFor: 'Android feature/UI modules — the most common module type in this stack.',
        dsl: `kolt {
    enableUtils.set(true)
    scaffoldThemeResources.set(true)
}`,
        snippet: `plugins { id("io.github.appspiriment.kolt.library-hilt-compose") }`,
    },
    {
        id: 'android-data-layer',
        pluginId: 'io.github.appspiriment.kolt.data',
        catalogAlias: 'kolt-data',
        family: 'Android',
        title: 'Android Data Layer',
        summary: '`library-hilt` baseline plus opt-in Room / Retrofit / DataStore / Security / WorkManager.',
        wires: '`library-hilt` baseline + opt-in persistence/networking via the `dataLayer {}` block.',
        usedFor: 'Android data-layer modules (repositories backed by Room/Retrofit/etc).',
        dsl: `dataLayer {
    room {
        enabled.set(true)
        usePaging.set(false)
    }
    retrofit {
        enabled.set(true)
        useChucker.set(true)
        useKotlinSerialization.set(false)
    }
    security { enabled.set(false) }
    dataStore { enabled.set(false) }
    workManager { enabled.set(false) }
}`,
        snippet: `plugins { id("io.github.appspiriment.kolt.data") }`,
    },
    {
        id: 'kmp-library',
        pluginId: 'io.github.appspiriment.kolt.kmp.library',
        catalogAlias: 'kmp-library',
        family: 'KMP',
        title: 'KMP Library',
        summary: 'Baseline Kotlin Multiplatform library — Android always, iOS/Desktop/WASM opt-in.',
        wires: 'AGP library + Kotlin Multiplatform (Android + optional iOS/Desktop/WASM) + Coroutines + stdlib + utils + logutils.',
        usedFor: 'KMP shared modules (ViewModels + domain logic) with no DI and no Compose.',
        dsl: `kmp {
    enableIos.set(false)      // adds iosArm64 + iosSimulatorArm64 + iosX64
    enableDesktop.set(false)  // adds jvm "desktop"
    enableWasm.set(false)     // adds wasmJs (browser)
    enableUtils.set(true)
}`,
        snippet: `plugins { id("io.github.appspiriment.kolt.kmp.library") }`,
    },
    {
        id: 'kmp-library-compose',
        pluginId: 'io.github.appspiriment.kolt.kmp.library-compose',
        catalogAlias: 'kmp-library-compose',
        family: 'KMP',
        title: 'KMP Library + Compose',
        summary: 'KMP library with Compose Multiplatform wired in, no Koin.',
        wires: '`kmp.library` baseline + Compose Multiplatform + Compose compiler.',
        usedFor: 'KMP UI modules that manage their own DI or have none.',
        dsl: `kmp {
    enableIos.set(true)
    enableDesktop.set(true)
}`,
        snippet: `plugins { id("io.github.appspiriment.kolt.kmp.library-compose") }`,
    },
    {
        id: 'kmp-library-koin',
        pluginId: 'io.github.appspiriment.kolt.kmp.library-koin',
        catalogAlias: 'kmp-library-koin',
        family: 'KMP',
        title: 'KMP Library + Koin',
        summary: 'KMP library with Koin KMP wired in, no Compose.',
        wires: '`kmp.library` + Koin KMP.',
        usedFor: 'KMP shared modules (ViewModel + domain) that need dependency injection.',
        dsl: `kmp {
    enableIos.set(true)
    enableDesktop.set(true)
}`,
        snippet: `plugins { id("io.github.appspiriment.kolt.kmp.library-koin") }`,
    },
    {
        id: 'kmp-library-koin-compose',
        pluginId: 'io.github.appspiriment.kolt.kmp.library-koin-compose',
        catalogAlias: 'kmp-library-koin-compose',
        family: 'KMP',
        title: 'KMP Library + Koin + Compose',
        summary: 'The full KMP feature-module plugin — Koin + Compose Multiplatform together.',
        wires: '`kmp.library` + Koin KMP + Compose Multiplatform + Compose compiler.',
        usedFor: 'KMP feature modules (Compose Multiplatform UI) — the most common KMP module type.',
        dsl: `kmp {
    enableIos.set(true)
    enableDesktop.set(true)
    enableWasm.set(false)
}`,
        snippet: `plugins { id("io.github.appspiriment.kolt.kmp.library-koin-compose") }`,
    },
    {
        id: 'kmp-data',
        pluginId: 'io.github.appspiriment.kolt.kmp.data',
        catalogAlias: 'kmp-data',
        family: 'KMP',
        title: 'KMP Data Layer',
        summary: 'KMP library baseline plus opt-in SQLDelight / Room KMP / Ktor / Ktorfit / Retrofit / DataStore.',
        wires: 'KMP library baseline + opt-in persistence/networking via `kmpDataLayer {}`.',
        usedFor: 'KMP data-layer modules shared across Android/iOS/Desktop.',
        dsl: `kmpDataLayer {
    sqlDelight { enabled.set(true) }       // full KMP, recommended
    // room { enabled.set(true); schemaVersion.set(1) }  // alpha, no wasmJs

    ktor { enabled.set(true); useLogging.set(false) }
    // ktorfit { enabled.set(true) }
    // retrofit { enabled.set(true) }      // androidMain only, not shared

    dataStore { enabled.set(false) }
    serialization { enabled.set(false) }
}`,
        snippet: `plugins { id("io.github.appspiriment.kolt.kmp.data") }`,
    },
    {
        id: 'kmp-application',
        pluginId: 'io.github.appspiriment.kolt.kmp.application',
        catalogAlias: 'kmp-application',
        family: 'KMP',
        title: 'KMP Application',
        summary: 'Android application host for a KMP project.',
        wires: 'AGP application + Kotlin Android + Compose UI launcher for a KMP project’s Android target.',
        usedFor: 'The Android app module (:androidApp) that hosts a KMP project’s shared code.',
        dsl: `kmp {
    addDevSuffixToDebug.set(true)
    debugApplicationIdSuffix.set(".dev")
    appendTimestampToDebugVersion.set(true)
    debugVersionTimestampPattern.set("yyyyMMdd-HHmm")
}`,
        snippet: `plugins { id("io.github.appspiriment.kolt.kmp.application") }`,
    },
];

export function getPluginById(id) {
    return PLUGINS.find(p => p.id === id);
}

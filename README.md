<div align="center">

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="demo-web/img/logo_name_dark.png">
  <source media="(prefers-color-scheme: light)" srcset="demo-web/img/logo_name_light.png">
  <img alt="Kolt Brand Logo" src="demo-web/img/logo_name_light.png" width="360">
</picture>

<br>
<br>

**Unified convention Gradle plugins & lightweight runtime libraries for Android & Kotlin Multiplatform**

[![Version](https://img.shields.io/badge/version-0.1.6-1a73e8?style=flat-square)](https://github.com/appspiriment/android-kmp-utils/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/Gradle-8.12-02303A?style=flat-square&logo=gradle&logoColor=white)](https://gradle.org)
[![AI-Agent Ready](https://img.shields.io/badge/AI--Agent-Ready-81c995?style=flat-square&logo=googlebard&logoColor=white)](#-ai-agent-readiness--steering)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange?style=flat-square)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen?style=flat-square)](https://github.com/appspiriment/android-kmp-utils/pulls)

<h3>
  <a href="https://appspiriment.github.io/android-kmp-utils/">Explore Documentation Portal</a>
  <span> &bull; </span>
  <a href="https://appspiriment.github.io/android-kmp-utils/docs/api/index.html">KDoc API Reference</a>
</h3>

</div>

---

## 📖 Overview

**Kolt** (published under group `io.github.appspiriment.kolt`) is a consolidated **Gradle composite build monorepo** merging plugin configurations and cross-platform runtime assets. It provides a structured, modern build toolchain and a lightweight toolkit for teams building client applications. 

By applying opinionated convention plugins, Kolt eliminates Gradle configuration boilerplate, enforces modular boundaries, and injects runtime utilities for UI components, state management, offline database persistence, local logging, and location APIs.

### 🔑 Key Features
* 🔌 **12 Gradle Convention Plugins**: Ready-to-use plugins for Android Apps, Android Libraries, and KMP Libraries (with optional bundles for Hilt, Koin, Compose, and SQLDelight).
* 🧱 **Stateless UI Design System**: Custom Compose Multiplatform components and token systems featuring HSL-curated themes and G2 curvature continuous card corners.
* 📦 **Modular Runtime Utilities**: Dedicated libraries for cross-platform string formatting, timezone geocoding, App Startup logging, and Play Store in-app updates.
* 🤖 **AI-Agent Steering**: Structured codebase context models designed specifically to align LLMs (Gemini, Claude, Cursor) on architectural patterns.
* 🛠️ **Developer Scaffolding CLI**: Interactive scripts and Android Studio tools to create new apps or scaffold configuration guides instantly.

---

## 🤖 AI-Agent Readiness & Steering

Large Kotlin Multiplatform codebases can confuse AI code generators, leading to compilation issues, build service clashes, and incorrect platform imports. Kolt is **AI-Agent-Ready by design**, featuring standard context templates that guide coding assistants:

* 📄 **`AGENTS.md`**: Authoritative guide enforcing monorepo rules, composite included builds, and dependency structures.
* ⚡ **`CLAUDE.md`**: Provides primary compile commands, testing commands, and syntax formatting rules for rapid coding cycles.
* 🛠️ **`KOLT.md`**: Detailed table referencing convention plugin IDs, DSL options (`AppspirimentExtension`), and target rules.
* 🎨 **`CODING_STANDARDS.md`**, `ARCHITECTURE.md`, `TESTING.md`: Hard constraints definingexpect/actual models, coroutine handlers, state flows, fakes, and Turbine testing.

### Scaffolding in Downstream Projects
Apply the Kolt Gradle plugins to any target project and run the built-in scaffolding task to generate local steering templates automatically:
```bash
./gradlew scaffoldKoltResources
```
Once generated, attach these templates to your AI session inputs to instantly align models with your codebase configuration.

---

## 📦 Monorepo Modules

| Directory | Type | Artifact ID | Description |
|-----------|------|-------------|-------------|
| [`build-logic/`](build-logic/README.md) | Conventions | *Included Build* | Android & KMP convention Gradle plugins, catalog generation, publish tasks |
| [`libs/utils`](libs/utils/README.md) | Library (KMP) | `utils` | Pure-Kotlin utilities: `UiText`, `AsyncState`, string formatting, time, and timezone utilities |
| [`libs/logutils`](libs/logutils/README.md) | Library (KMP) | `logutils` | expect/actual logging; silences output automatically in production builds |
| [`libs/compose-utils`](libs/compose-utils/README.md) | Library (Android) | `compose-utils` | 100+ Material3 Compose elements, form fields, wizards, and ViewModel helpers |
| [`libs/compose-kmp`](libs/compose-kmp/README.md) | Library (KMP) | `compose-kmp` | Core cross-platform Compose UI components and HSL token structures |
| [`libs/update-utils`](libs/update-utils/README.md) | Library (Android) | `update-utils` | Firebase-steered Play Store in-app update sheets |
| [`libs/location`](libs/location/README.md) | Library (KMP) | `location` | Location geolocator wrappers and reverse-geocoding systems |
| [`intellij-plugin/`](intellij-plugin/README.md) | IDE Plugin | *Plugin* | Project creation wizards, version bumping, and publishing tools for Android Studio |

---

## 🚀 Quick Start

### 1. Configure Version Catalogs
In your target project's `settings.gradle.kts`, pull the latest catalogs from Maven Central:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("koltlibs") {
            from("io.github.appspiriment.kolt:kolt-catalog:0.1.6")
        }
        create("kmplibs") {
            from("io.github.appspiriment.kolt:kmp-catalog:0.1.6")
        }
    }
}
```

### 2. Apply Convention Plugins
Apply plugins in your module-level `build.gradle.kts` files to receive instant build configurations:

```kotlin
// Android application module
plugins {
    alias(koltlibs.plugins.kolt.application)
}

// Android library module with Compose & Hilt
plugins {
    alias(koltlibs.plugins.kolt.library.hilt.compose)
}

// Kotlin Multiplatform library module
plugins {
    alias(kmplibs.plugins.kolt.kmp.library)
}
```

### 3. Add Runtime Dependencies
To use runtime utilities directly in your modules without convention plugins, apply the Kolt Bill of Materials (BOM):

```kotlin
dependencies {
    implementation(platform("io.github.appspiriment.kolt:kolt-bom:2026.06.4"))
    implementation("io.github.appspiriment.kolt:utils")
    implementation("io.github.appspiriment.kolt:logutils")
    implementation("io.github.appspiriment.kolt:compose-kmp")
}
```

---

## 🛠️ Local Development & Shell Scripts

### Build all modules
```bash
./gradlew assemble
```

### Build a single library module
```bash
./gradlew :libs:compose-utils:assembleDebug
```

### Run plugin integration tests
```bash
./gradlew -p build-logic :conventions:test
```

### Publish to Maven Local
To test catalog dependencies in local consumer projects:
```bash
./gradlew publishToMavenLocal
```

### Scaffolding CLI Script
Bootstraps a fresh Android/KMP project with the composite Gradle build structure and AI steering configs:
```bash
# Start the interactive wizard
./scripts/new-project.sh

# Non-interactive CLI flags (CI/Automations)
./scripts/new-project.sh --type kmp --name MyProject --package com.example.project
```

---

## 🤝 Credits & Acknowledgements

Kolt builds on outstanding open-source projects. We are grateful to the creators and maintainers of:
* **[Kotlin Multiplatform](https://kotlinlang.org)**: The multiplatform core enabling shared logic.
* **[Jetpack Compose / Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)**: Enforcing elegant declarative UI engineering.
* **[Vanniktech Gradle Maven Publish Plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)**: Isolating publishing workflows.
* **[Dokka](https://github.com/Kotlin/dokka)**: Powering our standard KDoc API documentation engine.

---

## 📄 License

```
Copyright 2026 Appspiriment

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

See the full [LICENSE](LICENSE) file for details.

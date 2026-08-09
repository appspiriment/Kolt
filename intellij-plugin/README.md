# Kolt Project Wizard — IntelliJ Plugin

Adds a **Kolt** entry to the New Project wizard in Android Studio and IntelliJ IDEA.

## What it creates

When you select **File → New Project → Kolt**, fill in three fields, and click Create:

```
<ProjectName>/
  settings.gradle.kts      ← catalog + modules wired
  build.gradle.kts          ← plugins declared apply false
  gradle.properties
  gradlew + gradle/wrapper/ ← immediately buildable
  app/build.gradle.kts      ← convention plugin applied, namespace set
  shared/build.gradle.kts   ← (KMP only)
  CLAUDE.md / AGENTS.md     ← AI-agent steering stubs (fill in project facts)
  docs/                     ← CODING_STANDARDS · ARCHITECTURE · TESTING · APPSPIRIMENT
  app/src/main/kotlin/<pkg>/
  app/src/main/res/          ← (Android only)
  shared/src/commonMain/kotlin/<pkg>/   ← (KMP only)
  ...
```

First `./gradlew :app:assembleDebug` scaffolds the Compose theme XML resources automatically.

## Build

This is a **standalone Gradle build** — not part of the main UtilsLibs composite build.

```bash
cd intellij-plugin

# Run a sandboxed IDE instance with the plugin installed (for manual testing)
./gradlew runIde

# Build the distributable ZIP
./gradlew buildPlugin
# Output: build/distributions/kolt-intellij-plugin-1.0.0.zip
```

## Install locally

1. `./gradlew buildPlugin`
2. In Android Studio / IntelliJ IDEA: **Settings → Plugins → ⚙ → Install Plugin from Disk...**
3. Select `build/distributions/kolt-intellij-plugin-1.0.0.zip`
4. Restart the IDE
5. **File → New Project** → look for **Kolt** in the left panel

## Updating templates

Templates are bundled from `project-templates/` at build time.
Edit the templates there (they're the single source of truth for both the shell script and this plugin),
then rebuild the plugin.

## Publishing to JetBrains Marketplace

1. Get a [JetBrains Marketplace token](https://plugins.jetbrains.com/author/me/tokens)
2. Set env var: `export PUBLISH_TOKEN=<your-token>`
3. Sign the plugin (requires a certificate — see JetBrains docs)
4. `./gradlew publishPlugin`

## Target IDE versions

Configured to target Android Studio Meerkat (2024.3.1) and later.
To target IntelliJ IDEA instead, change in `build.gradle.kts`:
```kotlin
androidStudio("2024.3.1.14")  →  ideaCommunity("2024.3.1")
```

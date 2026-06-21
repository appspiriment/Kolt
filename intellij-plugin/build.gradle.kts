import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    kotlin("jvm") version "2.1.21"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "io.github.appspiriment.kolt"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// ── Bundle steering-templates into the plugin JAR ──────────────────────────
// Reads from the sibling steering-templates directory so the plugin always
// ships the latest templates without manual copying.
val steeringTemplatesDir = rootDir.parentFile
    .resolve("build-logic/conventions/src/steering-templates")

val syncTemplates = tasks.register<Sync>("syncTemplates") {
    group = "kolt"
    description = "Copies project templates and docs into plugin resources."

    // Project scaffold templates
    from(steeringTemplatesDir.resolve("templates/android-project")) {
        into("templates/android-project")
    }
    from(steeringTemplatesDir.resolve("templates/kmp-project")) {
        into("templates/kmp-project")
    }
    // Steering docs (scaffolded into consumer project on creation)
    from(steeringTemplatesDir.resolve("docs")) {
        into("templates/docs")
        include("CODING_STANDARDS.md", "ARCHITECTURE.md", "TESTING.md", "KOLT.md")
    }
    // Root entry stubs
    from(steeringTemplatesDir) {
        include("CLAUDE.md", "AGENTS.md")
        into("templates")
    }
    into(layout.buildDirectory.dir("generated-resources"))
}

// After syncing, generate a .index file for each template dir so the plugin
// can enumerate files without classpath scanning at runtime.
val generateTemplateIndexes = tasks.register("generateTemplateIndexes") {
    dependsOn(syncTemplates)
    group = "kolt"
    description = "Generates .index files listing template contents."
    doLast {
        val genDir = syncTemplates.get().destinationDir
        listOf("android-project", "kmp-project").forEach { templateName ->
            val templateDir = File(genDir, "templates/$templateName")
            if (templateDir.isDirectory) {
                val index = templateDir.walkTopDown()
                    .filter { it.isFile && !it.name.startsWith(".") }
                    .map { it.relativeTo(templateDir).path }
                    .sorted()
                    .joinToString("\n")
                File(templateDir, ".index").writeText(index)
            }
        }
    }
}

sourceSets.main {
    resources.srcDir(generateTemplateIndexes.map { syncTemplates.get().destinationDir })
}

dependencies {
    intellijPlatform {
        // Target Android Studio Meerkat (2024.3.1) — change to ideaCommunity(...) for IntelliJ IDEA
        androidStudio("2024.3.1.14")
        // bundledPlugin("org.jetbrains.android")   // uncomment if you need Android-specific APIs
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "Kolt Project Wizard"
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "243"   // Android Studio Meerkat / IntelliJ 2024.3
            untilBuild = provider { null }   // no upper bound — stay compatible
        }
        description = """
            Adds a <b>Kolt</b> entry to the New Project wizard in Android Studio and
            IntelliJ IDEA. Creates a fully-configured Android or Kotlin Multiplatform project
            with convention plugins, version catalogs, theme scaffolding, and AI-agent steering
            docs (CLAUDE.md / AGENTS.md) pre-wired.
        """.trimIndent()
        changeNotes = """
            <b>1.0.0</b><br/>
            Initial release — Android-only and KMP project templates.
        """.trimIndent()
    }
    signing {
        // Configure with your JetBrains Marketplace signing certificate for publishing.
        // certificateChainFile.set(file("chain.crt"))
        // privateKeyFile.set(file("private.pem"))
        // password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
    }
    publishing {
        // token.set(providers.environmentVariable("PUBLISH_TOKEN"))
    }
}

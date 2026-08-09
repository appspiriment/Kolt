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

val syncTemplates = tasks.register<Sync>("syncTemplates") {
    group = "kolt"
    description = "Copies project templates and docs into plugin resources."

    // Project scaffold templates
    from(rootDir.parentFile.resolve("project-templates/templates/android-project")) {
        into("templates/android-project")
    }
    from(rootDir.parentFile.resolve("project-templates/templates/kmp-project")) {
        into("templates/kmp-project")
    }
    // Steering standards (single source of truth: Standards/)
    from(rootDir.parentFile.resolve("Standards")) {
        into("templates/docs")
        include("KOLT.md")
    }
    from(rootDir.parentFile.resolve("Standards/steering/kmp")) {
        into("templates/docs")
        include("architecture.md", "presentation-mvi.md", "testing.md")
        rename("architecture.md", "ARCHITECTURE.md")
        rename("presentation-mvi.md", "CODING_STANDARDS.md")
        rename("testing.md", "TESTING.md")
    }
    // Root entry stubs
    from(rootDir.parentFile.resolve("Standards")) {
        include("CLAUDE.md", "AGENTS.md", "GEMINI.md")
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

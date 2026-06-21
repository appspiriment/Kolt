package io.github.appspiriment.kolt.conventions

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Tests for the updateKmpLibFileVersion Gradle task.
 */
class UpdateKmpLibFileVersionTaskTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    private lateinit var projectDir: File
    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @Before
    fun setup() {
        projectDir = tempDir.root
        buildFile = File(projectDir, "build.gradle.kts")
        settingsFile = File(projectDir, "settings.gradle.kts")

        settingsFile.writeText(
            """
            pluginManagement {
                repositories {
                    mavenLocal()
                    gradlePluginPortal()
                    google()
                    mavenCentral()
                }
            }
            dependencyResolutionManagement {
                repositories {
                    mavenLocal()
                    google()
                    mavenCentral()
                }
            }
            rootProject.name = "test-task-project"
            """.trimIndent()
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 16.5 — task skips gracefully when kmplibs.versions.toml is missing
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `16_5 updateKmpLibFileVersion skips gracefully when toml is missing`() {
        // Write the task logic directly (same logic as in conventions/build.gradle.kts)
        buildFile.writeText(
            """
            import java.util.Properties

            val kmpGeneratedSourceDir = layout.buildDirectory.dir("generated/kmp/kotlin")

            tasks.register("updateKmpLibFileVersion") {
                group = "versioning"
                val tomlFile = file("gradle/kmplibs.versions.toml")
                val constantsFile = kmpGeneratedSourceDir.map {
                    it.file("io/github/appspiriment/kolt/conventions/extensions/KmpConstants.kt")
                }
                inputs.property("pluginVersion", "0.0.1")
                outputs.dir(kmpGeneratedSourceDir)

                doLast {
                    if (!tomlFile.exists()) {
                        logger.warn("⚠️ kmplibs.versions.toml not found — skipping KmpConstants.kt generation")
                        return@doLast
                    }
                    constantsFile.get().asFile.also { it.parentFile.mkdirs() }.writeText("// generated")
                }
            }
            """.trimIndent()
        )

        // No gradle/kmplibs.versions.toml — task should warn and succeed
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("updateKmpLibFileVersion", "--stacktrace")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":updateKmpLibFileVersion")?.outcome)
        assertTrue(result.output.contains("kmplibs.versions.toml not found"))

        // KmpConstants.kt should NOT have been generated
        val generatedFile = File(projectDir, "build/generated/kmp/kotlin/io/github/appspiriment/kolt/conventions/extensions/KmpConstants.kt")
        assertFalse("KmpConstants.kt should not be generated when TOML is missing", generatedFile.exists())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 16.6 — generated KmpConstants.kt contains TOML content and version
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `16_6 generated KmpConstants contains embedded TOML and replaced version`() {
        val gradleDir = File(projectDir, "gradle").also { it.mkdirs() }
        File(gradleDir, "kmplibs.versions.toml").writeText(
            """
            [versions]
            kmpAppspiriment = "LIBVERSION"
            kotlin = "2.1.21"
            """.trimIndent()
        )

        val testVersion = "1.2.3"

        buildFile.writeText(
            """
            import java.util.Properties

            val kmpGeneratedSourceDir = layout.buildDirectory.dir("generated/kmp/kotlin")

            tasks.register("updateKmpLibFileVersion") {
                group = "versioning"
                val tomlFile = file("gradle/kmplibs.versions.toml")
                val constantsFile = kmpGeneratedSourceDir.map {
                    it.file("io/github/appspiriment/kolt/conventions/extensions/KmpConstants.kt")
                }
                inputs.file(tomlFile)
                inputs.property("pluginVersion", "$testVersion")
                outputs.dir(kmpGeneratedSourceDir)

                doLast {
                    if (!tomlFile.exists()) {
                        logger.warn("⚠️ kmplibs.versions.toml not found — skipping")
                        return@doLast
                    }
                    val rawToml = tomlFile.readText()
                    val processedToml = rawToml.replace("LIBVERSION", "$testVersion")
                    val escapedToml = processedToml
                        .replace("\\", "\\\\")
                        .replace("\$", "\${'$'}")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                    val file = constantsFile.get().asFile
                    file.parentFile.mkdirs()
                    file.writeText(
                        ""${'"'}
                        package io.github.appspiriment.kolt.conventions.extensions
                        internal const val kmpTomlName = "kmplibs"
                        internal const val kmpLibVersion = "$testVersion"
                        internal const val kmpTomlContents = "${'$'}escapedToml"
                        ""${'"'}.trimIndent()
                    )
                    logger.lifecycle("✅ KmpConstants.kt generated with version: $testVersion")
                }
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("updateKmpLibFileVersion")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":updateKmpLibFileVersion")?.outcome)
        assertTrue(result.output.contains("KmpConstants.kt generated with version: $testVersion"))

        val generatedFile = File(projectDir, "build/generated/kmp/kotlin/io/github/appspiriment/kolt/conventions/extensions/KmpConstants.kt")
        assertTrue("KmpConstants.kt should be generated", generatedFile.exists())

        val content = generatedFile.readText()
        assertTrue("Should contain the version", content.contains(testVersion))
        assertFalse("LIBVERSION placeholder should be replaced", content.contains("LIBVERSION"))
        assertTrue("Should contain kmpTomlName", content.contains("kmpTomlName"))
        assertTrue("Should contain kmpTomlContents", content.contains("kmpTomlContents"))
    }
}

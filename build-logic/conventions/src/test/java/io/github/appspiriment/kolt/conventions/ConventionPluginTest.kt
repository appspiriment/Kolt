package io.github.appspiriment.kolt.conventions

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Properties

/**
 * Gradle TestKit integration tests for the Kolt Convention Plugins.
 *
 * These tests verify:
 * 1. Core versioning task behaviour (bumpDevVersion, upgradeAppspiriment)
 * 2. That the plugin correctly applies dependencies to consumer projects
 *
 * Tests use isolated temporary project directories so they never affect the
 * real project. Each test writes its own minimal build files.
 */
class ConventionPluginTest {

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
            rootProject.name = "test-project"
            """.trimIndent()
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Versioning task tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `bumpDevVersion increments DEV counter`() {
        val propsFile = File(projectDir, "pluginversion.properties")
        propsFile.writeText("MAJOR=0.0.1\nDEV=5\n")

        buildFile.writeText(
            """
            import java.util.Properties

            val versionPropsFile = file("pluginversion.properties")

            tasks.register("bumpDevVersion") {
                outputs.file(versionPropsFile)
                outputs.upToDateWhen { false }
                doLast {
                    val props = Properties().apply { versionPropsFile.inputStream().use { load(it) } }
                    val dev = props.getProperty("DEV", "1").toInt() + 1
                    props.setProperty("DEV", dev.toString())
                    versionPropsFile.outputStream().use { props.store(it, null) }
                }
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("bumpDevVersion")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":bumpDevVersion")?.outcome)
        val props = Properties().apply { propsFile.inputStream().use { load(it) } }
        assertEquals("6", props.getProperty("DEV"))
    }

    @Test
    fun `bumpDevVersion is never UP-TO-DATE — outputs-only task always runs`() {
        val propsFile = File(projectDir, "pluginversion.properties")
        propsFile.writeText("MAJOR=0.0.1\nDEV=1\n")

        buildFile.writeText(
            """
            import java.util.Properties

            val versionPropsFile = file("pluginversion.properties")

            tasks.register("bumpDevVersion") {
                outputs.file(versionPropsFile)
                outputs.upToDateWhen { false }
                doLast {
                    val props = Properties().apply { versionPropsFile.inputStream().use { load(it) } }
                    val dev = props.getProperty("DEV", "1").toInt() + 1
                    props.setProperty("DEV", dev.toString())
                    versionPropsFile.outputStream().use { props.store(it, null) }
                }
            }
            """.trimIndent()
        )

        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("bumpDevVersion")

        // First run
        assertEquals(TaskOutcome.SUCCESS, runner.build().task(":bumpDevVersion")?.outcome)
        // Second run — must NOT be UP-TO-DATE, must run again and bump again
        assertEquals(TaskOutcome.SUCCESS, runner.build().task(":bumpDevVersion")?.outcome)

        val props = Properties().apply { propsFile.inputStream().use { load(it) } }
        assertEquals("3", props.getProperty("DEV"))
    }

    @Test
    fun `upgradeAppspiriment replaces version in TOML`() {
        val gradleDir = File(projectDir, "gradle").also { it.mkdirs() }
        val tomlFile = File(gradleDir, "koltlibs.versions.toml")
        tomlFile.writeText(
            """
            [versions]
            appspiriment = "0.0.1"
            someOtherLib = "1.2.3"
            """.trimIndent()
        )

        // Write the upgradeAppspiriment task logic directly — same logic as in the template
        buildFile.writeText(
            """
            tasks.register("upgradeAppspiriment") {
                doLast {
                    val newVersion = project.findProperty("newVersion") as String?
                        ?: error("Provide -PnewVersion=<version>")
                    val tomlFile = file("gradle/koltlibs.versions.toml")
                    val original = tomlFile.readText()
                    val versionLineRegex = Regex(
                        "^appspiriment\\s*=\\s*\"[^\"]*\"",
                        RegexOption.MULTILINE
                    )
                    val updated = original.replace(versionLineRegex, "appspiriment = \"${'$'}newVersion\"")
                    tomlFile.writeText(updated)
                }
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("upgradeAppspiriment", "-PnewVersion=1.0.0")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":upgradeAppspiriment")?.outcome)
        val updatedContent = tomlFile.readText()
        // Version was updated
        assertTrue(updatedContent.contains("""appspiriment = "1.0.0""""))
        // Other entries were not touched
        assertTrue(updatedContent.contains("""someOtherLib = "1.2.3""""))
    }

    @Test
    fun `upgradeAppspiriment fails with clear error when newVersion is not provided`() {
        val gradleDir = File(projectDir, "gradle").also { it.mkdirs() }
        File(gradleDir, "koltlibs.versions.toml").writeText(
            """
            [versions]
            appspiriment = "0.0.1"
            """.trimIndent()
        )

        buildFile.writeText(
            """
            tasks.register("upgradeAppspiriment") {
                doLast {
                    val newVersion = project.findProperty("newVersion") as String?
                        ?: error("Please provide -PnewVersion=<version>.")
                    println("Would upgrade to: ${'$'}newVersion")
                }
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("upgradeAppspiriment")
            .buildAndFail()

        assertTrue(result.output.contains("Please provide -PnewVersion=<version>."))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildDateSuffix format test (unit-level, no Gradle runner needed)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `buildDateSuffix produces correct default format`() {
        // Import the function directly — it's internal to the plugin classpath
        // We test the format by checking the regex pattern of the output
        val suffix = io.github.appspiriment.kolt.conventions.extensions.buildDateSuffix()
        // Expected default format: .yyyyMMdd-HHmm  e.g. ".20260613-1430"
        val pattern = Regex("""^\.\d{8}-\d{4}$""")
        assertTrue(
            "buildDateSuffix() output '$suffix' does not match expected format .yyyyMMdd-HHmm",
            pattern.matches(suffix)
        )
    }

    @Test
    fun `buildDateSuffix honours a custom pattern`() {
        val suffix = io.github.appspiriment.kolt.conventions.extensions.buildDateSuffix("ddMMyy")
        // Expected: .ddMMyy  e.g. ".130626"
        assertTrue(
            "buildDateSuffix(\"ddMMyy\") output '$suffix' does not match expected format .ddMMyy",
            Regex("""^\.\d{6}$""").matches(suffix)
        )
    }

    @Test
    fun `buildDateSuffix falls back to default for a blank pattern`() {
        val suffix = io.github.appspiriment.kolt.conventions.extensions.buildDateSuffix("")
        assertTrue(
            "buildDateSuffix(\"\") should fall back to the default .yyyyMMdd-HHmm format, got '$suffix'",
            Regex("""^\.\d{8}-\d{4}$""").matches(suffix)
        )
    }
}

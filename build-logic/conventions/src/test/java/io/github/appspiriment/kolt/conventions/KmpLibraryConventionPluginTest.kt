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

/**
 * Gradle TestKit integration tests for KmpLibraryConventionPlugin and
 * KmpLibraryComposeConventionPlugin.
 *
 * Tests apply the plugin to a minimal project via TestKit and assert the
 * resulting configuration. The plugin JAR is loaded from the local build
 * via the TestKit classpath.
 */
class KmpLibraryConventionPluginTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    private lateinit var projectDir: File
    private lateinit var buildFile: File
    private lateinit var settingsFile: File
    private lateinit var gradleDir: File

    @Before
    fun setup() {
        projectDir = tempDir.root
        buildFile = File(projectDir, "build.gradle.kts")
        settingsFile = File(projectDir, "settings.gradle.kts")
        gradleDir = File(projectDir, "gradle").also { it.mkdirs() }

        // Write a minimal kmplibs.versions.toml for the test project
        File(gradleDir, "kmplibs.versions.toml").writeText(
            """
            [versions]
            compileSdk = "34"
            minSdk = "26"
            targetSdk = "34"
            javaVersion = "17"
            agp = "8.13.1"
            kmpAppspiriment = "0.0.1"
            kotlin = "2.3.10"
            coroutines = "1.10.1"
            composeMultiplatform = "1.8.0"
            koin = "4.0.4"
            koinAnnotations = "2.0.0"
            ksp = "2.3.6"
            ktor = "3.1.3"
            sqlDelight = "2.0.2"
            datastore = "1.1.4"
            kotlinxSerialization = "1.8.1"
            turbine = "1.2.0"
            appspirimentLogUtils = "0.0.1"
            appspirimentUtils = "0.0.5"

            [libraries]
            kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
            kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
            kotlin-test = { group = "org.jetbrains.kotlin", name = "kotlin-test", version.ref = "kotlin" }
            turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
            koin-core = { group = "io.insert-koin", name = "koin-core", version.ref = "koin" }
            koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
            koin-test = { group = "io.insert-koin", name = "koin-test", version.ref = "koin" }
            koin-annotations = { group = "io.insert-koin", name = "koin-annotations", version.ref = "koinAnnotations" }
            koin-ksp-compiler = { group = "io.insert-koin", name = "koin-ksp-compiler", version.ref = "koinAnnotations" }
            koin-compose = { group = "io.insert-koin", name = "koin-compose", version.ref = "koin" }
            koin-compose-viewmodel = { group = "io.insert-koin", name = "koin-compose-viewmodel", version.ref = "koin" }
            appspiriment-utils = { group = "io.github.appspiriment.kolt", name = "utils", version.ref = "appspirimentUtils" }
            appspiriment-logutils-dev = { group = "io.github.appspiriment.kolt", name = "logutils-dev", version.ref = "appspirimentLogUtils" }
            appspiriment-logutils-prod = { group = "io.github.appspiriment.kolt", name = "logutils-prod", version.ref = "appspirimentLogUtils" }

            [plugins]
            kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
            kotlin-compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
            compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
            kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
            devtools-ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
            android-library = { id = "com.android.library", version.ref = "agp" }
            android-application = { id = "com.android.application", version.ref = "agp" }
            """.trimIndent()
        )

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
                versionCatalogs {
                    create("kmplibs") {
                        from(files("gradle/kmplibs.versions.toml"))
                    }
                }
            }
            rootProject.name = "test-kmp-project"
            """.trimIndent()
        )
    }

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args, "--stacktrace")

    // ─────────────────────────────────────────────────────────────────────────
    // Task 13.1 — applying the plugin registers the `kmp` extension
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `13_1 applying kmpLibrary plugin registers kmp extension`() {
        buildFile.writeText(
            """
            plugins {
                id("io.github.appspiriment.kolt.kmp.library")
            }
            android { namespace = "com.example.test" }

            tasks.register("checkExtension") {
                doLast {
                    val ext = project.extensions.findByName("kmp")
                    require(ext != null) { "kmp extension not registered" }
                    println("kmp extension found: ${'$'}{ext!!::class.simpleName}")
                }
            }
            """.trimIndent()
        )

        val result = runner("checkExtension").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":checkExtension")?.outcome)
        assertTrue(result.output.contains("kmp extension found"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 13.2 — default config applies only androidTarget
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `13_2 default config applies only androidTarget`() {
        buildFile.writeText(
            """
            plugins {
                id("io.github.appspiriment.kolt.kmp.library")
            }
            android { namespace = "com.example.test" }

            tasks.register("checkTargets") {
                doLast {
                    val kotlin = project.extensions.getByType(
                        org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java
                    )
                    val targetNames = kotlin.targets.map { it.name }
                    println("Targets: ${'$'}targetNames")
                    require("android" in targetNames) { "android target missing" }
                    require("iosArm64" !in targetNames) { "iosArm64 should not be present by default" }
                    require("desktop" !in targetNames) { "desktop should not be present by default" }
                }
            }
            """.trimIndent()
        )

        val result = runner("checkTargets").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":checkTargets")?.outcome)
        assertTrue(result.output.contains("android"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 13.3 — enableIos = true adds iOS targets and source sets
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `13_3 enableIos adds iosArm64 iosSimulatorArm64 iosX64 and iosMain iosTest source sets`() {
        buildFile.writeText(
            """
            plugins {
                id("io.github.appspiriment.kolt.kmp.library")
            }
            android { namespace = "com.example.test" }
            kmp { enableIos.set(true) }

            tasks.register("checkIos") {
                doLast {
                    val kotlin = project.extensions.getByType(
                        org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java
                    )
                    val targetNames = kotlin.targets.map { it.name }
                    val sourceSetNames = kotlin.sourceSets.map { it.name }
                    println("Targets: ${'$'}targetNames")
                    println("SourceSets: ${'$'}sourceSetNames")
                    require("iosArm64" in targetNames) { "iosArm64 missing" }
                    require("iosSimulatorArm64" in targetNames) { "iosSimulatorArm64 missing" }
                    require("iosX64" in targetNames) { "iosX64 missing" }
                    require("iosMain" in sourceSetNames) { "iosMain source set missing" }
                    require("iosTest" in sourceSetNames) { "iosTest source set missing" }
                }
            }
            """.trimIndent()
        )

        val result = runner("checkIos").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":checkIos")?.outcome)
        assertTrue(result.output.contains("iosArm64"))
        assertTrue(result.output.contains("iosMain"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 13.4 — enableDesktop = true adds jvm("desktop") and source sets
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `13_4 enableDesktop adds desktop jvm target and desktopMain desktopTest source sets`() {
        buildFile.writeText(
            """
            plugins {
                id("io.github.appspiriment.kolt.kmp.library")
            }
            android { namespace = "com.example.test" }
            kmp { enableDesktop.set(true) }

            tasks.register("checkDesktop") {
                doLast {
                    val kotlin = project.extensions.getByType(
                        org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java
                    )
                    val targetNames = kotlin.targets.map { it.name }
                    val sourceSetNames = kotlin.sourceSets.map { it.name }
                    println("Targets: ${'$'}targetNames")
                    println("SourceSets: ${'$'}sourceSetNames")
                    require("desktop" in targetNames) { "desktop target missing" }
                    require("desktopMain" in sourceSetNames) { "desktopMain source set missing" }
                    require("desktopTest" in sourceSetNames) { "desktopTest source set missing" }
                }
            }
            """.trimIndent()
        )

        val result = runner("checkDesktop").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":checkDesktop")?.outcome)
        assertTrue(result.output.contains("desktop"))
        assertTrue(result.output.contains("desktopMain"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 13.5 — enableWasm = true adds wasmJs target and source sets
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `13_5 enableWasm adds wasmJs target and wasmJsMain wasmJsTest source sets`() {
        buildFile.writeText(
            """
            plugins {
                id("io.github.appspiriment.kolt.kmp.library")
            }
            android { namespace = "com.example.test" }
            kmp { enableWasm.set(true) }

            tasks.register("checkWasm") {
                doLast {
                    val kotlin = project.extensions.getByType(
                        org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java
                    )
                    val targetNames = kotlin.targets.map { it.name }
                    val sourceSetNames = kotlin.sourceSets.map { it.name }
                    println("Targets: ${'$'}targetNames")
                    println("SourceSets: ${'$'}sourceSetNames")
                    require("wasmJs" in targetNames) { "wasmJs target missing" }
                    require("wasmJsMain" in sourceSetNames) { "wasmJsMain source set missing" }
                    require("wasmJsTest" in sourceSetNames) { "wasmJsTest source set missing" }
                }
            }
            """.trimIndent()
        )

        val result = runner("checkWasm").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":checkWasm")?.outcome)
        assertTrue(result.output.contains("wasmJs"))
        assertTrue(result.output.contains("wasmJsMain"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 13.6 — commonMain always contains kotlinx-coroutines-core
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `13_6 commonMain always contains kotlinx-coroutines-core`() {
        buildFile.writeText(
            """
            plugins {
                id("io.github.appspiriment.kolt.kmp.library")
            }
            android { namespace = "com.example.test" }

            tasks.register("checkCoroutines") {
                doLast {
                    val deps = configurations
                        .filter { it.name == "commonMainImplementation" }
                        .flatMap { it.dependencies }
                        .map { "${'$'}{it.group}:${'$'}{it.name}" }
                    println("commonMainImplementation deps: ${'$'}deps")
                    require(deps.any { it.contains("kotlinx-coroutines-core") }) {
                        "kotlinx-coroutines-core not found in commonMainImplementation"
                    }
                }
            }
            """.trimIndent()
        )

        val result = runner("checkCoroutines").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":checkCoroutines")?.outcome)
        assertTrue(result.output.contains("kotlinx-coroutines-core"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 13.7 — commonTest always contains kotlin-test, coroutines-test, turbine
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `13_7 commonTest always contains kotlin-test kotlinx-coroutines-test turbine`() {
        buildFile.writeText(
            """
            plugins {
                id("io.github.appspiriment.kolt.kmp.library")
            }
            android { namespace = "com.example.test" }

            tasks.register("checkTestDeps") {
                doLast {
                    val deps = configurations
                        .filter { it.name == "commonTestImplementation" }
                        .flatMap { it.dependencies }
                        .map { "${'$'}{it.group}:${'$'}{it.name}" }
                    println("commonTestImplementation deps: ${'$'}deps")
                    require(deps.any { it.contains("kotlin-test") }) { "kotlin-test missing" }
                    require(deps.any { it.contains("kotlinx-coroutines-test") }) { "kotlinx-coroutines-test missing" }
                    require(deps.any { it.contains("turbine") }) { "turbine missing" }
                }
            }
            """.trimIndent()
        )

        val result = runner("checkTestDeps").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":checkTestDeps")?.outcome)
        assertTrue(result.output.contains("kotlin-test"))
        assertTrue(result.output.contains("turbine"))
    }
}

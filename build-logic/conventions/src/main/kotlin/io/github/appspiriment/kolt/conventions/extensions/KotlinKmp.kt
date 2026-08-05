package io.github.appspiriment.kolt.conventions.extensions

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configures the KMP Android target early (before afterEvaluate).
 * Sets compileSdk, minSdk, jvmTarget, the published Android variant, and Kotlin
 * compiler options. Called immediately in apply() so Android configuration is
 * available before the consuming build script runs (the consumer only needs to set
 * `android { namespace = ... }`).
 */
internal fun Project.configureKmpEarly(kotlinExtension: KotlinMultiplatformExtension) {
    // Idempotent: KmpLibraryKoinComposeConventionPlugin pre-calls this before super.apply()
    if (kotlinExtension.targets.findByName("android") != null) return
    val configs = kmpProjectConfigs
    kotlinExtension.apply {
        androidTarget {
            // Publish the release library variant (required by Maven publishing / vanniktech).
            publishLibraryVariants("release")
            compilations.all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.fromTarget(configs.javaVersion.majorVersion))
                    }
                }
            }
        }

        // Global Kotlin compiler options for all targets
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            )
        }
    }

    // Configure the AGP android library extension (compileSdk/minSdk) so consumer
    // modules don't need to repeat it. Namespace is still module-specific.
    extensions.configure<LibraryExtension> {
        compileSdk = configs.compileSdk
        defaultConfig { minSdk = configs.minSdk }
    }
}

/**
 * Configures optional KMP targets based on the [KmpExtension] values.
 * Called inside afterEvaluate so user-configured extension values are available.
 */
internal fun Project.configureKmpLate(
    kotlinExtension: KotlinMultiplatformExtension,
    kmpExt: KmpExtension
) {
    if (kmpExt.enableIos.getOrElse(false)) {
        configureIosTargets(kotlinExtension)
    }
    if (kmpExt.enableDesktop.getOrElse(false)) {
        configureDesktopTarget(kotlinExtension)
    }
    if (kmpExt.enableWasm.getOrElse(false)) {
        configureWasmTarget(kotlinExtension)
    }
}

/**
 * Adds iosArm64, iosSimulatorArm64, iosX64 targets.
 *
 * KGP 2.x's default target hierarchy template automatically creates the `iosMain`/`iosTest`
 * intermediate source sets and wires the three iOS targets to them. We deliberately do NOT
 * add `dependsOn` edges by hand — doing so disables the default template (KGP emits a warning
 * and skips it). Consumers/other plugins reference `sourceSets.iosMain` as usual.
 */
internal fun Project.configureIosTargets(kotlinExtension: KotlinMultiplatformExtension) {
    kotlinExtension.apply {
        iosArm64()
        iosSimulatorArm64()
        iosX64()
    }
}

/**
 * Adds the jvm("desktop") target. `desktopMain`/`desktopTest` are wired to commonMain/commonTest
 * automatically (every target's main/test depends on common by default).
 * Idempotent: KmpLibraryKoinComposeConventionPlugin pre-calls this before Compose is applied
 * to ensure desktopJar is registered before Compose's withType(KotlinJvmTarget) callback fires.
 */
internal fun Project.configureDesktopTarget(kotlinExtension: KotlinMultiplatformExtension) {
    // Idempotent: skip if KmpLibraryKoinComposeConventionPlugin already added the target early
    if (kotlinExtension.targets.findByName("desktop") != null) return

    kotlinExtension.apply {
        jvm("desktop")
    }
}

/**
 * Adds the wasmJs target with browser execution. `wasmJsMain`/`wasmJsTest` are wired to
 * commonMain/commonTest automatically by KGP.
 */
internal fun Project.configureWasmTarget(kotlinExtension: KotlinMultiplatformExtension) {
    kotlinExtension.apply {
        @Suppress("OPT_IN_USAGE")
        wasmJs {
            browser()
        }
    }
}

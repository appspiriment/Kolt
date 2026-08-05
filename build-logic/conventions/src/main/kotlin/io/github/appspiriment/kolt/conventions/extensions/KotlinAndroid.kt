package io.github.appspiriment.kolt.conventions.extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ────────────────────────────────────────────────────────────────────────────────
// Extension Functions
// ────────────────────────────────────────────────────────────────────────────────

internal fun Project.configureAndroidEarly(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        compileSdk = projectConfigs.compileSdk

        defaultConfig {
            minSdk = projectConfigs.minSdk
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            vectorDrawables.useSupportLibrary = true
        }

        if (this is ApplicationExtension) {
            defaultConfig.targetSdk = projectConfigs.targetSdk
        }

        compileOptions {
            sourceCompatibility = projectConfigs.javaVersion
            targetCompatibility = projectConfigs.javaVersion
        }
    }
}


internal fun Project.configureAndroidLate(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    addDevSuffixToDebug: Boolean
) {
    // Kotlin compiler options — must match compileOptions.sourceCompatibility (Java 17)
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(projectConfigs.javaVersion.toString()))
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-Xannotation-default-target=param-property",
                "-Xcontext-parameters"
            )
        }
    }
}


/** Default timestamp pattern appended to debug versionName. */
internal const val DEFAULT_DEBUG_VERSION_TIMESTAMP_PATTERN = "yyyyMMdd-HHmm"

/**
 * Generates a version name suffix from the current time, e.g. `.20260613-1430`.
 *
 * @param pattern a [DateTimeFormatter] pattern; defaults to [DEFAULT_DEBUG_VERSION_TIMESTAMP_PATTERN].
 *   Falls back to the default if the supplied pattern is blank or invalid.
 */
internal fun buildDateSuffix(pattern: String = DEFAULT_DEBUG_VERSION_TIMESTAMP_PATTERN): String {
    val safePattern = pattern.ifBlank { DEFAULT_DEBUG_VERSION_TIMESTAMP_PATTERN }
    val formatter = try {
        DateTimeFormatter.ofPattern(safePattern, Locale.ENGLISH)
    } catch (_: IllegalArgumentException) {
        DateTimeFormatter.ofPattern(DEFAULT_DEBUG_VERSION_TIMESTAMP_PATTERN, Locale.ENGLISH)
    }
    return ".${formatter.format(LocalDateTime.now())}"
}

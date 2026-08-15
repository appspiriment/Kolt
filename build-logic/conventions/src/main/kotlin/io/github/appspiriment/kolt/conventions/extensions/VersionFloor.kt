package io.github.appspiriment.kolt.conventions.extensions

import org.gradle.api.GradleException
import org.gradle.api.artifacts.VersionCatalog

/**
 * Compares two dot-separated, numeric-leading version strings (e.g. "8.13.1", "2.3.10",
 * "2026.02.01"). Any non-numeric suffix (e.g. "-alpha01", ".dev-00") is stripped before
 * comparing — only the leading numeric segments participate.
 *
 * Returns a negative number if [a] < [b], zero if equal, positive if [a] > [b].
 */
internal fun compareVersions(a: String, b: String): Int {
    fun segments(v: String) = v.substringBefore("-").split(".").map { it.toIntOrNull() ?: 0 }
    val segA = segments(a)
    val segB = segments(b)
    for (i in 0 until maxOf(segA.size, segB.size)) {
        val diff = segA.getOrElse(i) { 0 } - segB.getOrElse(i) { 0 }
        if (diff != 0) return diff
    }
    return 0
}

/**
 * Hard-fails the build if the consumer's `[alias]` version in [catalog] is below
 * [minVersion] — the version Kolt's convention plugins are actually compiled and tested
 * against. Consumers remain free to raise this value in their own version catalog TOML
 * at any time; only downgrading below Kolt's floor is rejected.
 */
internal fun requireAtLeast(
    catalog: VersionCatalog,
    alias: String,
    minVersion: String,
    projectName: String,
) {
    val actual = catalog.getVersion(alias)
    if (compareVersions(actual, minVersion) < 0) {
        throw GradleException(
            "Kolt: project '$projectName' declares $alias=$actual in its version catalog, " +
                "but this version of Kolt requires $alias >= $minVersion (the version its " +
                "convention plugins are built and tested against). Raise `$alias` in " +
                "gradle/koltlibs.versions.toml (or kmplibs.versions.toml) — Kolt only " +
                "enforces a floor, you are always free to go higher."
        )
    }
}

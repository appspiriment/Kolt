package io.github.appspiriment.kolt.conventions.plugins

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import io.github.appspiriment.kolt.conventions.extensions.koltLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

/**
 * Base convention plugin for Android library modules.
 *
 * Subclasses opt in to Hilt and/or Compose capabilities by overriding [setupHilt]
 * and [setupCompose] flags. This inheritance-based capability pattern keeps each
 * concrete plugin class minimal — a single boolean override is all that's needed.
 *
 * **Known limitation**: adding a new capability (e.g. Firebase, KMP) requires
 * modifying this base class and all subclasses that should include it. If the
 * number of capability combinations grows large, consider switching to a
 * composition-based approach using a single plugin with a configuration extension.
 */
open class AndroidLibraryConventionPlugin : AndroidBaseConventionPlugin() {
    override val Project.commonExtension get() = extensions.getByType<LibraryExtension>()
    open val setupHilt: Boolean = false
    open val setupCompose: Boolean = false

    override fun apply(target: Project) {
        target.run {
            // Apply the Android Library plugin first — required before any android {} block
            pluginManager.apply("com.android.library")
            if (setupHilt) setupHilt()
            if (setupCompose) setupCompose()
            super.apply(this)
        }
    }
}

/** Android library module with Hilt dependency injection. */
open class AndroidLibraryHiltConventionPlugin : AndroidLibraryConventionPlugin() {
    override val setupHilt: Boolean = true
}

/** Android library module with Jetpack Compose UI. */
class AndroidLibraryComposeConventionPlugin : AndroidLibraryConventionPlugin() {
    override val setupCompose: Boolean = true
}

/** Android library module with both Hilt and Jetpack Compose. */
class AndroidLibraryHiltComposeConventionPlugin : AndroidLibraryConventionPlugin() {
    override val setupHilt: Boolean = true
    override val setupCompose: Boolean = true
}

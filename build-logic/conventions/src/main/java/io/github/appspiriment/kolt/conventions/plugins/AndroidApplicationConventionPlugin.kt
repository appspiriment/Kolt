package io.github.appspiriment.kolt.conventions.plugins

import com.android.build.api.dsl.ApplicationExtension
import io.github.appspiriment.kolt.conventions.extensions.koltLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationConventionPlugin : AndroidBaseConventionPlugin() {

    override val Project.commonExtension get() = extensions.getByType<ApplicationExtension>()

    override fun apply(target: Project) {
        target.run {
            // Apply the Android Application plugin first — required before any android {} block
            pluginManager.apply("com.android.application")

            setupHilt()
            setupCompose()
            super.apply(this)
        }
    }
}

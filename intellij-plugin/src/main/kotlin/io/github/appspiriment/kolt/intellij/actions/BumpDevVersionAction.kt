package io.github.appspiriment.kolt.intellij.actions

import io.github.appspiriment.kolt.intellij.publish.GradleTaskRunner
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Tools > Kolt > Bump Dev Version
 *
 * Increments the `DEV` counter in `version.properties` (e.g. `1.0.0.dev-03 → 1.0.0.dev-04`).
 * No credentials needed — this is a local file operation done by Gradle.
 */
class BumpDevVersionAction : AnAction(
    "Bump Dev Version",
    "Increment DEV counter in version.properties",
    null,
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        GradleTaskRunner.run(
            project  = project,
            args     = listOf("bumpDevVersion"),
            tabTitle = "Bump Dev Version",
        )
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

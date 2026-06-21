package io.github.appspiriment.kolt.intellij.actions

import io.github.appspiriment.kolt.intellij.publish.GradleTaskRunner
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Tools > Kolt > Publish All → Maven Local
 *
 * Runs `./gradlew publishAllToMavenLocal` in the current project root.
 * Useful during development to make changes available to other local projects
 * without needing to go through Maven Central.
 */
class PublishToMavenLocalAction : AnAction(
    "Publish All → Maven Local",
    "Run publishAllToMavenLocal for all Kolt modules",
    null,
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        GradleTaskRunner.run(
            project  = project,
            args     = listOf("publishAllToMavenLocal"),
            tabTitle = "Publish → Maven Local",
        )
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

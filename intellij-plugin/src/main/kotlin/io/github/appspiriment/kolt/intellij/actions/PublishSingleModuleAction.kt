package io.github.appspiriment.kolt.intellij.actions

import io.github.appspiriment.kolt.intellij.publish.GradleTaskRunner
import io.github.appspiriment.kolt.intellij.settings.KoltSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBList

/**
 * Tools > Kolt > Publish Single Module…
 *
 * Shows a popup letting the user pick one module to publish to Maven Local or Maven Central.
 */
class PublishSingleModuleAction : AnAction(
    "Publish Single Module…",
    "Choose a single Kolt module to publish",
    null,
) {

    data class Module(
        val label: String,
        val localArgs: List<String>,
        val releaseArgs: List<String>,
    )

    private val modules = listOf(
        Module(
            label       = "convention-plugins (build-logic)",
            localArgs   = listOf("--project-dir", "build-logic", ":conventions:publishToMavenLocal"),
            releaseArgs = listOf("--project-dir", "build-logic", ":conventions:publishToSonatype", "-PisRelease"),
        ),
        Module(
            label       = "logutils",
            localArgs   = listOf(":libs:logutils:publishToMavenLocal"),
            releaseArgs = listOf(":libs:logutils:publishToSonatype", "-PisRelease"),
        ),
        Module(
            label       = "utils",
            localArgs   = listOf(":libs:utils:publishToMavenLocal"),
            releaseArgs = listOf(":libs:utils:publishToSonatype", "-PisRelease"),
        ),
        Module(
            label       = "compose-utils",
            localArgs   = listOf(":libs:compose-utils:publishToMavenLocal"),
            releaseArgs = listOf(":libs:compose-utils:publishToSonatype", "-PisRelease"),
        ),
        Module(
            label       = "update-utils",
            localArgs   = listOf(":libs:update-utils:publishToMavenLocal"),
            releaseArgs = listOf(":libs:update-utils:publishToSonatype", "-PisRelease"),
        ),
    )

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val items = modules.flatMap { m ->
            listOf(
                "${m.label}  →  Maven Local",
                "${m.label}  →  Maven Central (release)",
            )
        }

        val list = JBList(items)
        JBPopupFactory.getInstance()
            .createListPopupBuilder(list)
            .also { /* intentional no-op: builder wires the list */ }

        // Use the simpler createPopupChooserBuilder API
        JBPopupFactory.getInstance()
            .createPopupChooserBuilder(items)
            .setTitle("Publish Module")
            .setItemChosenCallback { chosen ->
                val index = items.indexOf(chosen)
                val module = modules[index / 2]
                val isRelease = index % 2 == 1
                val settings = KoltSettings.instance
                val args = if (isRelease) {
                    module.releaseArgs + if (settings.signOnRelease) emptyList() else listOf("-PnoSign")
                } else {
                    module.localArgs
                }
                val destination = if (isRelease) "Central" else "Local"
                GradleTaskRunner.run(
                    project  = project,
                    args     = args,
                    tabTitle = "Publish ${module.label} → $destination",
                )
            }
            .createPopup()
            .showInBestPositionFor(e.dataContext)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

// ── Convenience sub-actions exposed in the Tools > Kolt > Libraries submenu ──

private fun moduleLocalAction(module: PublishSingleModuleAction.Module) = object : AnAction(
    "${module.label} → Maven Local", null, null,
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        GradleTaskRunner.run(project, module.localArgs, "Publish ${module.label} → Local")
    }
    override fun update(e: AnActionEvent) { e.presentation.isEnabledAndVisible = e.project != null }
}

private fun moduleReleaseAction(module: PublishSingleModuleAction.Module) = object : AnAction(
    "${module.label} → Maven Central", null, null,
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val settings = KoltSettings.instance
        val args = module.releaseArgs + if (settings.signOnRelease) emptyList() else listOf("-PnoSign")
        GradleTaskRunner.run(project, args, "Publish ${module.label} → Central")
    }
    override fun update(e: AnActionEvent) { e.presentation.isEnabledAndVisible = e.project != null }
}

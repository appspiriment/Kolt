package io.github.appspiriment.kolt.intellij.actions

import io.github.appspiriment.kolt.intellij.publish.GradleTaskRunner
import io.github.appspiriment.kolt.intellij.settings.KoltCredentials
import io.github.appspiriment.kolt.intellij.settings.KoltSettings
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

/**
 * Tools > Kolt > Publish All → Maven Central (Release)
 *
 * Publishes every Kolt module to Sonatype OSSRH / Maven Central.
 *
 * Because of a known vanniktech classloader conflict when multiple modules share one
 * Gradle daemon, this action delegates to the per-module `publishToSonatype` tasks in
 * separate sequential Gradle invocations via [GradleTaskRunner].  Each invocation runs
 * one module so the BuildService registrations never clash.
 *
 * Credentials are injected as environment variables from [KoltCredentials.toEnvMap]
 * so they never appear on the Gradle command line.
 */
class PublishToMavenCentralAction : AnAction(
    "Publish All → Maven Central (Release)",
    "Sign and publish all Kolt modules to Sonatype OSSRH",
    null,
) {
    private val modules = listOf(
        // Convention plugins + version catalogs
        "build-logic" to listOf(
            ":conventions:publishToSonatype",
            "-PisRelease",
            "--project-dir", "build-logic",
        ),
        // Runtime libraries — published from root with per-module task paths
        "logutils"      to listOf(":libs:logutils:publishToSonatype",      "-PisRelease"),
        "utils"         to listOf(":libs:utils:publishToSonatype",          "-PisRelease"),
        "compose-utils" to listOf(":libs:compose-utils:publishToSonatype", "-PisRelease"),
        "update-utils"  to listOf(":libs:update-utils:publishToSonatype",  "-PisRelease"),
    )

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val settings = KoltSettings.instance

        // Guard: warn if credentials look absent
        val creds = KoltCredentials.toEnvMap()
        if (creds["OSSRH_USERNAME"].isNullOrBlank() || creds["OSSRH_PASSWORD"].isNullOrBlank()) {
            val choice = Messages.showYesNoDialog(
                project,
                "OSSRH credentials are not configured.\n\n" +
                "Go to Settings → Tools → Kolt to enter your Sonatype token.\n\n" +
                "Continue anyway? (Gradle will fall back to ~/.gradle/gradle.properties)",
                "Missing Credentials",
                "Continue",
                "Open Settings",
                Messages.getWarningIcon(),
            )
            if (choice != Messages.YES) {
                com.intellij.openapi.options.ShowSettingsUtil.getInstance()
                    .showSettingsDialog(project, "Kolt")
                return
            }
        }

        val signingArgs = if (settings.signOnRelease) listOf("-PisRelease") else emptyList()

        // Run each module in a separate GradleTaskRunner call so each gets its own
        // Gradle daemon — avoids the SonatypeRepositoryBuildService classloader conflict.
        modules.forEachIndexed { index, (name, baseArgs) ->
            val args = (baseArgs + signingArgs).distinct()
            GradleTaskRunner.run(
                project  = project,
                args     = args,
                tabTitle = "Publish $name → Central (${index + 1}/${modules.size})",
            )
        }

        NotificationGroupManager.getInstance()
            .getNotificationGroup("Kolt")
            .createNotification(
                "Publish to Maven Central",
                "Started ${modules.size} Gradle jobs. Check the Run tool window for progress.",
                NotificationType.INFORMATION,
            )
            .notify(project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

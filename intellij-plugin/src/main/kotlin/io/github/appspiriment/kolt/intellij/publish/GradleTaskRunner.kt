package io.github.appspiriment.kolt.intellij.publish

import io.github.appspiriment.kolt.intellij.settings.KoltCredentials
import io.github.appspiriment.kolt.intellij.settings.KoltSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import java.io.File
import java.nio.charset.Charset

/**
 * Runs `./gradlew` tasks for the given project and streams the output into
 * a dedicated console panel in the **Run tool window** — exactly like a normal
 * Gradle run, with ANSI colour and a stop button.
 *
 * Credentials from [KoltCredentials] are injected as environment variables
 * so they never appear on the command line.
 */
object GradleTaskRunner {

    private val log = logger<GradleTaskRunner>()

    /**
     * @param project  The open IntelliJ project (provides the working directory).
     * @param args     Gradle arguments, e.g. `listOf("publishAllToMavenLocal", "--no-parallel")`.
     * @param tabTitle Label shown on the Run tool window tab.
     * @param env      Additional environment variables merged with the credential env.
     */
    fun run(
        project: Project,
        args: List<String>,
        tabTitle: String,
        env: Map<String, String> = emptyMap(),
    ) {
        val projectDir = File(project.basePath ?: run {
            log.warn("Project has no basePath — cannot run Gradle task")
            return
        })

        val gradlew = File(
            projectDir,
            if (SystemInfo.isWindows) "gradlew.bat" else "gradlew",
        )
        if (!gradlew.exists()) {
            notifyError(project, tabTitle, "gradlew not found in ${projectDir.path}.\n" +
                "Run this action from a Gradle project with a Gradle wrapper.")
            return
        }

        val settings = KoltSettings.instance
        val cmdLine = mutableListOf(gradlew.absolutePath).apply {
            addAll(args)
            // Append any extra Gradle flags from settings
            val extraFlags = settings.extraGradleFlags.trim()
            if (extraFlags.isNotBlank()) {
                addAll(extraFlags.split("\\s+".toRegex()))
            }
        }

        log.info("[Kolt] Running: ${cmdLine.joinToString(" ")}")

        // Build merged environment: credential env + caller extras + current process env
        val mergedEnv = System.getenv().toMutableMap().apply {
            putAll(KoltCredentials.toEnvMap())
            putAll(env)
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val processBuilder = ProcessBuilder(cmdLine)
                    .directory(projectDir)
                    .redirectErrorStream(true)
                processBuilder.environment().apply {
                    clear()
                    putAll(mergedEnv)
                }

                val process = processBuilder.start()

                // Wire into IntelliJ's process/console infrastructure
                val handler = OSProcessHandler(process, cmdLine.joinToString(" "), Charset.defaultCharset())
                val console = TextConsoleBuilderFactory.getInstance()
                    .createBuilder(project)
                    .apply { setViewer(false) }
                    .console

                console.attachToProcess(handler)

                handler.addProcessListener(object : ProcessAdapter() {
                    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                        // Output is already displayed in the console
                    }
                    override fun processTerminated(event: ProcessEvent) {
                        val code = event.exitCode
                        if (code == 0) {
                            console.print("\n✅ $tabTitle — SUCCESS\n",
                                com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT)
                        } else {
                            console.print("\n❌ $tabTitle — FAILED (exit $code)\n",
                                com.intellij.execution.ui.ConsoleViewContentType.ERROR_OUTPUT)
                        }
                    }
                })

                // Show in Run tool window
                val descriptor = RunContentDescriptor(
                    console,
                    handler,
                    console.component,
                    tabTitle,
                )
                descriptor.isAutoFocusContent = true

                ApplicationManager.getApplication().invokeLater {
                    RunContentManager.getInstance(project).showRunContent(
                        DefaultRunExecutor.getRunExecutorInstance(),
                        descriptor,
                    )
                    handler.startNotify()
                }
            } catch (e: Exception) {
                log.error("[Kolt] Failed to start Gradle process", e)
                notifyError(project, tabTitle, "Failed to start: ${e.message}")
            }
        }
    }

    private fun notifyError(project: Project, title: String, message: String) {
        ApplicationManager.getApplication().invokeLater {
            com.intellij.notification.NotificationGroupManager.getInstance()
                .getNotificationGroup("Kolt")
                .createNotification(title, message, com.intellij.notification.NotificationType.ERROR)
                .notify(project)
        }
    }
}

package io.github.appspiriment.kolt.intellij

import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent

/**
 * Provides the settings panel shown inside the "New Project" wizard.
 *
 * Renders two controls beneath the standard "Location" field:
 *   - Project type  : Android / KMP  (radio buttons via buttonsGroup)
 *   - Package name  : editable text field
 */
class KoltProjectPeer : ProjectGeneratorPeer<KoltProjectSettings> {

    var selectedType = KoltProjectSettings.ProjectType.ANDROID
    var packageName  = "com.example.myapp"

    private val panel = panel {
        buttonsGroup("Project type:") {
            KoltProjectSettings.ProjectType.entries.forEach { type ->
                row { radioButton(type.label, type) }
            }
        }.bind(::selectedType)

        row("Package name:") {
            textField()
                .columns(COLUMNS_LARGE)
                .bindText(::packageName)
                .validationOnInput {
                    val v = it.text.trim()
                    when {
                        v.isBlank() ->
                            error("Package name is required")
                        !v.matches(Regex("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*){1,}\$")) ->
                            error("Use reverse-domain format: com.example.myapp")
                        else -> null
                    }
                }
                .also { it.component.toolTipText = "e.g. com.example.myapp" }
        }

        row {
            comment(
                "Convention plugins, version catalog, Gradle wrapper, source directories, " +
                "and AI-agent steering docs (<b>CLAUDE.md</b>, <b>docs/</b>) are created automatically. " +
                "First <code>./gradlew :app:assembleDebug</code> scaffolds the Compose theme XML resources."
            )
        }
    }

    // ── ProjectGeneratorPeer contract ─────────────────────────────────────────

    /** Returns the main settings panel. */
    override fun getComponent(): JComponent = panel

    /**
     * Called by the wizard to embed our fields into a SettingsStep (e.g. the
     * "Project SDK" step). We add the package name field here so it appears
     * inline with the standard wizard fields when the step layout is used.
     */
    @Suppress("OVERRIDE_DEPRECATION")
    override fun buildUI(settingsStep: SettingsStep) {
        settingsStep.addSettingsField("Package name:", panel)
    }

    override fun getSettings(): KoltProjectSettings {
        panel.apply()
        return KoltProjectSettings(
            projectType = selectedType,
            packageName = packageName.trim(),
        )
    }

    override fun validate(): ValidationInfo? {
        val pkg = packageName.trim()
        if (!pkg.matches(Regex("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*){1,}\$"))) {
            return ValidationInfo("Invalid package name '$pkg'. Use reverse-domain format: com.example.myapp")
        }
        return null
    }

    /** No async background work in this peer. */
    override fun isBackgroundJobRunning(): Boolean = false
}

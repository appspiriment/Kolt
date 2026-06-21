package io.github.appspiriment.kolt.intellij.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent
import javax.swing.JPasswordField

/**
 * Settings panel shown at **Settings → Tools → Kolt**.
 *
 * Non-secret fields bind directly to [KoltSettings].
 * Secret fields (OSSRH password, GPG passphrase) read/write via [KoltCredentials]
 * so they are stored in the OS keychain, never written to disk in plain text.
 */
class KoltSettingsConfigurable : Configurable {

    private val settings = KoltSettings.instance

    // Password fields — kept as local vars so we can read them on Apply
    private var ossrhPasswordField: JPasswordField? = null
    private var gpgPassphraseField: JPasswordField? = null

    // Backing local state (reset/apply lifecycle)
    private var ossrhUsername  = settings.ossrhUsername
    private var gpgKeyName     = settings.gpgKeyName
    private var signOnRelease  = settings.signOnRelease
    private var extraFlags     = settings.extraGradleFlags

    override fun getDisplayName(): String = "Kolt"

    override fun createComponent(): JComponent = panel {

        // ── Maven Central / Sonatype OSSRH ───────────────────────────────────
        group("Maven Central (Sonatype OSSRH)") {
            row {
                comment(
                    "Credentials for publishing to Maven Central via Sonatype OSSRH.<br/>" +
                    "Generate a <b>user token</b> at " +
                    "<a href='https://central.sonatype.com/account'>central.sonatype.com/account</a> " +
                    "(use the token, not your account password). " +
                    "Passwords are stored securely in the OS keychain."
                )
            }
            row("Username (token):") {
                textField()
                    .columns(COLUMNS_LARGE)
                    .bindText(::ossrhUsername)
                    .also { it.component.toolTipText = "Sonatype user token username" }
            }
            row("Password (token):") {
                val field = JPasswordField(KoltCredentials.ossrhPassword ?: "")
                cell(field).columns(COLUMNS_LARGE)
                    .also { ossrhPasswordField = field }
            }
        }

        // ── GPG Signing ───────────────────────────────────────────────────────
        group("GPG Signing") {
            row {
                comment(
                    "Used for signing release artifacts. The GPG key must be present in your " +
                    "local keyring (<code>gpg --list-secret-keys</code>). Leave <b>Key ID</b> " +
                    "blank to use the default key."
                )
            }
            row {
                checkBox("Sign release publications automatically")
                    .bindSelected(::signOnRelease)
            }
            row("Key ID (optional):") {
                textField()
                    .columns(COLUMNS_MEDIUM)
                    .bindText(::gpgKeyName)
                    .also { it.component.toolTipText = "e.g. ABCDEF01 or user@example.com" }
            }
            row("Passphrase (optional):") {
                val field = JPasswordField(KoltCredentials.gpgPassphrase ?: "")
                cell(field).columns(COLUMNS_LARGE)
                    .also { gpgPassphraseField = field }
                comment("Leave blank if your key has no passphrase or uses gpg-agent.")
            }
        }

        // ── Advanced ──────────────────────────────────────────────────────────
        collapsibleGroup("Advanced") {
            row("Extra Gradle flags:") {
                textField()
                    .columns(COLUMNS_LARGE)
                    .bindText(::extraFlags)
                    .also {
                        it.component.toolTipText =
                            "Appended to every publish invocation. e.g. --stacktrace --no-configuration-cache"
                    }
            }
            row {
                comment(
                    "Credentials can also be set in <code>~/.gradle/gradle.properties</code> as " +
                    "<code>ossrhUsername</code> / <code>ossrhPassword</code>. IDE settings take " +
                    "precedence when both are present."
                )
            }
        }
    }

    override fun isModified(): Boolean {
        val pwChanged  = ossrhPasswordField?.let { String(it.password) } != (KoltCredentials.ossrhPassword ?: "")
        val gpgChanged = gpgPassphraseField?.let { String(it.password) } != (KoltCredentials.gpgPassphrase ?: "")
        return pwChanged || gpgChanged ||
            ossrhUsername != settings.ossrhUsername ||
            gpgKeyName    != settings.gpgKeyName    ||
            signOnRelease != settings.signOnRelease  ||
            extraFlags    != settings.extraGradleFlags
    }

    override fun apply() {
        // Persist non-secret fields
        settings.ossrhUsername   = ossrhUsername
        settings.gpgKeyName      = gpgKeyName
        settings.signOnRelease   = signOnRelease
        settings.extraGradleFlags = extraFlags

        // Persist secrets via OS keychain
        ossrhPasswordField?.let {
            KoltCredentials.ossrhPassword = String(it.password).ifBlank { null }
        }
        gpgPassphraseField?.let {
            KoltCredentials.gpgPassphrase = String(it.password).ifBlank { null }
        }
    }

    override fun reset() {
        ossrhUsername = settings.ossrhUsername
        gpgKeyName    = settings.gpgKeyName
        signOnRelease = settings.signOnRelease
        extraFlags    = settings.extraGradleFlags
        ossrhPasswordField?.text = KoltCredentials.ossrhPassword ?: ""
        gpgPassphraseField?.text = KoltCredentials.gpgPassphrase ?: ""
    }
}

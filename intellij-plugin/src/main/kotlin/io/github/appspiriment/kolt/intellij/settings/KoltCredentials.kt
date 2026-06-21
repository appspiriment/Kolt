package io.github.appspiriment.kolt.intellij.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

/**
 * Secure credential storage for the Kolt plugin.
 *
 * Delegates to IntelliJ's [PasswordSafe], which uses the OS keychain (macOS Keychain,
 * Windows Credential Manager, Linux Secret Service / KeePass fallback) so passwords
 * are never written to disk in plain text.
 */
object KoltCredentials {

    private const val PLUGIN_NAME = "KoltPlugin"

    // ── OSSRH (Sonatype Maven Central) ────────────────────────────────────────

    private val ossrhAttributes = CredentialAttributes(
        generateServiceName(PLUGIN_NAME, "OSSRH"),
        userName = KoltSettings.instance.ossrhUsername.ifBlank { "ossrh" },
    )

    var ossrhPassword: String?
        get() = PasswordSafe.instance.getPassword(ossrhAttributes)
        set(value) {
            PasswordSafe.instance.set(
                ossrhAttributes,
                if (value.isNullOrBlank()) null
                else Credentials(ossrhAttributes.userName ?: "ossrh", value),
            )
        }

    // ── GPG passphrase ────────────────────────────────────────────────────────

    private val gpgAttributes = CredentialAttributes(
        generateServiceName(PLUGIN_NAME, "GPG"),
        userName = "gpg",
    )

    var gpgPassphrase: String?
        get() = PasswordSafe.instance.getPassword(gpgAttributes)
        set(value) {
            PasswordSafe.instance.set(
                gpgAttributes,
                if (value.isNullOrBlank()) null
                else Credentials("gpg", value),
            )
        }

    // ── Convenience: build env-var map for Gradle invocation ──────────────────

    /**
     * Returns environment variables to inject when running Gradle publish tasks.
     * Includes only entries that have values — empty strings are omitted so Gradle
     * can fall back to `~/.gradle/gradle.properties` entries when IDE credentials
     * haven't been configured.
     */
    fun toEnvMap(): Map<String, String> = buildMap {
        val settings = KoltSettings.instance

        settings.ossrhUsername.ifNotBlank { put("OSSRH_USERNAME", it) }
        ossrhPassword?.ifNotBlank           { put("OSSRH_PASSWORD", it) }
        settings.gpgKeyName.ifNotBlank      { put("SIGNING_KEY_ID", it) }
        gpgPassphrase?.ifNotBlank           { put("SIGNING_PASSWORD", it) }
    }

    private fun String.ifNotBlank(block: (String) -> Unit) {
        if (this.isNotBlank()) block(this)
    }
}

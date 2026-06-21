package io.github.appspiriment.kolt.intellij.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Application-level (global) settings for the Kolt plugin.
 *
 * Non-secret fields only — stored in `appspiriment.xml` under the IDE config dir.
 * Secrets (OSSRH password, GPG passphrase) are stored separately via [KoltCredentials]
 * which uses [com.intellij.ide.passwordSafe.PasswordSafe] (OS keychain / KeePass).
 *
 * Access via [KoltSettings.instance].
 */
@Service(Service.Level.APP)
@State(
    name = "KoltSettings",
    storages = [Storage("appspiriment.xml")],
)
class KoltSettings : PersistentStateComponent<KoltSettings> {

    /** Sonatype OSSRH username (token username from https://central.sonatype.com/account). */
    var ossrhUsername: String = ""

    /** GPG key ID or email used for signing releases. Leave blank to use the default key. */
    var gpgKeyName: String = ""

    /**
     * When true, `--release` automatically passes `-PisRelease` which triggers signing.
     * Default: true.
     */
    var signOnRelease: Boolean = true

    /**
     * Extra Gradle flags appended to every publish invocation.
     * e.g. `--no-configuration-cache --stacktrace`
     */
    var extraGradleFlags: String = ""

    // ── Companion ─────────────────────────────────────────────────────────────

    companion object {
        val instance: KoltSettings
            get() = ApplicationManager.getApplication().getService(KoltSettings::class.java)
    }

    // ── PersistentStateComponent ──────────────────────────────────────────────

    override fun getState(): KoltSettings = this

    override fun loadState(state: KoltSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}

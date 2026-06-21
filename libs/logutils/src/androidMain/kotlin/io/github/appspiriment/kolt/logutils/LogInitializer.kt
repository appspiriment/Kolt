package io.github.appspiriment.kolt.logutils

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.startup.Initializer

/**
 * Auto-gates logging based on the host app's build type, with zero consumer boilerplate.
 *
 * Registered via App Startup (see androidMain/AndroidManifest.xml). On app launch it sets
 * [Log.enabled] to whether the application is debuggable — true for debug builds, false for
 * release. Consumers can still override afterwards via [Log.init] / [Log.enabled].
 */
class LogInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val debuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        Log.enabled = debuggable
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

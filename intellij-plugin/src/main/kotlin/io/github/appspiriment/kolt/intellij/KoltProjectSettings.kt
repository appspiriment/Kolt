package io.github.appspiriment.kolt.intellij

/**
 * User-chosen settings collected by [KoltProjectPeer].
 * Passed to [KoltProjectGenerator.generateProject].
 */
data class KoltProjectSettings(
    val projectType: ProjectType = ProjectType.ANDROID,
    val packageName: String = "com.example.myapp",
) {
    enum class ProjectType(val label: String, val templateDir: String) {
        ANDROID("Android (Compose + Hilt)", "templates/android-project"),
        KMP    ("Kotlin Multiplatform (Android + optional iOS/Desktop)", "templates/kmp-project"),
    }
}

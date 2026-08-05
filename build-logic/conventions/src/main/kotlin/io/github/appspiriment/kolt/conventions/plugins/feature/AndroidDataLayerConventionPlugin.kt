package io.github.appspiriment.kolt.conventions.plugins.feature

import io.github.appspiriment.kolt.conventions.extensions.DATA_LAYER_EXTENSION_NAME
import io.github.appspiriment.kolt.conventions.extensions.DataLayerExtension
import io.github.appspiriment.kolt.conventions.extensions.Dependency
import io.github.appspiriment.kolt.conventions.extensions.koltLibs
import io.github.appspiriment.kolt.conventions.extensions.implementDependency
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for Android data layer modules.
 */
class AndroidDataLayerConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            val dataConfig = extensions.create<DataLayerExtension>(DATA_LAYER_EXTENSION_NAME)

            afterEvaluate {
                dependencies {
                    val libs = koltLibs

                    // --- PERSISTENCE (ROOM) ---
                    if (dataConfig.room.enabled.getOrElse(false)) {
                        implementDependency(libs, listOf(
                            Dependency(notation = "androidx.room:room-runtime", versionRef = "room"),
                            Dependency(notation = "androidx.room:room-ktx", versionRef = "room")
                        ))
                        add("ksp", "androidx.room:room-compiler:${libs.findVersion("room").get()}")

                        if (dataConfig.room.usePaging.getOrElse(false)) {
                            implementDependency(libs, listOf(
                                Dependency(notation = "androidx.room:room-paging", versionRef = "room")
                            ))
                        }
                    }

                    // --- SECURITY ---
                    if (dataConfig.security.enabled.getOrElse(false)) {
                        implementDependency(libs, listOf(
                            Dependency(notation = "androidx.security:security-crypto", versionRef = "security"),
                            Dependency(notation = "com.google.crypto.tink:tink-android", versionRef = "tink")
                        ))
                    }

                    // --- DATASTORE ---
                    if (dataConfig.dataStore.enabled.getOrElse(false)) {
                        implementDependency(libs, listOf(
                            Dependency(notation = "androidx.datastore:datastore-preferences", versionRef = "datastore")
                        ))
                    }

                    // --- WORK MANAGER ---
                    if (dataConfig.workManager.enabled.getOrElse(false)) {
                        implementDependency(libs, listOf(
                            Dependency(notation = "androidx.work:work-runtime-ktx", versionRef = "work"),
                            Dependency(notation = "androidx.hilt:hilt-work", versionRef = "hiltWork")
                        ))
                        add("ksp", "androidx.hilt:hilt-compiler:${libs.findVersion("hiltWork").get()}")
                    }

                    // --- NETWORKING (RETROFIT) ---
                    if (dataConfig.retrofit.enabled.getOrElse(false)) {
                        implementDependency(libs, listOf(
                            Dependency(notation = "com.squareup.retrofit2:retrofit", versionRef = "retrofit"),
                            Dependency(notation = "com.squareup.okhttp3:logging-interceptor", versionRef = "loggingInterceptor"),
                            Dependency(notation = "com.squareup.retrofit2:converter-gson", versionRef = "retrofit")
                        ))

                        if (dataConfig.retrofit.useChucker.getOrElse(false)) {
                            add("debugImplementation", "com.github.chuckerteam.chucker:library:${libs.findVersion("chucker").get()}")
                            add("releaseImplementation", "com.github.chuckerteam.chucker:library-no-op:${libs.findVersion("chucker").get()}")
                        }

                        if (dataConfig.retrofit.useKotlinSerialization.getOrElse(false)) {
                            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
                            implementDependency(libs, listOf(
                                Dependency(notation = "com.squareup.retrofit2:converter-kotlinx-serialization", versionRef = "retrofitSerialization")
                            ))
                        }
                    }
                }
            }
        }
    }
}

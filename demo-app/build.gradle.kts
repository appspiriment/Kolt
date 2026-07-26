plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.android.application")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(project(":libs:compose-kmp"))
                implementation(project(":libs:utils"))
                implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.9.0")
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation(project(":libs:compose-utils"))
                implementation(project(":libs:update-utils"))
                implementation(project(":libs:location-picker"))
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":libs:location-picker"))
            }
        }
    }
}

android {
    namespace = "io.github.appspiriment.kolt.demo"
    compileSdk = 36
    defaultConfig {
        applicationId = "io.github.appspiriment.kolt.demo"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        mainClass = "io.github.appspiriment.kolt.demo.MainKt"
    }
}

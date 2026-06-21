// ──────────────────────────────────────────────────────────────────────────────
// Kolt BOM (Bill of Materials)
//
// A Maven platform POM that constrains all Kolt library versions to a
// known-compatible set. Consumers add this once:
//
//   implementation(platform("io.github.appspiriment.kolt:kolt-bom:<version>"))
//
// After which all `io.github.appspiriment.kolt:*` artifacts can be declared without
// explicit versions. The convention plugins automatically inject this BOM, so
// most consumer projects never need to add it manually.
//
// Version: set by the root build.gradle.kts from BOM_VERSION in version.properties.
// Format: YYYY.MM.PATCH  (calendar-based, like the AndroidX BOM)
// ──────────────────────────────────────────────────────────────────────────────

plugins {
    `java-platform`
    id("io.github.appspiriment.kolt.publish")
}

// Lib versions are exposed as extra properties by the root build.gradle.kts,
// which reads them from version.properties. This avoids re-parsing the file here.
val utilsVersion:        String by rootProject.extra
val logutilsVersion:     String by rootProject.extra
val composeUtilsVersion: String by rootProject.extra
val composeKmpVersion:   String by rootProject.extra
val updateUtilsVersion:  String by rootProject.extra
val locationVersion:     String by rootProject.extra

dependencies {
    constraints {
        api("io.github.appspiriment.kolt:utils:$utilsVersion")
        api("io.github.appspiriment.kolt:logutils:$logutilsVersion")
        api("io.github.appspiriment.kolt:compose:$composeUtilsVersion")
        api("io.github.appspiriment.kolt:compose-kmp:$composeKmpVersion")
        api("io.github.appspiriment.kolt:update-utils:$updateUtilsVersion")
        api("io.github.appspiriment.kolt:location:$locationVersion")
    }
}

mavenPublishing {
    coordinates(artifactId = "kolt-bom")
    pom {
        name = "Kolt BOM"
        description = "Bill of Materials for the Kolt library suite — " +
            "constrains utils, logutils, compose, and update-utils to a " +
            "known-compatible version set."
        url = "https://github.com/appspiriment/android-kmp-utils"
        licenses {
            license {
                name = "Apache-2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }
        developers {
            developer {
                id = "appspiriment"
                name = "Appspiriment"
                url = "https://github.com/appspiriment"
            }
        }
        scm {
            connection = "scm:git:git://github.com/appspiriment/android-kmp-utils.git"
            developerConnection = "scm:git:ssh://github.com/appspiriment/android-kmp-utils.git"
            url = "https://github.com/appspiriment/android-kmp-utils"
        }
    }
}

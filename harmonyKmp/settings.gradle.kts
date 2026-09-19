// harmonyKmp/settings.gradle.kts
// Standalone build for the HarmonyOS .so.
// Isolated from the main project so JVM/Android builds are never affected.
//
// The Kotlin-OHOS fork KGP (2.1.255-SNAPSHOT) and the fork-patched kotlinx
// libraries come from the extracted repo_kotlinx.zip (CI downloads + extracts it).
//
// Usage (in CI, macOS ARM64 runner):
//   HARMONY_KOTLIN_REPO=/path/to/repo ./gradlew :linkHarmonyOSArm64

pluginManagement {
    repositories {
        // Local maven repo from Kotlin-OHOS repo_kotlinx.zip (fork KGP + kotlinx snapshots)
        val harmonyKotlinRepo = System.getenv("HARMONY_KOTLIN_REPO")
        if (harmonyKotlinRepo != null) {
            maven { url = uri(harmonyKotlinRepo) }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        val harmonyKotlinRepo = System.getenv("HARMONY_KOTLIN_REPO")
        if (harmonyKotlinRepo != null) {
            maven { url = uri(harmonyKotlinRepo) }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "harmonyKmp"
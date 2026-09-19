// harmonyKmp/build.gradle.kts
// HarmonyOS .so build using the Kotlin-OHOS fork (Kotlin 2.1.255-SNAPSHOT).
// Produces libshared.so + shared.h header for KNOI/ArkTS bridge.

plugins {
    kotlin("multiplatform") version "2.1.255-SNAPSHOT"
}

kotlin {
    harmonyOSArm64 {
        binaries.sharedLib {
            baseName = "shared"
        }
        compilerOptions {
            // JDK 8 aarch64 is required by Kotlin/Native cinterop
            freeCompilerArgs.add("-Xjdk-release=1.8")
        }
    }

    sourceSets {
        val harmonyOSArm64Main by getting {
            dependencies {
                // Pure Kotlin subset only for v1 — no SQLDelight/Ktor until
                // native variants for this target are verified.
                // compat: coroutines snapshot from fork repo
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2-SNAPSHOT")
            }
        }
        val harmonyOSArm64Test by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
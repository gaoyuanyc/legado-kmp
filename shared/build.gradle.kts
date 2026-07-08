// shared/build.gradle.kts - KMP Module for Legado
//
// BUILD INSTRUCTIONS:
// 1. Ensure JAVA_HOME points to JDK 17 (already configured in local.properties)
// 2. Android target: .\gradlew :shared:compileKotlinAndroid
// 3. HarmonyOS .so:  .\gradlew :shared:linkHarmonyOSArm64  (requires macOS ARM64 + KBA toolchain)
//
// NOTE: SQLDelight, Ktor, quickjs-kt dependencies require network access to Maven repositories.
//       The code structure and interface definitions below are complete and ready for compilation
//       when network access is available. On Windows x86_64, only the Android target can be built.

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sql.delight)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // JVM target for unit testing
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }

    // HarmonyOS target via Kotlin/Native
    // NOTE: harmonyOSArm64 is NOT a standard Kotlin/Native 2.3.21 target.
    // It requires a custom Kotlin-OHOS fork or KuiklyBase toolchain.
    // Disabled to keep JVM/Android builds working.
    //
    // harmonyOSArm64 {
    //     binaries.sharedLib {
    //         baseName = "shared"
    //     }
    // }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // SQLDelight (database)
                implementation(libs.sql.delight.runtime)
                implementation(libs.sql.delight.coroutines)
                implementation(libs.sql.delight.primitive.adapters)
                
                // Ktor (networking)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                
                // kotlinx (serialization, datetime, coroutines)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                
                // Okio (multiplatform I/O)
                implementation("com.squareup.okio:okio:3.10.2")
                
                // Koin (dependency injection)
                implementation("io.insert-koin:koin-core:4.0.2")
                
                // QuickJS for JS execution (native targets)
                // TODO: quickjs-kt not on Maven Central, need to add custom repo
                // implementation("com.dokar:quickjs-kt:1.0.3")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
                implementation("app.cash.sqldelight:sqlite-driver:2.3.2")
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {
                // kotlinx.coroutines JVM
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                
                // SQLDelight JVM driver
                implementation("app.cash.sqldelight:sqlite-driver:2.3.2")
                
                // Ktor JVM engine (CIO)
                implementation("io.ktor:ktor-client-cio:3.1.0")
                
                // Jsoup for HTML parsing
                implementation("org.jsoup:jsoup:1.16.2")
                
                // Rhino for JS execution
                implementation("org.mozilla:rhino:1.8.1")
                
                // OkHttp for HTTP
                implementation("com.squareup.okhttp3:okhttp:5.3.2")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("app.cash.sqldelight:sqlite-driver:2.3.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                // SQLDelight Android driver
                implementation(libs.sql.delight.android.driver)
                
                // Ktor Android engine
                implementation(libs.ktor.client.okhttp)
                
                // OkHttp
                implementation("com.squareup.okhttp3:okhttp:5.3.2")
                
                // Rhino (Android JS engine)
                implementation("org.mozilla:rhino:1.8.1")
                
                // Jsoup for HTML parsing
                implementation("org.jsoup:jsoup:1.16.2")
                
                // kotlinx.coroutines Android
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
            }
        }

        // val harmonyOSArm64Main by getting {
        //     dependsOn(commonMain)
        //     dependencies {
        //         implementation(libs.sql.delight.native.driver)
        //         implementation(libs.ktor.client.curl)
        //     }
        // }
    }
}

android {
    namespace = "io.legado.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("io.legado.shared.data")
        }
    }
}

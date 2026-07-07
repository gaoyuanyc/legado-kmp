// Top-level build file for legado-kmp
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.3.21" apply false
    id("com.android.library") version "8.13.2" apply false
    id("app.cash.sqldelight") version "2.0.2" apply false
}

tasks.register("clean", Delete) {
    delete rootProject.layout.buildDirectory
}

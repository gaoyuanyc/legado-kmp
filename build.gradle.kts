// Top-level build file for legado-kmp
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.4.0" apply false
    id("com.android.library") version "8.13.2" apply false
    id("com.android.application") version "8.13.2" apply false
    id("app.cash.sqldelight") version "2.0.2" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

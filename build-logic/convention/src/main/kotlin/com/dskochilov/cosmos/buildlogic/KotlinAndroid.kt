package com.dskochilov.cosmos.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

/**
 * Shared Android configuration for application and library modules.
 *
 * Kotlin itself is compiled by AGP's built-in Kotlin support (AGP 9+), so there is no
 * explicit Kotlin plugin here; the Kotlin jvmTarget defaults to [JavaVersion] set below.
 */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        compileSdk {
            version = release(37)
        }

        defaultConfig.minSdk = 26

        compileOptions.sourceCompatibility = JavaVersion.VERSION_11
        compileOptions.targetCompatibility = JavaVersion.VERSION_11
    }
}

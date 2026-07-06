package com.dskochilov.cosmos.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Adds Compose support to a module. Must be applied after
 * `cosmos.android.application` or `cosmos.android.library`.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val commonExtension = extensions.getByType(CommonExtension::class.java)
            configureAndroidCompose(commonExtension)
        }
    }
}

package com.dskochilov.cosmos.buildlogic

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/**
 * Applies static analysis (detekt + ktlint, both with Compose rule sets) to a module.
 * Both plugins hook their verification tasks into the `check` lifecycle task.
 */
internal fun Project.configureQuality() {
    pluginManager.apply("io.gitlab.arturbosch.detekt")
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")

    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    }

    extensions.configure<KtlintExtension> {
        version.set(libs.findVersion("ktlint").get().requiredVersion)
    }

    dependencies.add("detektPlugins", libs.findLibrary("compose-rules-detekt").get())
    dependencies.add("ktlintRuleset", libs.findLibrary("compose-rules-ktlint").get())
}

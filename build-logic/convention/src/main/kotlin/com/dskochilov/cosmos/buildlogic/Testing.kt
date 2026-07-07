package com.dskochilov.cosmos.buildlogic

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

/**
 * Runs unit tests on the JUnit Platform (JUnit 5/Jupiter) and adds the required
 * dependencies. Gradle 9 requires the platform launcher on the test runtime classpath.
 */
internal fun Project.configureJUnitJupiter() {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies.add("testImplementation", libs.findLibrary("junit-jupiter").get())
    dependencies.add("testRuntimeOnly", libs.findLibrary("junit-platform-launcher").get())
}

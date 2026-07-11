plugins {
    alias(libs.plugins.cosmos.jvm.library)
}

dependencies {
    api(project(":core:common"))
    // paging-common is a pure Kotlin/JVM artifact; see ADR 0005.
    api(libs.androidx.paging.common)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

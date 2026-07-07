plugins {
    alias(libs.plugins.cosmos.jvm.library)
}

dependencies {
    api(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

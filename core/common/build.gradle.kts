plugins {
    alias(libs.plugins.cosmos.jvm.library)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}

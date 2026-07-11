plugins {
    alias(libs.plugins.cosmos.jvm.library)
}

dependencies {
    api(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
}

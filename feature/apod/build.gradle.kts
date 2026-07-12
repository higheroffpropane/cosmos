plugins {
    alias(libs.plugins.cosmos.android.feature)
}

android {
    namespace = "com.dskochilov.cosmos.feature.apod"
}

dependencies {
    implementation(libs.coil.compose)
    // Registers Coil's OkHttp network fetcher via ServiceLoader at app runtime.
    implementation(libs.coil.network.okhttp)

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}

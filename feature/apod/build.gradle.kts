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
}

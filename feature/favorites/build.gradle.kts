plugins {
    alias(libs.plugins.cosmos.android.feature)
}

android {
    namespace = "com.dskochilov.cosmos.feature.favorites"
}

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}

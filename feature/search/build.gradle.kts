plugins {
    alias(libs.plugins.cosmos.android.feature)
}

android {
    namespace = "com.dskochilov.cosmos.feature.search"
}

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.paging.compose)

    testImplementation(libs.androidx.paging.testing)
}

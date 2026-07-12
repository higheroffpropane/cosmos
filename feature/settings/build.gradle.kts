plugins {
    alias(libs.plugins.cosmos.android.feature)
}

android {
    namespace = "com.dskochilov.cosmos.feature.settings"
}

dependencies {
    implementation(libs.androidx.activity.compose)
}

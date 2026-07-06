plugins {
    alias(libs.plugins.cosmos.android.library)
    alias(libs.plugins.cosmos.android.compose)
}

android {
    namespace = "com.dskochilov.cosmos.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material3)
}

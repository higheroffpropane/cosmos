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
    // Material3 1.4 no longer exposes the icon set transitively.
    api(libs.androidx.compose.material.icons.core)
}

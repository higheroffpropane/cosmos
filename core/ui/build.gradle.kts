plugins {
    alias(libs.plugins.cosmos.android.library)
    alias(libs.plugins.cosmos.android.compose)
}

android {
    namespace = "com.dskochilov.cosmos.core.ui"
}

dependencies {
    implementation(project(":core:designsystem"))
}

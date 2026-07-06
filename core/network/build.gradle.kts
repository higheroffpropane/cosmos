plugins {
    alias(libs.plugins.cosmos.android.library)
}

android {
    namespace = "com.dskochilov.cosmos.core.network"
}

dependencies {
    implementation(project(":core:common"))
}

plugins {
    alias(libs.plugins.cosmos.android.library)
}

android {
    namespace = "com.dskochilov.cosmos.core.database"
}

dependencies {
    implementation(project(":core:common"))
}

plugins {
    alias(libs.plugins.cosmos.android.library)
}

android {
    namespace = "com.dskochilov.cosmos.core.datastore"
}

dependencies {
    implementation(project(":core:common"))
}

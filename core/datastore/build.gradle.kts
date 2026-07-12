plugins {
    alias(libs.plugins.cosmos.android.library)
    alias(libs.plugins.cosmos.hilt)
}

android {
    namespace = "com.dskochilov.cosmos.core.datastore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
}

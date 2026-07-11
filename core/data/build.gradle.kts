plugins {
    alias(libs.plugins.cosmos.android.library)
    alias(libs.plugins.cosmos.hilt)
}

android {
    namespace = "com.dskochilov.cosmos.core.data"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.retrofit.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.okhttp)
}

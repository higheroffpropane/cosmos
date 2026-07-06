plugins {
    alias(libs.plugins.cosmos.android.library)
}

android {
    namespace = "com.dskochilov.cosmos.core.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
}

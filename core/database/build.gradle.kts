plugins {
    alias(libs.plugins.cosmos.android.library)
    alias(libs.plugins.cosmos.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "com.dskochilov.cosmos.core.database"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    api(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.paging.common)
    ksp(libs.androidx.room.compiler)
}

import java.util.Properties

plugins {
    alias(libs.plugins.cosmos.android.library)
    alias(libs.plugins.cosmos.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// The key comes from local.properties (developer machines) or the NASA_API_KEY
// environment variable (CI). It is never committed.
val nasaApiKey: String = run {
    val localProperties = Properties()
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(localProperties::load)
    }
    (localProperties.getProperty("NASA_API_KEY") ?: System.getenv("NASA_API_KEY") ?: "")
        .trim()
        .removeSurrounding("\"")
}

android {
    namespace = "com.dskochilov.cosmos.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "NASA_API_KEY", "\"$nasaApiKey\"")
    }
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
}

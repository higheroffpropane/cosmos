plugins {
    `kotlin-dsl`
}

group = "com.dskochilov.cosmos.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "cosmos.android.application"
            implementationClass = "com.dskochilov.cosmos.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "cosmos.android.library"
            implementationClass = "com.dskochilov.cosmos.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "cosmos.android.compose"
            implementationClass = "com.dskochilov.cosmos.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "cosmos.android.feature"
            implementationClass = "com.dskochilov.cosmos.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("jvmLibrary") {
            id = "cosmos.jvm.library"
            implementationClass = "com.dskochilov.cosmos.buildlogic.JvmLibraryConventionPlugin"
        }
        register("hilt") {
            id = "cosmos.hilt"
            implementationClass = "com.dskochilov.cosmos.buildlogic.HiltConventionPlugin"
        }
    }
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "io.github.amirisback.androidapp.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradleApiPlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.androidapp.android.application.asProvider().get().pluginId
            implementationClass =
                "io.github.amirisback.androidapp.convention.AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = libs.plugins.androidapp.android.application.compose.get().pluginId
            implementationClass =
                "io.github.amirisback.androidapp.convention.AndroidApplicationComposeConventionPlugin"
        }
        register("androidHilt") {
            id = libs.plugins.androidapp.android.hilt.get().pluginId
            implementationClass =
                "io.github.amirisback.androidapp.convention.AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = libs.plugins.androidapp.android.room.get().pluginId
            implementationClass =
                "io.github.amirisback.androidapp.convention.AndroidRoomConventionPlugin"
        }
    }
}

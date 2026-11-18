package io.github.amirisback.androidapp.convention

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            extensions.configure<BasePluginExtension> {
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_hh-mm"))
                archivesName.set("${ProjectSetting.NAME_APK}-${ProjectSetting.PROJECT_VERSION_CODE}-$timestamp")
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = ProjectSetting.PROJECT_COMPILE_SDK

                defaultConfig {
                    applicationId = ProjectSetting.PROJECT_APP_ID
                    minSdk = ProjectSetting.PROJECT_MIN_SDK
                    targetSdk = ProjectSetting.PROJECT_TARGET_SDK
                    versionCode = ProjectSetting.PROJECT_VERSION_CODE
                    versionName = ProjectSetting.PROJECT_VERSION_NAME

                    vectorDrawables.useSupportLibrary = true
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                    buildConfigField("String", "DATABASE_NAME", ProjectSetting.DB)
                    buildConfigField("String", "PREF_NAME", ProjectSetting.PREF)

                    // Debug resource values
                    resValue("string", "app_name", ProjectSetting.NAME_APP_DEBUG)
                    resValue("string", "admob_app_id", ProjectAds.Admob.Debug.APP_ID)
                    resValue("string", "admob_banner", ProjectAds.Admob.Debug.BANNER_ID)
                    resValue("string", "admob_interstitial_id", ProjectAds.Admob.Debug.INTERSTITIAL_ID)
                    resValue("string", "admob_rewarded_id", ProjectAds.Admob.Debug.REWARDED_ID)
                }

                signingConfigs {
                    create("release") {
                        storeFile = project.file(ProjectSetting.PLAYSTORE_STORE_FILE)
                        storePassword = ProjectSetting.PLAYSTORE_STORE_PASSWORD
                        keyAlias = ProjectSetting.PLAYSTORE_KEY_ALIAS
                        keyPassword = ProjectSetting.PLAYSTORE_KEY_PASSWORD
                    }
                }

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false

                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )

                        signingConfig = signingConfigs.getByName("release")

                        // Release resource values
                        resValue("string", "app_name", ProjectSetting.NAME_APP)
                        resValue("string", "admob_app_id", ProjectAds.Admob.APP_ID)
                        resValue("string", "admob_banner", ProjectAds.Admob.BANNER_ID)
                        resValue("string", "admob_interstitial_id", ProjectAds.Admob.INTERSTITIAL_ID)
                        resValue("string", "admob_rewarded_id", ProjectAds.Admob.REWARDED_ID)
                    }
                }

                buildFeatures {
                    buildConfig = true
                    resValues = true
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                    freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
                }
            }
        }
    }
}

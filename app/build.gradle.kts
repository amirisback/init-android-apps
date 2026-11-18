plugins {
    alias(libs.plugins.androidapp.android.application)
    alias(libs.plugins.androidapp.android.application.compose)
    alias(libs.plugins.androidapp.android.hilt)
    alias(libs.plugins.androidapp.android.room)
    alias(libs.plugins.kotlinParcelize)
}

android {
    namespace = "io.github.amirisback.androidapp"
}

dependencies {
    debugImplementation(libs.github.chucker)
    releaseImplementation(libs.github.chucker.no.op)

    implementation(libs.frogo.android) {
        exclude(
            group = "com.github.chuckerteam.chucker",
            module = "library",
        )
        exclude(
            group = "com.github.chuckerteam.chucker",
            module = "library-no-op",
        )
    }

    ksp(libs.androidx.lifecycle.compiler)

    // Image Loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Game Core (Persistence & Monetization)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.google.play.services.ads)

    // Unit Testing (JVM)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

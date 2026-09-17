plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.chargenotifier.shared"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
}

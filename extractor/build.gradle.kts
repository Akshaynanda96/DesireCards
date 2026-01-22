plugins {
    id("com.android.library")
}

android {
    namespace = "org.schabi.newpipe.extractor"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.firebase.crashlytics.buildtools)
    implementation("com.google.code.findbugs:jsr305:3.0.2")
}

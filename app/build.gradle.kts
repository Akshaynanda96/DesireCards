plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.desirecards"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.desirecards"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.media:media:1.7.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.google.code.gson:gson:2.11.0")
//    implementation(project(":extractor"))
}
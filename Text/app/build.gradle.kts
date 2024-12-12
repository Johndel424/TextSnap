plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.text"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.text"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.google.mlkit:text-recognition:16.0.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    // add the dependency for the Google AI client SDK for Android
    implementation("com.google.ai.client.generativeai:generativeai:0.2.0")
    // Required for one-shot operations (to use `ListenableFuture` from Reactive Streams)
    implementation("com.google.guava:guava:31.0.1-android")
    // Required for streaming operations (to use `Publisher` from Guava Android)
    implementation("org.reactivestreams:reactive-streams:1.0.4")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("org.apache.poi:poi-ooxml:5.2.3")

}
import org.gradle.kotlin.dsl.implementation

plugins {
    id("com.google.devtools.ksp")
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.lab1"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.myapplication"
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

    implementation("androidx.room:room-runtime:2.8.4")
    implementation(libs.androidx.room.ktx)

    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See KSP Quickstart to add KSP to your build
    ksp("androidx.room:room-compiler:2.8.4")
    // If this project only uses Java source, use the Java annotationProcessor
    // No additional plugins are necessary
    annotationProcessor("androidx.room:room-compiler:2.8.4")

    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:2.8.4")

    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:2.8.4")

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:2.8.4")

    // optional - Test helpers
    testImplementation("androidx.room:room-testing:2.8.4")

    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:2.8.4")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
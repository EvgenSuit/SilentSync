plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp.plugin)
}

android {
    namespace = "com.suit.dndCalendar.impl"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(project(":dndCalendar:api"))
    implementation(project(":utility"))

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.androidx.core.ktx)

    testImplementation(project(":utility"))
    testImplementation(libs.junit)
    testImplementation(libs.androidx.arch.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.test.core.ktx)
    testImplementation(libs.androidx.sqlite)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.serialization)
    alias(libs.plugins.build.config)
    alias(libs.plugins.ksp.plugin)
    alias(libs.plugins.androidx.room)
}

buildConfig {
    buildConfigField("MAPBOX_ACCESS_TOKEN", gradleLocalProperties(rootDir, providers).getProperty("MAPBOX_ACCESS_TOKEN") ?: "")
}

android {
    namespace = "com.suit.dndlocation.impl"
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
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(project(":utility"))
    implementation(project(":dndLocation:api"))

    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.play.services.location)
    implementation(libs.coroutines.play.services)

    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    implementation(platform(libs.ktor.bom))
    implementation(libs.androidx.datastore)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    testImplementation(project(":testUtil"))
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(kotlin("test"))
}
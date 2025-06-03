import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false

    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
subprojects {
    plugins.withId("com.android.application") {
        configure<AppExtension> {
            buildTypes {
                getByName("debug").manifestPlaceholders.putAll(
                    mapOf(
                        "analyticsCollectionEnabled" to false,
                        "crashlyticsCollectionEnabled" to false
                    )
                )
                getByName("release").manifestPlaceholders.putAll(
                    mapOf(
                        "analyticsCollectionEnabled" to true,
                        "crashlyticsCollectionEnabled" to true
                    )
                )
            }
        }
    }
    plugins.withId("com.android.library") {
        configure<LibraryExtension> {
            buildTypes {
                getByName("debug").manifestPlaceholders.putAll(
                    mapOf(
                        "analyticsCollectionEnabled" to false,
                        "crashlyticsCollectionEnabled" to false
                    )
                )
                getByName("release").manifestPlaceholders.putAll(
                    mapOf(
                        "analyticsCollectionEnabled" to true,
                        "crashlyticsCollectionEnabled" to true
                    )
                )
            }
            testOptions.unitTests.isIncludeAndroidResources = true
        }
    }
}
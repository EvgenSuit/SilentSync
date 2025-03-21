package com.suit.utility.koin

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.suit.utility.analytics.SilentSyncAnalytics
import org.koin.dsl.module

val utilKoinModule = module {
    single {
        SilentSyncAnalytics(
            firebaseAnalytics = Firebase.analytics,
            firebaseCrashlytics = Firebase.crashlytics
        )
    }
}
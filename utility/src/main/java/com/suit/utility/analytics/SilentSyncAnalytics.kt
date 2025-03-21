package com.suit.utility.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

enum class SilentSyncEvent {
    SYNC_EVENTS,
    DND_ON,
    DND_OFF
}

class SilentSyncAnalytics(
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun recordException(e: Exception) {
        firebaseCrashlytics.recordException(e)
    }
    fun logEvent(event: SilentSyncEvent) {
        firebaseAnalytics.logEvent(event.name, null)
    }
}
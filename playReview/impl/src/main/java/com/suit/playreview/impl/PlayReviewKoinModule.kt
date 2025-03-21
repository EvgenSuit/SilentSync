package com.suit.playreview.impl

import androidx.datastore.dataStore
import com.google.android.gms.time.TrustedTimeClient
import com.suit.playreview.api.PlayReviewManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.lazyModule
import org.koin.dsl.module

fun playReviewKoinModule(trustedTimeClient: TrustedTimeClient?) = lazyModule {
    single<PlayReviewManager> {
        PlayReviewManagerImpl(
            trustedTimeClient = trustedTimeClient,
            dataStore = androidContext().playReviewDatastore
        )
    }
}
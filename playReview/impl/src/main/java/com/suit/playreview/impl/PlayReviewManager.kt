package com.suit.playreview.impl

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.google.android.gms.time.TrustedTimeClient
import com.suit.playreview.api.PlayReviewData
import com.suit.playreview.api.PlayReviewManager
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant

val Context.playReviewDatastore by dataStore("play_review_data.json", PlayReviewDataSerializer)
class PlayReviewManagerImpl(
    private val trustedTimeClient: TrustedTimeClient?,
    private val dataStore: DataStore<PlayReviewData>
): PlayReviewManager {
    private suspend fun getData(): PlayReviewData =
        dataStore.data.first()

    private suspend fun recordInstallTime(
        currTime: Instant
    ) =
        dataStore.updateData {
            PlayReviewData(
                didShow = false,
                installTime = currTime
            )
        }

    private fun isEligible(
        currTime: Instant,
        installTime: Instant
    ): Boolean = Duration.between(installTime, currTime) >= Duration.ofMinutes(1)

    override suspend fun doShowDialog(): Boolean {
        val data = getData()
        Log.d("PlayReview", "Returning false: ${trustedTimeClient == null || (data.installTime != null && data.didShow)}")
        if (trustedTimeClient == null || (data.installTime != null && data.didShow)) return false

        val installTime = data.installTime
        Log.d("PlayReview", "Instant: $installTime")
        if (installTime == null) {
            val currTime = trustedTimeClient.computeCurrentInstant()
            Log.d("PlayReview", "Time: $currTime")
            if (currTime == null) return false
            recordInstallTime(currTime)
            return false
        }
        val currTime = trustedTimeClient.computeCurrentInstant() ?: return false
        return isEligible(
            currTime = currTime,
            installTime = installTime
        )
    }
    override suspend fun labelDialogAsShown() {
        dataStore.updateData {
            PlayReviewData(
                didShow = true,
                installTime = it.installTime
            )
        }
        Log.d("PlayReview", "labeled as shown")
    }
}
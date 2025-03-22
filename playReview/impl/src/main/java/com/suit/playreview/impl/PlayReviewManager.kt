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
internal enum class PlayReviewManagerDiff(val days: Long) {
    DAYS(3)
}
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
    ): Boolean = Duration.between(installTime, currTime) >= Duration.ofDays(PlayReviewManagerDiff.DAYS.days)

    override suspend fun doShowDialog(): Boolean {
        val data = getData()
        if (trustedTimeClient == null || (data.installTime != null && data.didShow)) return false

        val installTime = data.installTime
        if (installTime == null) {
            val currTime = trustedTimeClient.computeCurrentInstant()
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
    }
}
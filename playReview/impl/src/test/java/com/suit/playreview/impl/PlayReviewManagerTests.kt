package com.suit.playreview.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.time.TrustedTimeClient
import com.suit.playreview.api.PlayReviewData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.temporal.ChronoUnit

@RunWith(RobolectricTestRunner::class)
class PlayReviewManagerTests {
    private lateinit var playReviewManager: PlayReviewManagerImpl
    private var trustedTimeClient: TrustedTimeClient? = null
    private lateinit var dataStore: DataStore<PlayReviewData>
    private val currInstant = Instant.now()

    private fun setupTimeClient(
        instant: Instant = currInstant
    ) {
        trustedTimeClient = mockk {
            coEvery { computeCurrentInstant() } returns instant
        }
    }
    private fun setupDatastore() {
        dataStore = ApplicationProvider.getApplicationContext<Context>()
            .playReviewDatastore
    }

    private fun setupManager() {
        playReviewManager = PlayReviewManagerImpl(
            trustedTimeClient = trustedTimeClient,
            dataStore = dataStore
        )
    }

    @Before
    fun setup() {
        setupTimeClient()
        setupDatastore()
        setupManager()
    }
    @After
    fun cleanup() {
        runBlocking { dataStore.updateData { PlayReviewData() } }
    }

    @Test
    fun trustedTimeClientNull_dialogNotShown() = runTest {
        trustedTimeClient = null
        setupManager()

        assertFalse(playReviewManager.doShowDialog())
        assertEquals(
            PlayReviewData(),
            dataStore.data.first()
        )
    }

    @Test
    fun installTimeNotNullAndDidShow_dialogNotShown() = runTest {
        dataStore.updateData {
            PlayReviewData(
                didShow = true,
                installTime = currInstant
            )
        }
        assertFalse(playReviewManager.doShowDialog())
        coVerify(inverse = true) { trustedTimeClient!!.computeCurrentInstant() }
    }

    @Test
    fun installTimeNullAndDidNotShow_installTimeRecorded_dialogNotShown() = runTest {
        assertFalse(playReviewManager.doShowDialog())
        coVerify(exactly = 1) { trustedTimeClient!!.computeCurrentInstant() }
        assertEquals(
            PlayReviewData(
                didShow = false,
                installTime = currInstant
            ),
            dataStore.data.first()
        )
    }
    @Test
    fun installTimeNotNullAndDidNotShow_isNotEligible_dialogNotShown() = runTest {
        setupTimeClient(instant = currInstant.plus(PlayReviewManagerDiff.DAYS.days-1,
            ChronoUnit.DAYS))
        dataStore.updateData {
            PlayReviewData(
                installTime = currInstant
            )
        }
        setupManager()
        assertFalse(playReviewManager.doShowDialog())
        coVerify(exactly = 1) { trustedTimeClient!!.computeCurrentInstant() }
    }
    @Test
    fun installTimeNotNullAndDidNotShow_isEligible_dialogShown() = runTest {
        setupTimeClient(instant = currInstant.plus(PlayReviewManagerDiff.DAYS.days,
            ChronoUnit.DAYS))
        dataStore.updateData {
            PlayReviewData(
                installTime = currInstant
            )
        }
        setupManager()
        assertTrue(playReviewManager.doShowDialog())
        coVerify(exactly = 1) { trustedTimeClient!!.computeCurrentInstant() }
    }

    @Test
    fun labelDialogAsShown_didShowTrue_installTimeNotChanged() = runTest {
        playReviewManager.doShowDialog()
        playReviewManager.labelDialogAsShown()
        assertEquals(
            PlayReviewData(
                didShow = true,
                installTime = currInstant
            ),
            dataStore.data.first()
        )
    }
}
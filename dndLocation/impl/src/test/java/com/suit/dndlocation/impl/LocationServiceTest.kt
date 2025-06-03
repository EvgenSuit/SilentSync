package com.suit.dndlocation.impl

import android.app.AlarmManager
import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.core.app.ServiceCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Task
import com.suit.dndlocation.api.RadiusMeasurement
import com.suit.dndlocation.api.SavedLocation
import com.suit.dndlocation.impl.db.SavedLocationsDAO
import com.suit.dndlocation.impl.db.SavedLocationsDb
import com.suit.testutil.test.MainDispatcherRule
import com.suit.utility.analytics.SilentSyncAnalytics
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class LocationServiceTest {
    @get: Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var service: LocationService

    private val mockFusedClient = mockk<FusedLocationProviderClient>(relaxed = true)
    private val mockAnalytics = mockk<SilentSyncAnalytics>(relaxed = true)
    private val mockSavedLocationsDb = mockk<SavedLocationsDb>(relaxed = true)
    private val mockSavedLocationsDAO = mockk<SavedLocationsDAO>(relaxed = true)
    private val mockNotificationManager = mockk<NotificationManager>(relaxed = true)
    private val mockAlarmManager = mockk<AlarmManager>(relaxed = true)

    private val callbackSlot = slot<LocationCallback>()


    @Before
    fun setup() {
        every { mockSavedLocationsDb.savedLocationDao() } returns mockSavedLocationsDAO
        every {
            mockFusedClient.requestLocationUpdates(
                any<LocationRequest>(),
                capture(callbackSlot),
                any()
            )
        } returns mockk<Task<Void>>()
        service = Robolectric.setupService(LocationService::class.java)

        val testModule = module {
            single { mockFusedClient }
            single { mockAnalytics }
            single { mockSavedLocationsDb }
            single { mockNotificationManager }
            single { mockAlarmManager }
            single<CoroutineScope> { TestScope(mainDispatcherRule.dispatcher) }
        }
        startKoin {
            modules(testModule)
        }
    }
    @After
    fun tearDown() {
        unmockkAll()
        stopKoin()
    }

    @Test
    fun `when entering a zone with turnDNDOnUponEntering = true, update status and turn on DND`() =
        runTest {
            val zoneId = 123L
            val savedLocation = SavedLocation(
                id = zoneId,
                fullAddress = "Test zone",
                latitude = 100.0,
                longitude = 200.0,
                radius = 50.0,
                radiusMeasurement = RadiusMeasurement.Meters,
                turnDNDOnUponEntering = true,
                turnDNDOffUponExiting = false,
            )

            every { mockSavedLocationsDAO.fetchLocations() } returns flowOf(listOf(savedLocation))

            service.onStartCommand(createIntent(false), 0, 0)
            val locationResult = LocationResult.create(listOf(savedLocation.androidLocation()))

            callbackSlot.captured.onLocationResult(locationResult)
            advanceUntilIdle()

            coVerify { mockSavedLocationsDAO.updateZoneStatus(zoneId, true, false) }
            verify { mockNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY) }
        }

    @Test
    fun `when no location wants DND-on during exit, turn DND off`() = runTest {
        val zoneId = 123L
        val savedLocation = SavedLocation(
            id = zoneId,
            fullAddress = "Test zone",
            latitude = 100.0,
            longitude = 200.0,
            radius = 50.0,
            radiusMeasurement = RadiusMeasurement.Meters,
            turnDNDOnUponEntering = false,
            turnDNDOffUponExiting = true,
            didEnter = true
        )
        every { mockSavedLocationsDAO.fetchLocations() } returns flowOf(listOf(savedLocation))
        service.onStartCommand(createIntent(false), 0, 0)

        val locationResult = LocationResult.create(listOf(Location("").apply {
            latitude = savedLocation.latitude + 25.0
            longitude = savedLocation.longitude
        }))
        callbackSlot.captured.onLocationResult(locationResult)
        advanceUntilIdle()

        coVerify { mockSavedLocationsDAO.updateZoneStatus(zoneId, false, true) }
        verify { mockNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL) }
    }

    @Test
    fun `exiting one zone does not turn DND off if another zone still wants DND-on`() = runTest {
        val zoneA = SavedLocation(
            id = 1L,
            fullAddress = "Zone A",
            longitude = 500.0,
            latitude = 600.0,
            radius = 30.0,
            radiusMeasurement = RadiusMeasurement.Meters,
            turnDNDOnUponEntering = true,
            turnDNDOffUponExiting = true,
            didEnter = true,
            didExit = false
        )
        val zoneB = SavedLocation(
            id = 2L,
            fullAddress = "Zone B",
            longitude = 700.0,
            latitude = 800.0,
            radius = 30.0,
            radiusMeasurement = RadiusMeasurement.Meters,
            turnDNDOnUponEntering = true,
            turnDNDOffUponExiting = false,
            didEnter = true,
            didExit = false
        )
        every { mockSavedLocationsDAO.fetchLocations() } returns flowOf(listOf(zoneA, zoneB))

        service.onStartCommand(createIntent(false), 0, 0)
        val spyLocation = spyk(Location("")).apply {
            every {
                distanceTo(match { loc -> loc.latitude == zoneA.latitude && loc.longitude == zoneA.longitude })
            } returns (zoneA.radius.toFloat() + LocationService.GEOFENCE_HYSTERESIS_METERS + 1) // considered as exit from zoneA
            every {
                distanceTo(match { loc -> loc.latitude == zoneB.latitude && loc.longitude == zoneB.longitude })
            } returns (zoneB.radius.toFloat() - LocationService.GEOFENCE_HYSTERESIS_METERS - 1) // still inside of zoneB
        }
        val locationResult = LocationResult.create(listOf(spyLocation))
        callbackSlot.captured.onLocationResult(locationResult)
        advanceUntilIdle()

        coVerify { mockSavedLocationsDAO.updateZoneStatus(zoneA.id , false, true) }
        verify (inverse = true) { mockNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL) }
    }

    @Test
    @Config(sdk = [31])
    fun `onStartCommand falls back to AlarmManager ang logs event if ForegroundServiceStartNotAllowedException gets thrown on Android S+`()
    = runTest {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

        mockkStatic(ServiceCompat::class)
        every {
            ServiceCompat.startForeground(
                any(),     // Match any Service instance
                any<Int>(),         // Match any Int ID
                any(), // Match any Notification
                any<Int>()          // Match any Int type
            )
        } throws ForegroundServiceStartNotAllowedException("")

        service.onStartCommand(null, 0, 0)

        verify {
            mockAlarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                any(),
                any<PendingIntent>()
            )
        }
    }

    private fun createIntent(highAccuracyMode: Boolean) = Intent().apply { putExtra(LocationService.HIGH_ACCURACY_MODE, highAccuracyMode) }
    private fun SavedLocation.androidLocation() = Location("").apply {
        latitude = this@androidLocation.latitude
        longitude = this@androidLocation.longitude
    }

}
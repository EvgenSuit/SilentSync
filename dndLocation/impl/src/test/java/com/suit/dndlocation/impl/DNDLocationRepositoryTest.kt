package com.suit.dndlocation.impl

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.suit.dndlocation.api.DNDLocationRepository
import com.suit.dndlocation.api.Feature
import com.suit.dndlocation.api.GeocodingManager
import com.suit.dndlocation.api.Geometry
import com.suit.dndlocation.api.LocationFeatureAvailabilityManager
import com.suit.dndlocation.api.Properties
import com.suit.dndlocation.api.RadiusMeasurement
import com.suit.dndlocation.api.RadiusValue
import com.suit.dndlocation.api.SavedLocation
import com.suit.dndlocation.impl.db.SavedLocationsDb
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class DNDLocationRepositoryTest {
    private lateinit var dndLocationRepository: DNDLocationRepository
    private lateinit var context: Context
    private lateinit var savedLocationsDb: SavedLocationsDb
    private val locationFeatureAvailabilityManager = mockk<LocationFeatureAvailabilityManager>(relaxed = true)
    private val geocodingManager = mockk<GeocodingManager>(relaxed = true)

    @Before
    fun setup() {
        context = spyk(ApplicationProvider.getApplicationContext<Context>())
        savedLocationsDb = Room.inMemoryDatabaseBuilder(
            context,
            SavedLocationsDb::class.java,
        ).build()
        dndLocationRepository = DNDLocationRepositoryImpl(
            context = context,
            savedLocationsDb = savedLocationsDb,
            locationFeatureAvailabilityManager = locationFeatureAvailabilityManager,
            geocodingManager = geocodingManager
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        savedLocationsDb.close()
    }

    @Test
    fun `if location exists, remove it`() = runTest {
        val savedLocation = SavedLocation(
            id = 1L,
            fullAddress = "Test zone",
            latitude = 100.0,
            longitude = 200.0,
            radius = 50.0,
            radiusMeasurement = RadiusMeasurement.Meters,
            turnDNDOnUponEntering = true,
            turnDNDOffUponExiting = true,
        )
        val savedLocation2 = savedLocation.copy(
            id = 2,
            fullAddress = "Test zone 2"
        )
        val dao = savedLocationsDb.savedLocationDao()
        dao.insertLocation(savedLocation)

        dndLocationRepository.confirmLocation(Feature(
            properties = Properties("123", savedLocation2.fullAddress),
            geometry = Geometry(coordinates = listOf(savedLocation2.longitude, savedLocation2.latitude)),
        ), true, true, RadiusValue(savedLocation2.radius.toInt(), savedLocation2.radiusMeasurement))

        assertEquals(null, dao.getLocation(savedLocation.id))
        assertEquals(savedLocation2, dao.getLocation(savedLocation2.id))
    }

    @Test
    fun `new service instance on toggle`() = runTest {
        dndLocationRepository.toggleLocationService(false, true)

        verify { context.stopService(any()) }
        verify { context.startForegroundService(any()) }
    }
}
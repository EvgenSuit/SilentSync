package com.suit.dndlocation.impl

import com.suit.dndlocation.api.RadiusMeasurement
import com.suit.dndlocation.api.SavedLocation
import junit.framework.TestCase.assertEquals
import org.junit.Test

class SavedLocationTest {
    @Test
    fun `radiusValueMeters returns same value when measurement is Meters`() {
        val radiusMeters = 50.0
        val location = SavedLocation(
            id = 1L,
            fullAddress = "123 Main St",
            longitude = 0.0,
            latitude = 0.0,
            radius = radiusMeters,
            radiusMeasurement = RadiusMeasurement.Meters,
            turnDNDOffUponExiting = false,
            turnDNDOnUponEntering = false
        )
        assertEquals(radiusMeters, location.radiusValueMeters())
    }

    @Test
    fun `radiusValueMeters converts Yards to Meters`() {
        val radiusYards = 35.0
        val expectedMeters = radiusYards * 0.9144
        val location = SavedLocation(
            id = 1L,
            fullAddress = "456 Oak Ave",
            longitude = 1.0,
            latitude = 1.0,
            radius = radiusYards,
            radiusMeasurement = RadiusMeasurement.Yards,
            turnDNDOnUponEntering = false,
            turnDNDOffUponExiting = false
        )
        assertEquals(expectedMeters, location.radiusValueMeters())
    }
}
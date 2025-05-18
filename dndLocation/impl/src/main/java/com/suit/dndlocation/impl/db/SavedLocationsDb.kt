package com.suit.dndlocation.impl.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.suit.dndlocation.api.RadiusMeasurementConverter
import com.suit.dndlocation.api.SavedLocation

@Database(entities = [SavedLocation::class], version = 1)
@TypeConverters(RadiusMeasurementConverter::class)
abstract class SavedLocationsDb: RoomDatabase() {
    abstract fun savedLocationDao(): SavedLocationsDAO
}
package com.suit.dndlocation.impl.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.suit.dndlocation.api.SavedLocation

@Database(entities = [SavedLocation::class], version = 1)
abstract class SavedLocationsDb: RoomDatabase() {
    abstract fun savedLocationDao(): SavedLocationsDAO
}
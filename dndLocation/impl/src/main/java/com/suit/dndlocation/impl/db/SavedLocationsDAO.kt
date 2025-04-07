package com.suit.dndlocation.impl.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.suit.dndlocation.api.SavedLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedLocationsDAO {
    @Insert(onConflict = REPLACE)
    suspend fun insertLocation(savedLocation: SavedLocation)

    @Query("SELECT * FROM SavedLocation")
    fun fetchLocations(): Flow<List<SavedLocation>>

    @Query("UPDATE SavedLocation SET didEnter = :didEnter, didExit = :didExit WHERE mapBoxId = :id")
    suspend fun updateZoneStatus(id: String, didEnter: Boolean, didExit: Boolean)
}
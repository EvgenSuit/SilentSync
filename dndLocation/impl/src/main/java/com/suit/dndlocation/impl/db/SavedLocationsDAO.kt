package com.suit.dndlocation.impl.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.suit.dndlocation.api.SavedLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedLocationsDAO {
    @Insert(onConflict = REPLACE)
    suspend fun insertLocation(savedLocation: SavedLocation)

    @Query("DELETE FROM SavedLocation")
    suspend fun deleteLocations()

    @Query("DELETE FROM SavedLocation WHERE id = :id")
    suspend fun deleteLocation(id: Long)
    @Query("SELECT id FROM SavedLocation WHERE longitude = :longitude AND latitude = :latitude LIMIT 1")
    suspend fun getLocationId(longitude: Double, latitude: Double): Long?
    @Query("SELECT * FROM SavedLocation WHERE id = :id")
    suspend fun getLocation(id: Long): SavedLocation?


    @Query("SELECT * FROM SavedLocation")
    fun fetchLocations(): Flow<List<SavedLocation>>

    @Query("SELECT EXISTS(SELECT 1 FROM SavedLocation WHERE longitude = :longitude AND latitude = :latitude)")
    suspend fun locationExists(longitude: Double, latitude: Double): Boolean

    @Query("UPDATE SavedLocation SET didEnter = :didEnter, didExit = :didExit WHERE id = :id")
    suspend fun updateZoneStatus(id: Long, didEnter: Boolean, didExit: Boolean)

    @Query("UPDATE SavedLocation SET didEnter = 0, didExit = 0")
    suspend fun resetAllZoneStatuses()
}
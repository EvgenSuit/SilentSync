package com.suit.dndlocation.impl.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.suit.dndlocation.api.SavedLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedLocationsDAO {
    @Insert
    suspend fun insertLocation(savedLocation: SavedLocation)

    @Query("SELECT * FROM SavedLocation")
    suspend fun fetchLocations(): Flow<List<SavedLocation>>
}
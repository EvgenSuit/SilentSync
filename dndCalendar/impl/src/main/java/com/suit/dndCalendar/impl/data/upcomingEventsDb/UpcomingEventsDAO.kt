package com.suit.dndCalendar.impl.data.upcomingEventsDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suit.dndcalendar.api.UpcomingEventData
import kotlinx.coroutines.flow.Flow

@Dao
internal interface UpcomingEventsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: UpcomingEventData)

    @Query("SELECT * FROM UpcomingEventData")
    fun getUpcomingEvents(): Flow<List<UpcomingEventData>>

    @Query("UPDATE UpcomingEventData SET scheduleDndOn = :set WHERE id = :id")
    suspend fun updateDndOnToggle(id: Long, set: Boolean)
    @Query("UPDATE UpcomingEventData SET scheduleDndOff = :set WHERE id = :id")
    suspend fun updateDndOffToggle(id: Long, set: Boolean)

    @Query("DELETE FROM UpcomingEventData WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM UpcomingEventData")
    suspend fun deleteAll()
}
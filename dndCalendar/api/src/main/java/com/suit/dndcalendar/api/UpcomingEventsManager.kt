package com.suit.dndcalendar.api

import kotlinx.coroutines.flow.Flow

interface UpcomingEventsManager {
    suspend fun insert(event: UpcomingEventData)

    suspend fun setDndOnToggle(id: Long, startTime: Long)
    suspend fun removeDndOnToggle(id: Long)

    suspend fun setDndOffToggle(id: Long, endTime: Long)
    suspend fun removeDndOffToggle(id: Long)

    suspend fun removeUpcomingEvent(id: Long)
    suspend fun deleteAllEvents()
    fun upcomingEventsFlow(): Flow<List<UpcomingEventData>>
}
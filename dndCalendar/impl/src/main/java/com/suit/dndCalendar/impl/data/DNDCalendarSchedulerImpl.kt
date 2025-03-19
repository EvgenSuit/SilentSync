package com.suit.dndCalendar.impl.data

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import com.suit.dndcalendar.api.DNDCalendarScheduler
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteriaManager
import com.suit.dndcalendar.api.UpcomingEventData
import com.suit.dndcalendar.api.UpcomingEventsManager
import com.suit.utility.NoCalendarCriteriaFound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toCollection
import java.text.SimpleDateFormat
import java.time.Clock

internal class DNDCalendarSchedulerImpl(
    private val context: Context,
    private val clock: Clock,
    private val upcomingEventsManager: UpcomingEventsManager,
    private val dndScheduleCalendarCriteriaManager: DNDScheduleCalendarCriteriaManager
): DNDCalendarScheduler {

    override suspend fun schedule() {
        val upcomingEvents = upcomingEventsManager.upcomingEventsFlow().firstOrNull()
        val criteria = dndScheduleCalendarCriteriaManager.getCriteria().firstOrNull()

        if (criteria == null || criteria.isEmpty()) {
            if (!upcomingEvents.isNullOrEmpty()) {
                upcomingEvents.forEach { upcomingEventsManager.removeUpcomingEvent(it.id) }
                return
            }
            throw NoCalendarCriteriaFound("No criteria for calendar-based dnd toggle was given")
        }

        val likeNames = criteria.likeNames
        val attendees = criteria.attendees
        val likeNameEvents = getLikeNameEvents(likeNames).toCollection(mutableListOf())
        val likeAttendeesEvents = getAttendeeData(attendees).toCollection(mutableListOf())

        val currUpcomingEventIds = mutableListOf<Long>()
        (likeNameEvents.map { it.eventId } + likeAttendeesEvents.map { it.eventId })
            .toSet().forEach { eventId ->
                val basicEventData = getBasicEventData(eventId)
                // for some reason, when an event with matching attendee criteria changes, a second attendee event gets
                // detected by getAttendeeData, with id being incremented from time to time, like [AttendeeData(eventId=496, attendeeName=Name), AttendeeData(eventId=501, attendeeName=Name)]
                // so below check makes sure that event exists
                if (basicEventData != null) {
                    val attendees = likeAttendeesEvents.filter { it.eventId == eventId }
                    val calendarEvent = CalendarEventData(
                        id = eventId,
                        title = basicEventData.title,
                        startTime = basicEventData.startTime,
                        endTime = basicEventData.endTime,
                        attendees = attendees.map { it.attendeeName },
                        deleted = basicEventData.deleted)
                    currUpcomingEventIds.add(eventId)

                    // checks if current event's end time is equal to another event's start time
                    // if true, don't schedule dnd off for current event
                    val doesOverlap = doesOverlap(endTime = basicEventData.endTime)
                    scheduleAlarms(
                        event = calendarEvent,
                        endTimeOverlaps = doesOverlap,
                        onGetSavedUpcomingEventData = { upcomingEvents?.firstOrNull { it.id == eventId }} )
                }
            }
        removeEventsNotMatchingCriteria(
            savedUpcomingEventIds = upcomingEvents?.map { it.id }?.toSet(),
            currUpcomingEventIds = currUpcomingEventIds.toSet())
    }

    private suspend fun getBasicEventData(
        eventId: Long
    ): BasicEventData? {
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.DELETED
        )
        val cursor: Cursor? = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            "${CalendarContract.Events._ID} = ?",
            arrayOf(eventId.toString()),
            null
        )
        var basicEventData: BasicEventData? = null
        cursor.useCursor {
            val id = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
            val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
            val startTime =
                it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
            val endTime =
                it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
            val deleted =
                it.getInt(it.getColumnIndexOrThrow(CalendarContract.Events.DELETED))
            basicEventData = BasicEventData(
                id = id,
                title = title,
                startTime = startTime,
                endTime = endTime,
                deleted = deleted == 1
            )
        }
        return basicEventData
    }

    private data class BasicEventData(
        val id: Long,
        val title: String,
        val startTime: Long,
        val endTime: Long,
        val deleted: Boolean = false
    )

    private fun getLikeNameEvents(
        likeNames: List<String>
    ) = flow {
        if (likeNames.isEmpty()) return@flow
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE
        )
        val cursor = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            likeNames.joinToString(" OR ") { "${CalendarContract.Events.TITLE} LIKE ?" },
            likeNames.map { "%$it%" }.toTypedArray(),
            null
        )
        cursor.useCursor {
            val eventId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
            val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
            emit(
                LikeNameEventData(
                    eventId = eventId,
                    name = title
                )
            )
        }
    }
    private data class LikeNameEventData(
        val eventId: Long,
        val name: String
    )

    private fun getAttendeeData(
        attendees: List<String>
    ): Flow<AttendeeData> = flow {
        if (attendees.isEmpty()) return@flow
        val projection = arrayOf(
            CalendarContract.Attendees.EVENT_ID,
            CalendarContract.Attendees.ATTENDEE_NAME
        )
        val cursor = context.contentResolver.query(
            CalendarContract.Attendees.CONTENT_URI,
            projection,
            attendees.joinToString(" OR ") { "${CalendarContract.Attendees.ATTENDEE_NAME} LIKE ?" },
            attendees.map { "%$it%" }.toTypedArray(),
            null
        )
        cursor.useCursor {
            val eventId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Attendees.EVENT_ID))
            val name = it.getString(it.getColumnIndexOrThrow(CalendarContract.Attendees.ATTENDEE_NAME))
            emit(
                AttendeeData(
                    eventId = eventId,
                    attendeeName = name
                )
            )
        }
    }
    private data class AttendeeData(
        val eventId: Long,
        val attendeeName: String
    )

    @SuppressLint("ScheduleExactAlarm")
    private suspend fun scheduleAlarms(event: CalendarEventData,
                                       endTimeOverlaps: Boolean,
                                       onGetSavedUpcomingEventData: () -> UpcomingEventData?) {
        when (event.deleted || event.endTime < clock.millis()) {
            true -> {
                upcomingEventsManager.removeUpcomingEvent(event.id)
                Log.d(
                    "EventScheduler",
                    "Removed dnd toggle: $event"
                )
            }
            false -> {
                upcomingEventsManager.apply {
                    // if dnd toggle is already scheduled, make sure that dnd on and off options are not set to default
                    val upcomingEvent = onGetSavedUpcomingEventData()?.copy(
                        id = event.id,
                        title = event.title,
                        startTime = event.startTime,
                        endTime = event.endTime
                    ) ?: event.toUpcomingEvent()
                    insert(upcomingEvent)
                    if (upcomingEvent.scheduleDndOn) setDndOnToggle(event.id, event.startTime)
                    if (upcomingEvent.scheduleDndOff) {
                        if (!endTimeOverlaps) setDndOffToggle(event.id, event.endTime)
                        else removeDndOffToggle(event.id)
                    }
                }

                println("Scheduled DND ON for ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.startTime)} " +
                        "and DND OFF for ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.endTime)}.")
                Log.d(
                    "EventScheduler",
                    "Scheduled DND ON for ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.startTime)} " +
                            "and DND OFF for ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.endTime)}."
                )
            }
        }
    }

    private suspend fun removeEventsNotMatchingCriteria(
        savedUpcomingEventIds: Set<Long>?,
        currUpcomingEventIds: Set<Long>
    ) {
        // remove upcoming events that no longer match given criteria during syncing
        if (savedUpcomingEventIds != null) {
            val diff = savedUpcomingEventIds - currUpcomingEventIds
            diff.forEach {
                upcomingEventsManager.removeUpcomingEvent(it)
            }
        }
    }

    // checks if current event's end time is equal to another event's start time
    // returns true if no overlapping happens
    private fun doesOverlap(endTime: Long): Boolean {
        val projection = arrayOf(
            CalendarContract.Events.DTSTART
        )
        val cursor = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            "${CalendarContract.Events.DTSTART} = ?",
            arrayOf(endTime.toString()),
            null
        )
        return cursor?.use {
            it.count != 0
        } ?: false
    }

    private suspend fun Cursor?.useCursor(block: suspend (Cursor) -> Unit) =
        this?.use {
            if (it.count != 0) {
                while (it.moveToNext()) block(it)
            }
        }
}

enum class DNDActionType {
    DND_ON, DND_OFF
}
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
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.time.Clock

internal class DNDCalendarSchedulerImpl(
    private val context: Context,
    private val clock: Clock,
    private val upcomingEventsManager: UpcomingEventsManager,
    private val dndScheduleCalendarCriteriaManager: DNDScheduleCalendarCriteriaManager
): DNDCalendarScheduler {

    override suspend fun schedule() {
        val criteria = dndScheduleCalendarCriteriaManager.getCriteria().firstOrNull() ?:
            throw NoCalendarCriteriaFound("No criteria for calendar-based dnd toggle was given")

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.DELETED
        )
        println("Executing EventScheduler")
        val cursor: Cursor? = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            "${CalendarContract.Events.TITLE} LIKE ?",
            arrayOf("%${criteria.likeName}%"),
            null
        )
        Log.d(
            "EventScheduler",
            "Executing EventScheduler"
        )
        val upcomingEvents = upcomingEventsManager.upcomingEventsFlow().firstOrNull()
        cursor?.use {
            if (it.count != 0) {
                while (it.moveToNext()) {
                    val eventId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
                    val title =
                        it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                    val startTime =
                        it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                    val endTime =
                        it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
                    val deleted =
                        it.getInt(it.getColumnIndexOrThrow(CalendarContract.Events.DELETED))
                    val event = CalendarEventData(eventId, title, startTime, endTime, deleted == 1)
                    scheduleAlarms(event,
                        onGetSavedUpcomingEventData = { upcomingEvents?.firstOrNull { it.id == eventId }} )

                    println("Event ID: $eventId, Title: $title, Start: $startTime, End: $endTime, Is deleted: $deleted")
                    Log.d(
                        "EventScheduler",
                        "Event ID: $eventId, Title: $title, Start: $startTime, End: $endTime, Is deleted: $deleted"
                    )
                }
            } else {
                Log.d("EventScheduler", "No calendar events found.")
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private suspend fun scheduleAlarms(event: CalendarEventData,
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
                    println(upcomingEvent)
                    insert(upcomingEvent)
                    if (upcomingEvent.scheduleDndOn) setDndOnToggle(event.id, event.startTime)
                    if (upcomingEvent.scheduleDndOff) setDndOffToggle(event.id, event.endTime)
                }

                println("Scheduled DND ON at ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.startTime)} " +
                        "and DND OFF at ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.endTime)}.")
                Log.d(
                    "EventScheduler",
                    "Scheduled DND ON at ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.startTime)} " +
                            "and DND OFF at ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.endTime)}."
                )
            }
        }
    }
}

enum class DNDActionType {
    DND_ON, DND_OFF
}
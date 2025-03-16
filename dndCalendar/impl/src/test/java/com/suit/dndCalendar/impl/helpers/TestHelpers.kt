package com.suit.dndCalendar.impl.helpers

import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.provider.CalendarContract
import com.suit.dndCalendar.impl.data.CalendarEventData
import org.robolectric.Robolectric
import org.robolectric.android.controller.ContentProviderController

class TestHelpers {
    companion object {
        fun buildProvider(): ContentProviderController<EventCalendarProvider> {
            val providerInfo = ProviderInfo().apply {
                authority = CalendarContract.AUTHORITY
                grantUriPermissions = true
            }
            return Robolectric.buildContentProvider(EventCalendarProvider::class.java).create(providerInfo)
        }

        fun insertCalendarData(context: Context, eventData: CalendarEventData) {
            context.contentResolver.apply {
                insert(CalendarContract.Events.CONTENT_URI, ContentValues().apply {
                    put(CalendarContract.Events._ID, eventData.id)
                    put(CalendarContract.Events.TITLE, eventData.title)
                    put(CalendarContract.Events.DTSTART, eventData.startTime)
                    put(CalendarContract.Events.DTEND, eventData.endTime)
                    put(CalendarContract.Events.DELETED, eventData.deleted)
                })
                insert(CalendarContract.Attendees.CONTENT_URI, ContentValues().apply {
                    put(CalendarContract.Attendees.EVENT_ID, eventData.id)
                    eventData.attendees.forEach { put(CalendarContract.Attendees.ATTENDEE_NAME, it) }
                })
            }

        }
    }
}
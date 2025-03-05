package com.suit.silentsync.helpers

import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.provider.CalendarContract
import com.suit.silentsync.data.CalendarEventData
import org.robolectric.Robolectric
import org.robolectric.android.controller.ContentProviderController

class TestHelpers {
    companion object {
        fun buildProvider(): ContentProviderController<SilentSyncCalendarProvider> {
            val providerInfo = ProviderInfo().apply {
                authority = CalendarContract.AUTHORITY
                grantUriPermissions = true
            }
            return Robolectric.buildContentProvider(SilentSyncCalendarProvider::class.java).create(providerInfo)
        }

        fun insert(context: Context, eventData: CalendarEventData) {
            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, ContentValues().apply {
                put(CalendarContract.Events._ID, eventData.id)
                put(CalendarContract.Events.TITLE, eventData.title)
                put(CalendarContract.Events.DTSTART, eventData.startTime)
                put(CalendarContract.Events.DTEND, eventData.endTime)
                put(CalendarContract.Events.DELETED, eventData.deleted)
            })
        }
    }
}
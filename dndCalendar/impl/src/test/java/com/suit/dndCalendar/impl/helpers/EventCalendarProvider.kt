package com.suit.dndCalendar.impl.helpers

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.CalendarContract

private const val EVENTS = 1
private const val ATTENDEES = 2

private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
    addURI(CalendarContract.AUTHORITY, "events", EVENTS)
    addURI(CalendarContract.AUTHORITY, "attendees", ATTENDEES)
}

class EventCalendarProvider: ContentProvider() {
    private lateinit var calendarDBHelper: CalendarDBHelper
    private lateinit var database: SQLiteDatabase

    override fun onCreate(): Boolean {
        val tContext = context
        if (tContext == null) {
            println("Context is null")
            return false
        }
        calendarDBHelper = CalendarDBHelper(tContext)
        database = calendarDBHelper.writableDatabase
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val queryBuilder = SQLiteQueryBuilder()
        when (uriMatcher.match(uri)) {
            EVENTS -> queryBuilder.tables = CalendarEventEntry.TABLE_NAME
            ATTENDEES -> queryBuilder.tables = CalendarAttendeeEntry.TABLE_NAME
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        val cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder)
        cursor.setNotificationUri(context?.contentResolver, uri) // not sure what this is for
        return cursor
    }

    override fun getType(uri: Uri): String? {
        TODO("Not yet implemented")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val table = when (uriMatcher.match(uri)) {
            EVENTS -> CalendarEventEntry.TABLE_NAME
            ATTENDEES -> CalendarAttendeeEntry.TABLE_NAME
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        val rowId = database.insert(table, "", values)
        if (rowId > 0) {
            val uriWithAppendedId = ContentUris.withAppendedId(uri, rowId)
            context?.contentResolver?.notifyChange(uriWithAppendedId, null)
            return uriWithAppendedId
        }
        throw SQLiteException("Failed to insert a record")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        //return database.delete()
        TODO("Not yet implemented")
    }

    override fun shutdown() {
        super.shutdown()
        calendarDBHelper.close()
        database.close()
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return database.update(CalendarEventEntry.TABLE_NAME, values, selection, selectionArgs)
    }
}
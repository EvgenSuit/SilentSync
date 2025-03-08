package com.suit.dndCalendar.impl.helpers

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.CalendarContract

class SilentSyncCalendarProvider: ContentProvider() {
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
        queryBuilder.tables = CalendarEntry.TABLE_NAME
        val cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder)
        cursor.setNotificationUri(context?.contentResolver, uri) // not sure what this is for
        return cursor
    }

    override fun getType(uri: Uri): String? {
        TODO("Not yet implemented")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val rowId = database.insert(CalendarEntry.TABLE_NAME, "", values)
        println(rowId)
        if (rowId > 0) {
            val uriWithAppendedId = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, rowId)
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
        return database.update(CalendarEntry.TABLE_NAME, values, selection, selectionArgs)
    }
}
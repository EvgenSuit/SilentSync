package com.suit.dndCalendar.impl.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.provider.CalendarContract


object CalendarEventEntry: BaseColumns {
    const val TABLE_NAME = "events"
    const val TITLE = CalendarContract.Events.TITLE
    const val DTSTART = CalendarContract.Events.DTSTART
    const val DTEND = CalendarContract.Events.DTEND
    const val DELETED = CalendarContract.Events.DELETED
}
object CalendarAttendeeEntry: BaseColumns {
    const val EVENT_ID = CalendarContract.Attendees.EVENT_ID
    const val TABLE_NAME = "attendees"
    const val ATTENDEE_NAME = CalendarContract.Attendees.ATTENDEE_NAME
}

class CalendarDBHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.apply {
            execSQL(SQL_CREATE_EVENT_ENTRIES)
            execSQL(SQL_CREATE_ATTENDEE_ENTRIES)
        }
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.apply {
            execSQL(SQL_DELETE_EVENT_ENTRIES)
            execSQL(SQL_DELETE_ATTENDEE_ENTRIES)
        }
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "CalendarHelper.db"
        private const val SQL_DELETE_EVENT_ENTRIES = "DROP TABLE IF EXISTS ${CalendarEventEntry.TABLE_NAME}"
        private const val SQL_CREATE_EVENT_ENTRIES =
            "CREATE TABLE ${CalendarEventEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${CalendarEventEntry.TITLE} TEXT," +
                    "${CalendarEventEntry.DTSTART} BIGINT," +
                    "${CalendarEventEntry.DTEND} BIGINT," +
                    "${CalendarEventEntry.DELETED} INTEGER DEFAULT 0)"

        private const val SQL_CREATE_ATTENDEE_ENTRIES =
            "CREATE TABLE ${CalendarAttendeeEntry.TABLE_NAME} (" +
                    "${CalendarAttendeeEntry.EVENT_ID} INTEGER PRIMARY KEY, " +
                    "${CalendarAttendeeEntry.ATTENDEE_NAME} TEXT" +
                    ")"
        private const val SQL_DELETE_ATTENDEE_ENTRIES = "DROP TABLE IF EXISTS ${CalendarAttendeeEntry.TABLE_NAME}"
    }
}
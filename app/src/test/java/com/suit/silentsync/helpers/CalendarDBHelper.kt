package com.suit.silentsync.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.provider.CalendarContract


object CalendarEntry: BaseColumns {
    const val TABLE_NAME = "events"
    const val TITLE = CalendarContract.Events.TITLE
    const val DTSTART = CalendarContract.Events.DTSTART
    const val DTEND = CalendarContract.Events.DTEND
    const val DELETED = CalendarContract.Events.DELETED
}

class CalendarDBHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${CalendarEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${CalendarEntry.TITLE} TEXT," +
                "${CalendarEntry.DTSTART} BIGINT," +
                "${CalendarEntry.DTEND} BIGINT," +
                "${CalendarEntry.DELETED} INTEGER DEFAULT 0)"
    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${CalendarEntry.TABLE_NAME}"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "CalendarHelper.db"
    }
}
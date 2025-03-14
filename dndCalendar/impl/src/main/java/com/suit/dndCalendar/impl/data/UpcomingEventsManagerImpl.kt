package com.suit.dndCalendar.impl.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.suit.dndCalendar.impl.data.upcomingEventsDb.UpcomingEventsDb
import com.suit.dndCalendar.impl.receivers.DNDReceiver
import com.suit.dndcalendar.api.UpcomingEventData
import com.suit.dndcalendar.api.UpcomingEventsManager

internal class UpcomingEventsManagerImpl(
    private val context: Context,
    private val db: UpcomingEventsDb
): UpcomingEventsManager {
    override suspend fun insert(event: UpcomingEventData) {
        db.dao().insert(event)
    }

    override suspend fun setDndOnToggle(id: Long, startTime: Long) {
        val pendingIntent = constructPendingIntent(id, dndOn = true)
        context.getSystemService(AlarmManager::class.java)
            .setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                startTime,
                pendingIntent
            )
        db.dao().updateDndOnToggle(id, set = true)
    }
    override suspend fun removeDndOnToggle(id: Long) {
        val pendingIntent = constructPendingIntent(id, dndOn = true)
        context.getSystemService(AlarmManager::class.java)
            .cancel(pendingIntent)
        db.dao().updateDndOnToggle(id, set = false)
    }

    override suspend fun setDndOffToggle(id: Long, endTime: Long) {
        val pendingIntent = constructPendingIntent(id, dndOn = false)
        context.getSystemService(AlarmManager::class.java)
            .setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                endTime,
                pendingIntent
            )
        db.dao().updateDndOffToggle(id, set = true)
    }
    override suspend fun removeDndOffToggle(id: Long) {
        db.dao().updateDndOffToggle(id, set = false)
    }

    override suspend fun removeUpcomingEvent(id: Long) {
        val dndOnPendingIntent = constructPendingIntent(id, dndOn = true)
        val dndOffPendingIntent = constructPendingIntent(id, dndOn = false)
        context.getSystemService(AlarmManager::class.java).apply {
            cancel(dndOnPendingIntent)
            cancel(dndOffPendingIntent)
        }
        db.dao().delete(id)
    }

    override suspend fun deleteAllEvents() {
        db.dao().deleteAll()
    }

    override fun upcomingEventsFlow() = db.dao().getUpcomingEvents()

    private fun constructPendingIntent(id: Long, dndOn: Boolean): PendingIntent {
        val intent = Intent(context, DNDReceiver::class.java).apply {
            putExtra("action", (if (dndOn) DNDActionType.DND_ON else DNDActionType.DND_OFF).name)
            putExtra("eventId", id)
        }
        return PendingIntent.getBroadcast(
            context, (id*2+(if (dndOn) 0 else 1)).toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
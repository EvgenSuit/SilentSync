package com.suit.dndCalendar.impl.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.suit.dndCalendar.impl.data.DNDActionType
import com.suit.dndcalendar.api.CalendarEventChecker
import com.suit.dndcalendar.api.UpcomingEventsManager
import com.suit.utility.analytics.SilentSyncAnalytics
import com.suit.utility.analytics.SilentSyncEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class DNDReceiver: BroadcastReceiver(), KoinComponent {
    private val calendarEventChecker: CalendarEventChecker by inject()
    private val scope: CoroutineScope by inject()
    private val upcomingEventsManager: UpcomingEventsManager by inject()
    private val silentSyncAnalytics: SilentSyncAnalytics by inject()

    // might get called multiple times
    override fun onReceive(context: Context?, intent: Intent?) {
        scope.launch {
            try {
                val action = intent!!.getStringExtra("action")!!
                val eventId = intent.getLongExtra("eventId", 0L)

                if (calendarEventChecker.doesEventExist(eventId)) {
                    val notificationManager =
                        context!!.getSystemService(NotificationManager::class.java)
                    val isDndOn =
                        notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY
                    when (action) {
                        DNDActionType.DND_ON.name -> {
                            // make sure dnd isn't turned on again
                            if (!isDndOn && calendarEventChecker.doTurnDNDon(eventId)) {
                                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                                silentSyncAnalytics.logEvent(SilentSyncEvent.DND_ON)
                            }
                        }

                        DNDActionType.DND_OFF.name -> {
                            val upcomingEvent = upcomingEventsManager.upcomingEventsFlow().firstOrNull()
                                ?.firstOrNull { it.id == eventId }
                            if (isDndOn && upcomingEvent != null) {
                                upcomingEventsManager.removeUpcomingEvent(eventId)
                                if (upcomingEvent.scheduleDndOff) {
                                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                                    silentSyncAnalytics.logEvent(SilentSyncEvent.DND_OFF)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                silentSyncAnalytics.recordException(e)
            }
        }
    }
}
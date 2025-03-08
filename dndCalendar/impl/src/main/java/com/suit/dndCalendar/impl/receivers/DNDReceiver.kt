package com.suit.dndCalendar.impl.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.suit.dndCalendar.impl.data.DNDActionType
import com.suit.dndcalendar.api.CalendarEventChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class DNDReceiver: BroadcastReceiver(), KoinComponent {
    private val calendarEventChecker: CalendarEventChecker by inject()
    private val scope: CoroutineScope by inject()

    // might get called multiple times
    override fun onReceive(context: Context?, intent: Intent?) {
        scope.launch {
            try {
                val action = intent!!.getStringExtra("action")!!
                val eventId = intent.getLongExtra("eventId", 0L)

                println("Action: $action, eventId: $eventId")
                Log.d("EventScheduler", "Action: $action, eventId: $eventId")
                if (calendarEventChecker.doesEventExist(eventId)) {
                    val notificationManager =
                        context!!.getSystemService(NotificationManager::class.java)
                    val isDndOn =
                        notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY
                    when (action) {
                        DNDActionType.DND_ON.name -> {
                            // make sure dnd isn't turned on again
                            if (!isDndOn && calendarEventChecker.doTurnDNDon(eventId)) {
                                println("turning dnd on")
                                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                            }
                        }

                        DNDActionType.DND_OFF.name -> {
                            // turn dnd off only if it's on
                            if (isDndOn) {
                                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("DNDReceiver exception: $e")
                Log.d("EventScheduler", "DNDReceiver exception: $e")
            }
        }
    }
}
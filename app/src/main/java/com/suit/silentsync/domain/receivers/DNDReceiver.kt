package com.suit.silentsync.domain.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.suit.silentsync.domain.DNDActionType
import com.suit.silentsync.domain.EventChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DNDReceiver: BroadcastReceiver(), KoinComponent {
    private val eventChecker: EventChecker by inject()
    private val scope: CoroutineScope by inject()

    // might get called multiple times
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            val action = intent!!.getStringExtra("action")!!
            val eventId = intent.getLongExtra("eventId", 0L)

            println("Action: $action, eventId: $eventId")
            Log.d("EventScheduler", "Action: $action, eventId: $eventId")
            scope.launch {
                if (eventChecker.doesEventExist(eventId)) {
                    val notificationManager = context!!.getSystemService(NotificationManager::class.java)
                    val isDndOn = notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY
                    when (action) {
                        DNDActionType.DND_ON.name -> {
                            // make sure dnd isn't turned on again
                            if (!isDndOn && eventChecker.doTurnDNDon(eventId)) {
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
            }
        } catch (e: Exception) {
            println("DNDReceiver exception: $e")
            Log.d("EventScheduler", "DNDReceiver exception: $e")
        }
    }
}
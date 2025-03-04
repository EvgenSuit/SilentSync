package com.suit.silentsync

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Clock

class DNDReceiver: BroadcastReceiver(), KoinComponent {
    private val eventChecker: EventChecker by inject()
    private val clock: Clock by inject()

    // might get called multiple times
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            val action = intent!!.getStringExtra("action")!!
            val eventId = intent.getLongExtra("eventId", 0L)

            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                if (eventChecker.doesEventExist(eventId)) {
                    val notificationManager = context!!.getSystemService(NotificationManager::class.java)
                    val isDndOn = notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY
                    when (action) {
                        DNDActionType.DND_ON.name ->
                            // make sure dnd isn't turned on again
                            if (!isDndOn && eventChecker.doTurnDNDon(eventId, clock.millis())) {
                                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                            }

                        DNDActionType.DND_OFF.name -> {
                            // turn dnd off only if it's on
                            if (isDndOn && eventChecker.doTurnDNDoff(eventId, clock.millis())) {
                                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                            }
                        }
                    }
                }
                Log.d("CalendarReceiver", "Action: $action, eventId: $eventId")
            }
        } catch (e: Exception) {
            Log.d("CalendarReceiver", "DNDReceiver exception: $e")
        }
    }
}
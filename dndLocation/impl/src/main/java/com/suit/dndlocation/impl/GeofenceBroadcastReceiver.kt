package com.suit.dndlocation.impl

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.suit.dndlocation.impl.db.SavedLocationsDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.any
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeofenceBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val savedLocationsDb by inject<SavedLocationsDb>()
    private val coroutineScope by inject<CoroutineScope>()
    private val notificationManager by inject<NotificationManager>()

    override fun onReceive(ctx: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return

        val transition = geofencingEvent.geofenceTransition
        val dao = savedLocationsDb.savedLocationDao()
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            triggeringGeofences?.forEach { geo ->
                val id = geo.requestId
                coroutineScope.launch {
                    val allLocations = dao.fetchLocations().first()
                    val location = dao.getLocation(id.toLong())
                    when (geofencingEvent.geofenceTransition) {
                        Geofence.GEOFENCE_TRANSITION_ENTER -> {
                            if (location?.turnDNDOnUponEntering == true) {
                                notificationManager.setInterruptionFilter(
                                    NotificationManager.INTERRUPTION_FILTER_PRIORITY
                                )
                            }
                            dao.updateZoneStatus(id.toLong(), true, false)
                        }
                        Geofence.GEOFENCE_TRANSITION_EXIT -> {
                            // don't turn DND off when already located inside of another zone after exiting the other one (overlapping)
                            // as exit events could be detected after an enter event
                            if (location?.turnDNDOffUponExiting == true && allLocations.count { it.didEnter } == 1) {
                                notificationManager.setInterruptionFilter(
                                    NotificationManager.INTERRUPTION_FILTER_ALL
                                )
                            }
                            dao.updateZoneStatus(id.toLong(), false, true)
                        }
                    }
                }
            }
        }
    }
}

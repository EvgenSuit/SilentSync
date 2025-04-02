package com.suit.dndlocation.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        println("Event: ${GeofencingEvent.fromIntent(intent)}")
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) {
            println("Geofencing event error: ${GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            println("Triggering geofences: ${triggeringGeofences}")
        } else {
            println("Invalid geofencing type: $geofenceTransition")
        }
    }
}
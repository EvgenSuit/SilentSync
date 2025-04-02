package com.suit.dndlocation.impl

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.common.internal.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService: Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun buildChannel() {
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_NONE
                )
            )
    }

    private fun buildNotification(): Notification {
        buildChannel()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            //.setSmallIcon(R.drawable.ic_launcher)
            .build();
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        ServiceCompat.startForeground(
            this,
            1,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        )
        val geofencePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        geofencingClient = LocationServices.getGeofencingClient(this@LocationService)
        /*geofencingClient.addGeofences(
            GeofencingRequest.Builder().apply {
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL)
                addGeofence(
                    Geofence.Builder()
                        .setRequestId("1")
                        .setCircularRegion(
                            51.0878733,
                            17.0120722,
                            1_000f // meters
                        )
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build()
                )
            }.build(),
            geofencePendingIntent
        ).run {
            addOnFailureListener { println("GEOFENCING ERROR: $it") }
            addOnSuccessListener { println("Added geofencing") }
        } */

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            LocationRequest.Builder(LOCATION_UPDATE_INTERVAL_MILLIS)
                .setIntervalMillis(LOCATION_UPDATE_INTERVAL_MILLIS)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build(),
            object: LocationCallback() {
                @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                override fun onLocationResult(locationResult: LocationResult) {
                    val lastLocation = locationResult.lastLocation ?: return

                    println("Result: ${lastLocation.let { "Lat: ${it.latitude}, Long: ${it.longitude}" }}")

                    val targetLatitude = 51.08793 //51.0878733
                    val targetLongitude = 17.011963 //17.0120722
                    val targetLocation = Location("").apply {
                        latitude = targetLatitude
                        longitude = targetLongitude
                    }

                    // if location is within a specified radius, add it to a list
                    // then select the lowest distance and toggle dnd for that one
                    val distanceInMeters = lastLocation.distanceTo(targetLocation)
                    println("Distance: $distanceInMeters")
                }
            },
            Looper.getMainLooper())
        return super.onStartCommand(intent, flags, startId)
    }

    private companion object {
        const val CHANNEL_ID = "04022025"
        const val CHANNEL_NAME = "CHANNEL NAME"
        const val LOCATION_UPDATE_INTERVAL_MILLIS = 10_000L
    }

    // TODO
    override fun onDestroy() {

        super.onDestroy()
    }
}
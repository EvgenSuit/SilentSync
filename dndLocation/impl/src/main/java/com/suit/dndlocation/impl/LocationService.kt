package com.suit.dndlocation.impl

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.suit.dndlocation.impl.db.SavedLocationsDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class LocationService: Service(), KoinComponent {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val savedLocationsDb by inject<SavedLocationsDb>()
    private val coroutineScope by inject<CoroutineScope>()

    override fun onBind(intent: Intent?) = null

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
            .build();
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val savedLocationsDao = savedLocationsDb.savedLocationDao()

        ServiceCompat.startForeground(
            this,
            1,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object: LocationCallback() {
            @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            override fun onLocationResult(locationResult: LocationResult) {
                coroutineScope.launch {
                    val lastLocation = locationResult.lastLocation ?: return@launch

                    println("Result: ${lastLocation.let { "Lat: ${it.latitude}, Long: ${it.longitude}" }}")
                    val savedLocations = savedLocationsDao.fetchLocations().first().map { location ->
                        println("Saved location: ${location.let { "Lat: ${it.latitude}, Long: ${it.longitude}" }}")

                        val targetLatitude = location.latitude //51.08793 //51.0878733
                        val targetLongitude = location.longitude //17.011963 //17.0120722
                        val targetLocation = Location("").apply {
                            latitude = location.latitude
                            longitude = location.longitude
                        }

                        // if location is within a specified radius, add it to a list
                        // then select the lowest distance and toggle dnd for that one
                        val distanceInMeters = lastLocation.distanceTo(targetLocation)
                        location to distanceInMeters
                    }.sortedBy { it.second }

                    savedLocations.forEach { (location, distanceInMeters) ->
                        println("Radius meters: ${location.radiusMeters}, Distance: $distanceInMeters")

                        if (distanceInMeters <= location.radiusMeters) {
                            println("Located withing the bounds")

                            if (!location.didEnter) {
                                // update status irrespectively of DND options since they're prone to change
                                savedLocationsDao.updateZoneStatus(location.mapBoxId, true, false)
                                if (location.turnDNDOnUponEntering) {
                                    println("Turning DND on")
                                }
                            }
                            // turn dnd off and exit the loop after managing the closest location within the bounds,
                            // accounting for overlaps
                            return@launch
                        } else if (location.didEnter && !location.didExit) {
                            savedLocationsDao.updateZoneStatus(location.mapBoxId, false, true)
                            if (location.turnDNDOffUponExiting) {
                                println("Turning DND off")
                            }
                        }
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            LocationRequest.Builder(LOCATION_UPDATE_INTERVAL_MILLIS)
                .setIntervalMillis(LOCATION_UPDATE_INTERVAL_MILLIS)
                // TODO change to balanced
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build(),
            locationCallback,
            Looper.getMainLooper())
        return super.onStartCommand(intent, flags, startId)
    }

    private companion object {
        const val CHANNEL_ID = "04022025"
        const val CHANNEL_NAME = "CHANNEL NAME"
        const val LOCATION_UPDATE_INTERVAL_MILLIS = 10_000L
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}
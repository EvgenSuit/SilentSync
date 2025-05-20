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
import com.suit.dndlocation.api.SavedLocation
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
    private lateinit var notificationManager: NotificationManager

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
    }
    override fun onBind(intent: Intent?) = null

    private fun buildChannel() {
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MIN
                )
            )

    }

    private fun buildNotification(): Notification {
        buildChannel()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val highAccuracyMode = intent?.getBooleanExtra("HIGH_ACCURACY_MODE", false) ?: false
        handleZones(highAccuracyMode)
        return START_STICKY
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun handleZones(highAccuracyMode: Boolean) {
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
                val currentLocation = locationResult.lastLocation ?: return
                CurrentLocation.updateLocation(currentLocation)

                coroutineScope.launch {
                    println("Current location: ${currentLocation.let { "Lat: ${it.latitude}, Long: ${it.longitude}" }}")

                    val savedLocations = savedLocationsDao.fetchLocations().first()
                    var closestInsideLocation: Pair<SavedLocation, Float>? = null
                    var minDistanceInside = Float.MAX_VALUE
                    var closestOutsideLocation: Pair<SavedLocation, Float>? = null
                    var minDistanceOutside = Float.MAX_VALUE

                    for (savedLocation in savedLocations) {
                        val targetLocation = Location("").apply {
                            latitude = savedLocation.latitude
                            longitude = savedLocation.longitude
                        }
                        val distanceInMeters = currentLocation.distanceTo(targetLocation)
                        if (distanceInMeters <= savedLocation.radius) {
                            if (!savedLocation.didEnter) {
                                if (distanceInMeters < minDistanceInside) {
                                    closestInsideLocation = savedLocation to distanceInMeters
                                    minDistanceInside = distanceInMeters
                                }
                            }
                        } else if (savedLocation.didEnter && !savedLocation.didExit) {
                            if (distanceInMeters < minDistanceOutside) {
                                closestOutsideLocation = savedLocation to distanceInMeters
                                minDistanceOutside = distanceInMeters
                            }
                        }
                    }
                    closestInsideLocation?.let { (location, _) ->
                        if (location.turnDNDOnUponEntering) {
                            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                        }
                        savedLocationsDao.updateZoneStatus(location.mapBoxId, true, false)
                        println("Turning DND on")
                    }
                    // After checking all locations, handle the closest exited location if no new entry occurred
                    if (closestInsideLocation == null) {
                        closestOutsideLocation?.let { (location, _) ->
                            if (location.turnDNDOffUponExiting) {
                                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                            }
                            savedLocationsDao.updateZoneStatus(location.mapBoxId, false, true)
                        }
                    }
                }
            }
        }

        fusedLocationClient.removeLocationUpdates(locationCallback)
        val locationUpdateIntervalMillis = if (highAccuracyMode) 2_000L else 30_000L
        fusedLocationClient.requestLocationUpdates(
            LocationRequest.Builder(locationUpdateIntervalMillis)
                .setIntervalMillis(locationUpdateIntervalMillis)
                .setPriority(if (highAccuracyMode) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_LOW_POWER)
                .build(),
            locationCallback,
            Looper.getMainLooper())
    }

    private companion object {
        const val CHANNEL_ID = "04022025"
        const val CHANNEL_NAME = "CHANNEL NAME"
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}
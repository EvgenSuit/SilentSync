package com.suit.dndlocation.impl

import android.Manifest
import android.app.AlarmManager
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.HandlerThread
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.suit.dndlocation.api.SavedLocation
import com.suit.dndlocation.impl.db.SavedLocationsDb
import com.suit.utility.analytics.SilentSyncAnalytics
import com.suit.utility.analytics.SilentSyncEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class LocationService: Service(), KoinComponent {
    private val fusedLocationClient by inject<FusedLocationProviderClient>()
    private lateinit var locationCallback: LocationCallback
    private val analytics by inject<SilentSyncAnalytics>()
    private val coroutineScope by inject<CoroutineScope>()
    private val savedLocationsDb by inject<SavedLocationsDb>()
    private val notificationManager by inject<NotificationManager>()
    private val alarmManager by inject<AlarmManager>()

    override fun onBind(intent: Intent?) = null

    private fun buildChannel() {
        notificationManager
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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val highAccuracyMode = intent?.getBooleanExtra("HIGH_ACCURACY_MODE", false) ?: false
            handleZones(highAccuracyMode)
        } catch (e: IllegalStateException) {
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
               alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 15_000, PendingIntent.getService(this, 0,
                   Intent(this, LocationService::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
               analytics.logEvent(SilentSyncEvent.LOCATION_SERVICE_ALARM_FALLBACK)
           } else {
               analytics.recordException(e)
           }
        }
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
        locationCallback = object: LocationCallback() {
            @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLocation = locationResult.lastLocation ?: return
                CurrentLocation.updateLocation(currentLocation)

                coroutineScope.launch {
                    val saved = savedLocationsDao.fetchLocations().first()

                    // 1) Detect crossings
                    val zonesJustEntered = mutableListOf<SavedLocation>()
                    val zonesJustExited  = mutableListOf<SavedLocation>()

                    for (z in saved) {
                        val dist = currentLocation.distanceTo(Location("").apply {
                            latitude  = z.latitude
                            longitude = z.longitude
                        })

                        // Hysteresis. Avoids rapid re-entry/exit due to GPS jitter. (buffer zone)
                        val nowInside = dist < z.radius - GEOFENCE_HYSTERESIS_METERS
                        val nowOutside = dist > z.radius + GEOFENCE_HYSTERESIS_METERS

                        if (nowInside && !z.didEnter) {
                            zonesJustEntered += z
                        }
                        if (nowOutside && z.didEnter && !z.didExit) {
                            zonesJustExited += z
                        }
                    }

                    // 2) Update DB flags in one go
                    zonesJustEntered.forEach { z ->
                        savedLocationsDao.updateZoneStatus(z.id, true, false)
                    }
                    zonesJustExited.forEach { z ->
                        savedLocationsDao.updateZoneStatus(z.id, false, true)
                    }

                    // 3) Edge-trigger DND
                    // a) Any zone we just entered that says "turn on" => fire once
                    zonesJustEntered.firstOrNull { it.turnDNDOnUponEntering }?.let {
                        notificationManager.setInterruptionFilter(
                            NotificationManager.INTERRUPTION_FILTER_PRIORITY
                        )
                        return@launch // we’re done for this callback
                    }

                    // b) Else, any zone we just exited that says "turn off" => but only if
                    //    no other zone is currently active that still wants DND-on
                    if (zonesJustExited.any { it.turnDNDOffUponExiting }) {
                        // check fresh active zones:
                        val stillInside = saved
                            .map { z -> z.copy(
                                didEnter = if (zonesJustEntered.any { it.id == z.id }) true
                                else if (zonesJustExited.any  { it.id == z.id }) false
                                else z.didEnter
                            )
                            }
                            .filter { it.didEnter }

                        // only turn off if none of those stillInside want DND-on
                        if (stillInside.none { it.turnDNDOnUponEntering }) {
                            notificationManager.setInterruptionFilter(
                                NotificationManager.INTERRUPTION_FILTER_ALL
                            )
                        }
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            LocationRequest.Builder(0)
                .setPriority(if (highAccuracyMode) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setWaitForAccurateLocation(true)
                .setMinUpdateDistanceMeters(if (highAccuracyMode) 2f else 4f)
                .build(),
            locationCallback,
            HandlerThread("LocationThread").apply { start() }.looper)
    }

    companion object {
        private const val CHANNEL_ID = "04022025"
        private const val CHANNEL_NAME = "CHANNEL NAME"
        const val GEOFENCE_HYSTERESIS_METERS = 5f
        const val HIGH_ACCURACY_MODE = "HIGH_ACCURACY_MODE"
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}
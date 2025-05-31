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
import com.suit.utility.analytics.SilentSyncAnalytics
import com.suit.utility.analytics.SilentSyncEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class LocationService: Service(), KoinComponent {
    private val fusedLocationClient by inject<FusedLocationProviderClient>()
    private lateinit var locationCallback: LocationCallback
    private val analytics by inject<SilentSyncAnalytics>()

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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val highAccuracyMode = intent?.getBooleanExtra("HIGH_ACCURACY_MODE", false) ?: false
            handleZones(highAccuracyMode)
        } catch (e: IllegalStateException) {
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
               val mgr = this.getSystemService(AlarmManager::class.java)
               mgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 10_000, PendingIntent.getService(this, 0,
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
                println("Current location: $currentLocation")
                CurrentLocation.updateLocation(currentLocation)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            LocationRequest.Builder(0)
                //.setIntervalMillis(2000)
                .setPriority(if (highAccuracyMode) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setWaitForAccurateLocation(true)
                .setMinUpdateDistanceMeters(2f)
                .build(),
            locationCallback,
            HandlerThread("LocationThread").apply { start() }.looper)
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
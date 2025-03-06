package com.suit.silentsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.suit.silentsync.navigation.SilentSyncNavHost
import com.suit.silentsync.ui.theme.SilentSyncTheme

class MainActivity : ComponentActivity() {


    /*override fun onStart() {
        super.onStart()

        begin()
    }
    private fun scheduleAlarms() {
        dndCalendarScheduler.schedule(applicationContext)
    }*/

    /*private fun begin() {
        // TODO: make the process of asking for permissions user-friendly
        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !applicationContext.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            return
        }
        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
            return
        }
        scheduleAlarms()
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SilentSyncTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SilentSyncNavHost(
                        modifier = Modifier
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

package com.suit.silentsync

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.suit.silentsync.ui.theme.SilentSyncTheme

class MainActivity : ComponentActivity() {
    private val calendarPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) scheduleAlarm() //scheduleWork()
    }

    override fun onStart() {
        super.onStart()
        //toggleDnd()
        begin()
    }

    private fun toggleDnd() {
        applicationContext.getSystemService(NotificationManager::class.java)
            .setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
    }

    private fun scheduleAlarm() {

    }

    private fun scheduleWork() {
        /*val work = OneTimeWorkRequestBuilder<CalendarWorker>()
            .setConstraints(
                Constraints.Builder()
                .addContentUriTrigger(CalendarContract.Events.CONTENT_URI, true)
                .build())
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "CalendarWorker",
            ExistingWorkPolicy.REPLACE,
            work
        )*/
    }

    private fun begin() {
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
        } else scheduleAlarm()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SilentSyncTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ElevatedButton(
                            onClick = {
                                begin()
                            }
                        ) {
                            Text("Begin")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SilentSyncTheme {
        Greeting("Android")
    }
}
package com.suit.feature.dndcalendar.presentation.ui

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.suit.feature.dndcalendar.R
import com.suit.utility.ui.PermissionDialog

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DNDPermissionComponent(
    onShowUI: () -> Unit
) {
    val context = LocalContext.current
    var dialogMessage by remember {
        mutableStateOf("")
    }
    var dialogCallback by remember {
        mutableStateOf({})
    }
    val notificationPolicyMessage = stringResource(R.string.notification_policy_permission)
    val scheduleExactAlarmsMessage = stringResource(R.string.schedule_exact_alarm)
    val readCalendarMessage = stringResource(R.string.read_calendar)
    val readCalendarPermissionState = rememberPermissionState(Manifest.permission.READ_CALENDAR)

    val notificationManager = context.getSystemService(NotificationManager::class.java)
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    var isNotificationPolicyAccessGranted by remember {
        mutableStateOf(false)
    }
    var canScheduleExactAlarms by remember {
        mutableStateOf(false)
    }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        // manually reassign on each ON_START event
        isNotificationPolicyAccessGranted = notificationManager.isNotificationPolicyAccessGranted
        if (!isNotificationPolicyAccessGranted) {
            dialogCallback = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            }
            dialogMessage = notificationPolicyMessage
            return@LifecycleEventEffect
        }

        canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms()
        else true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms) {
            dialogCallback = {
                context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
            dialogMessage = scheduleExactAlarmsMessage
        }
    }

    LaunchedEffect(isNotificationPolicyAccessGranted, canScheduleExactAlarms, readCalendarPermissionState.status) {
        if (isNotificationPolicyAccessGranted && canScheduleExactAlarms) {
            if (!readCalendarPermissionState.status.isGranted) {
                // if a user previously denied the permission and clicks on Accept again, navigate them to app settings
                if (readCalendarPermissionState.status.shouldShowRationale) {
                    dialogCallback = {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        })
                    }
                    dialogMessage = readCalendarMessage
                    return@LaunchedEffect
                }
                dialogCallback = {
                    readCalendarPermissionState.launchPermissionRequest()
                }
                dialogMessage = readCalendarMessage
            } else {
                dialogMessage = ""
                onShowUI()
            }
        }
    }

    if (dialogMessage.isNotEmpty()) {
        PermissionDialog(
            text = dialogMessage,
            onDismiss = {},
            onAccept = dialogCallback
        )
    }
}
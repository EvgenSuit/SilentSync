package com.suit.dndCalendar.impl.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.suit.dndcalendar.api.DNDCalendarScheduler
import com.suit.utility.analytics.SilentSyncAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class CalendarChangeReceiver: BroadcastReceiver(), KoinComponent {
    private val dndCalendarScheduler: DNDCalendarScheduler by inject()
    private val coroutineScope: CoroutineScope by inject()
    private val silentSyncAnalytics: SilentSyncAnalytics by inject()

    override fun onReceive(context: Context, intent: Intent) {
        coroutineScope.launch {
            try {
                dndCalendarScheduler.schedule()
            } catch (e: Exception) {
                silentSyncAnalytics.recordException(e)
            }
        }
    }
}
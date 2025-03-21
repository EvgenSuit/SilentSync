package com.suit.testutil.test

import com.suit.utility.analytics.SilentSyncAnalytics
import io.mockk.mockk

fun analyticsMock(): SilentSyncAnalytics = mockk(relaxed = true)
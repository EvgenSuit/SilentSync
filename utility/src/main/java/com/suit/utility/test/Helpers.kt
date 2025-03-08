package com.suit.utility.test

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.core.app.ApplicationProvider

fun ComposeContentTestRule.getString(@StringRes id: Int) =
    ApplicationProvider.getApplicationContext<Context>()
        .getString(id)
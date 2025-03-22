package com.suit.playreview.api

interface PlayReviewManager {
    suspend fun doShowDialog(): Boolean
    suspend fun labelDialogAsShown()
}
package com.suit.utility.ui

sealed class DNDCalendarUIEvent {
    data class ShowSnackbar(val uiText: UIText): DNDCalendarUIEvent()
    data object Unfocus: DNDCalendarUIEvent()
}
package com.suit.dndcalendar.api

interface CalendarEventChecker {
    fun doesEventExist(id: Long): Boolean

    // below checks are needed in case event changes aren't detected in time by the receiver

    // accounts for a case where the user changes start time of an already active event to the future (do nothing)
    fun doTurnDNDon(id: Long): Boolean

    // accounts for a case where the user changes end time of an already active event closer to the curr time - do nothing
    fun doTurnDNDoff(id: Long): Boolean
}
package com.suit.dndCalendar.impl

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class TestClock: Clock() {
    private var instant = Instant.now()

    fun advance(millis: Long) {
        instant = instant.plusMillis(millis)
    }

    override fun instant(): Instant = instant

    override fun withZone(zone: ZoneId?): Clock = system(zone)

    override fun getZone(): ZoneId = ZoneId.systemDefault()
}
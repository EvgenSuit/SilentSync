package com.suit.silentsync

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class TestClock: Clock() {
    private val instant = Instant.now()
    override fun instant(): Instant = instant

    override fun withZone(zone: ZoneId?): Clock = system(zone)

    override fun getZone(): ZoneId = ZoneId.systemDefault()
}
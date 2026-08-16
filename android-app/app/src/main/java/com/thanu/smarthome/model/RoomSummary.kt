package com.thanu.smarthome.model

/*
 * Lightweight per-room device summary, derived from the same
 * real-time snapshot used to load the room list. Powers the
 * abstract grid tiles on RoomScreen without a second round-trip.
 */
data class RoomSummary(
    val deviceCount: Int = 0,
    val devicesOn: Int = 0,
    val hasCriticalAlert: Boolean = false
)

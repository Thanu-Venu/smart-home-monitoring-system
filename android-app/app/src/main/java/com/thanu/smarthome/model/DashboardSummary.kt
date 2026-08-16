package com.thanu.smarthome.model

/*
 * Aggregate counts across every home the signed-in user owns —
 * powers the stat cards at the top of the Home screen. Computed
 * client-side by walking the same Firebase snapshot the home list
 * itself already fetches (each home node already carries its full
 * floors/rooms/devices subtree), so this doesn't cost an extra read.
 */
data class DashboardSummary(
    val floorsCount: Int = 0,
    val roomsCount: Int = 0,
    val devicesCount: Int = 0,
    val devicesOn: Int = 0
)

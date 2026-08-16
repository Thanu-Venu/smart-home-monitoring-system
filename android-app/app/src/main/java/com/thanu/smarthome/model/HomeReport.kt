package com.thanu.smarthome.model

/*
 * One entry in a HomeReport's active-alerts list — a device that
 * SafetyMonitor (or a manual "Simulate: Error") has flagged.
 */
data class DeviceAlert(
    val roomName: String = "",
    val deviceName: String = "",
    val alert: String = ""
)

/*
 * Snapshot usage report for a single home, aggregated client-side
 * from the live homes/{homeId} subtree. This is the in-app answer
 * to the spec's "usage data of the important devices can be
 * tracked from the mobile app" requirement.
 */
data class HomeReport(
    val floorsCount: Int = 0,
    val roomsCount: Int = 0,
    val totalDevices: Int = 0,
    val devicesOn: Int = 0,
    val devicesOff: Int = 0,
    val deviceTypeCounts: Map<String, Int> = emptyMap(),
    val activeAlerts: List<DeviceAlert> = emptyList()
)

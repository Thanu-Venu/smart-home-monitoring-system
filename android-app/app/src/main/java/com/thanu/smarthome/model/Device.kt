package com.thanu.smarthome.model

data class Device(
    var id: String = "",
    var name: String = "",
    var type: String = "",

    // Current operational state
    var status: String = "OFF",

    // Current power state
    var on: Boolean = false,

    // Multi-switch support
    var switchCount: Int = 0,
    var switches: List<DeviceSwitch> = emptyList(),

    // Safety-critical device support
    var maxOnDurationMinutes: Int = 0,

    // Automatic scheduling
    var scheduleEnabled: Boolean = false,
    var scheduleStart: String = "",
    var scheduleEnd: String = "",

    // Security camera support
    var cameraUri: String = ""
)

data class DeviceSwitch(
    var id: String = "",
    var name: String = "",
    var on: Boolean = false,
    var status: String = "OFF"
)
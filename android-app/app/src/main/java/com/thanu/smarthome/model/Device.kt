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

    // Epoch millis timestamp of when the device was last switched ON.
    // Used by SafetyMonitor to enforce maxOnDurationMinutes.
    var turnedOnAt: Long = 0,

    // Safety condition surfaced by the safety cutoff worker: "NORMAL" or "CRITICAL".
    var condition: String = "NORMAL",

    // Human-readable message set when a device is auto-switched off for safety.
    var alert: String = "",

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
    var status: String = "OFF",

    // Per-switch automatic scheduling — mirrors Device's own
    // scheduleEnabled/scheduleStart/scheduleEnd, but scoped to just
    // this one switch, since a gang-box's switches can each control
    // something different (e.g. one switch for a light that should
    // follow a schedule, another for a fan that shouldn't). Matches
    // the web simulator's updateMultiSwitchSchedule, which already
    // schedules a single switch this same way.
    var scheduleEnabled: Boolean = false,
    var scheduleStart: String = "",
    var scheduleEnd: String = ""
)
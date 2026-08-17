package com.thanu.smarthome.worker

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/*
 * SCHEDULE MONITOR
 *
 * Enforces the spec's "some light bulbs may be set to turn on and
 * off automatically during a preset time period" requirement — while
 * still letting the owner manually override a schedule-controlled
 * device from the app, which the spec's device-control requirement
 * implies should always work regardless of what else controls a
 * device.
 *
 * Any device with scheduleEnabled = true and a scheduleStart /
 * scheduleEnd ("HH:mm", 24-hour) is treated as schedule-controlled:
 * this worker turns it ON the moment the current time *enters* that
 * window and OFF the moment it *leaves* it, handling windows that
 * wrap past midnight (e.g. 21:00 - 05:00).
 *
 * Importantly, this only acts at the moment a window boundary is
 * actually crossed (edge-triggered), not on every poll. lastWindowState
 * remembers whether each device was inside its window as of the
 * previous check; if that hasn't changed since last time, the device
 * is left alone no matter what its current on/off state is. That's
 * what lets a manual toggle from the app stick between schedule
 * boundaries — e.g. turning a light ON outside its window keeps it
 * on until the window's own start/end time naturally reasserts
 * control, instead of this worker fighting the user's last action
 * every 30 seconds.
 *
 * Devices the user has manually marked ERROR/DISCONNECTED (via
 * DeviceScreen's "Simulate" menu) are left alone — a device that
 * isn't reachable can't be scheduled.
 *
 * A Multi-Switch (gang-box) device can ALSO have its own individual
 * switches scheduled independently (e.g. a "Light Switch" that should
 * follow a schedule, sitting next to a "Fan Switch" that shouldn't) —
 * see checkSwitchSchedules. This is checked separately from the
 * device-level schedule above, since a Multi-Switch device itself
 * never has scheduleEnabled = true (only its individual switches do).
 */
object ScheduleMonitor {

    private const val TAG = "ScheduleMonitor"

    /*
     * Checked less frequently than SafetyMonitor since schedules
     * only need minute-level precision, not second-level.
     */
    private const val CHECK_INTERVAL_MS = 30_000L

    private val database = FirebaseDatabase.getInstance(
        "https://smart-home-monitoring-sy-f5e88-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    private val homesRef = database.getReference("homes")

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private var isRunning = false

    /*
     * "was this schedule-controlled thing inside its window as of the
     * last check?". Keyed by deviceId for a device-level schedule, or
     * "deviceId/switches/switchId" for an individual switch's own
     * schedule — the two never collide since a switchId always has a
     * "/switches/" segment in it. Rebuilt fresh every check cycle (see
     * checkAllDevices), so deleted/no-longer-scheduled devices and
     * switches drop out on their own instead of leaking forever.
     */
    private var lastWindowState: Map<String, Boolean> = emptyMap()


    fun start() {

        if (isRunning) {
            return
        }

        isRunning = true

        Log.d(TAG, "Schedule monitor started")

        scope.launch {

            while (true) {

                try {
                    checkAllDevices()
                } catch (exception: Exception) {

                    Log.e(TAG, "Schedule check failed", exception)
                }

                delay(CHECK_INTERVAL_MS)
            }
        }
    }


    private suspend fun checkAllDevices() {

        val homesSnapshot = homesRef.get().awaitResult()

        val nowMinutes = currentMinuteOfDay()

        /*
         * Built fresh this cycle, then swapped into lastWindowState
         * once everything's been checked — see the field comment.
         */
        val newWindowState = mutableMapOf<String, Boolean>()

        for (homeSnapshot in homesSnapshot.children) {

            val homeId = homeSnapshot.key ?: continue

            for (floorSnapshot in homeSnapshot.child("floors").children) {

                val floorId = floorSnapshot.key ?: continue

                for (roomSnapshot in floorSnapshot.child("rooms").children) {

                    val roomId = roomSnapshot.key ?: continue

                    for (deviceSnapshot in roomSnapshot.child("devices").children) {

                        val deviceId = deviceSnapshot.key ?: continue

                        // A manually-simulated fault means neither the
                        // device's own schedule nor any of its switches'
                        // schedules should actually change its on/off
                        // state. isControllable is threaded through
                        // instead of `continue`-ing past this device
                        // entirely, so window tracking below still runs
                        // for a faulted device — otherwise, the moment
                        // it's reconnected, lastWindowState would have
                        // no entry for it, this worker would treat it as
                        // "first time seen," and immediately force it to
                        // whatever the schedule window currently
                        // dictates, overriding whatever the user just
                        // set on reconnect.
                        val status =
                            deviceSnapshot.child("status")
                                .getValue(String::class.java) ?: "OFF"

                        val isControllable =
                            status != "ERROR" && status != "DISCONNECTED"

                        checkDeviceLevelSchedule(
                            homeId = homeId,
                            floorId = floorId,
                            roomId = roomId,
                            deviceId = deviceId,
                            deviceSnapshot = deviceSnapshot,
                            nowMinutes = nowMinutes,
                            newWindowState = newWindowState,
                            isControllable = isControllable
                        )

                        // Independent of the device-level schedule
                        // above — a Multi-Switch device's own
                        // scheduleEnabled is always false, its
                        // switches carry their own schedules instead.
                        checkSwitchSchedules(
                            homeId = homeId,
                            floorId = floorId,
                            roomId = roomId,
                            deviceId = deviceId,
                            deviceSnapshot = deviceSnapshot,
                            nowMinutes = nowMinutes,
                            newWindowState = newWindowState,
                            isControllable = isControllable
                        )
                    }
                }
            }
        }

        lastWindowState = newWindowState
    }


    private fun checkDeviceLevelSchedule(
        homeId: String,
        floorId: String,
        roomId: String,
        deviceId: String,
        deviceSnapshot: DataSnapshot,
        nowMinutes: Int,
        newWindowState: MutableMap<String, Boolean>,
        isControllable: Boolean
    ) {

        val scheduleEnabled =
            deviceSnapshot.child("scheduleEnabled").getValue(Boolean::class.java) ?: false

        if (!scheduleEnabled) {
            return
        }

        val startMinutes =
            parseTimeToMinutes(
                deviceSnapshot.child("scheduleStart").getValue(String::class.java)
            )

        val endMinutes =
            parseTimeToMinutes(
                deviceSnapshot.child("scheduleEnd").getValue(String::class.java)
            )

        if (startMinutes == null || endMinutes == null) {
            return
        }

        val shouldBeOn =
            isWithinWindow(nowMinutes, startMinutes, endMinutes)

        // Record this device's window state for the *next* cycle's
        // comparison, regardless of what we do below.
        newWindowState[deviceId] = shouldBeOn

        val previousShouldBeOn = lastWindowState[deviceId]

        if (previousShouldBeOn != null && previousShouldBeOn == shouldBeOn) {

            /*
             * Still inside the same window segment as last check —
             * no boundary crossed, so this is exactly the gap where
             * a manual override from the app should be respected.
             * Leave the device's actual on/off state alone.
             */
            return
        }

        /*
         * A manually-simulated fault (ERROR/DISCONNECTED) means the
         * schedule shouldn't actually touch this device's state — but
         * newWindowState above was still recorded, so a later reconnect
         * resumes tracking from here instead of restarting cold.
         */
        if (!isControllable) {
            return
        }

        /*
         * Either this is the very first time this device has been
         * seen (app just started, or its schedule was just turned
         * on) or the window boundary was crossed since the last
         * check — in both cases the schedule takes control now.
         */
        val isOn =
            deviceSnapshot.child("on").getValue(Boolean::class.java) ?: false

        if (shouldBeOn == isOn) {
            // Already in the correct state, nothing to write.
            return
        }

        val deviceRef = homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")
            .child(roomId)
            .child("devices")
            .child(deviceId)

        val deviceName =
            deviceSnapshot.child("name").getValue(String::class.java) ?: "Device"

        Log.d(
            TAG,
            "$deviceName ($deviceId) schedule window changed -> " +
                    (if (shouldBeOn) "ON" else "OFF")
        )

        val updates: Map<String, Any> = mapOf(
            "on" to shouldBeOn,
            "status" to if (shouldBeOn) "ON" else "OFF",
            "turnedOnAt" to if (shouldBeOn) System.currentTimeMillis() else 0L,

            /*
             * Same as DeviceViewModel.toggleDevice()'s manual toggle:
             * any state change this worker makes clears a previous
             * safety-cutoff CRITICAL alert, since the schedule taking
             * control again means the old alert no longer applies.
             * Without this, a device SafetyMonitor cut off would show
             * ON with a stale CRITICAL banner once its schedule turns
             * it back on.
             */
            "condition" to "NORMAL",
            "alert" to ""
        )

        deviceRef.updateChildren(updates)
    }


    /*
     * Same edge-triggered on/off logic as checkDeviceLevelSchedule,
     * but applied per-switch for a Multi-Switch device — each switch
     * carries its own scheduleEnabled/scheduleStart/scheduleEnd,
     * independent of every other switch on the same gang-box.
     *
     * After changing a switch's own on/status, the device-level
     * on/status is also recomputed from ALL of the device's switches
     * (anyOn) in the same write, so the device card and room-grid
     * summary — which read the device-level "on" field directly,
     * not the individual switches — stay in sync. This mirrors
     * DeviceViewModel.toggleSwitch()'s derivation logic on the manual
     * toggle path.
     */
    private fun checkSwitchSchedules(
        homeId: String,
        floorId: String,
        roomId: String,
        deviceId: String,
        deviceSnapshot: DataSnapshot,
        nowMinutes: Int,
        newWindowState: MutableMap<String, Boolean>,
        isControllable: Boolean
    ) {

        val switchesSnapshot = deviceSnapshot.child("switches")

        if (!switchesSnapshot.hasChildren()) {
            return
        }

        // Current on/off state of every switch on this device, so
        // recomputing "is any switch on" after this cycle's changes
        // doesn't require a second Firebase read.
        val currentSwitchOnStates =
            switchesSnapshot.children.associate { switchSnapshot ->

                (switchSnapshot.key ?: "") to
                        (
                                switchSnapshot.child("on")
                                    .getValue(Boolean::class.java) ?: false
                                )
            }

        // Switch id -> new on/off state, only for switches this cycle
        // actually changes (schedule boundary crossed). Applied to
        // currentSwitchOnStates below to get the final anyOn.
        val changedSwitchStates = mutableMapOf<String, Boolean>()

        for (switchSnapshot in switchesSnapshot.children) {

            val switchId = switchSnapshot.key ?: continue

            val switchScheduleEnabled =
                switchSnapshot.child("scheduleEnabled")
                    .getValue(Boolean::class.java) ?: false

            if (!switchScheduleEnabled) {
                continue
            }

            val startMinutes =
                parseTimeToMinutes(
                    switchSnapshot.child("scheduleStart")
                        .getValue(String::class.java)
                )

            val endMinutes =
                parseTimeToMinutes(
                    switchSnapshot.child("scheduleEnd")
                        .getValue(String::class.java)
                )

            if (startMinutes == null || endMinutes == null) {
                continue
            }

            val shouldBeOn =
                isWithinWindow(nowMinutes, startMinutes, endMinutes)

            val windowKey = "$deviceId/switches/$switchId"

            newWindowState[windowKey] = shouldBeOn

            val previousShouldBeOn = lastWindowState[windowKey]

            if (previousShouldBeOn != null && previousShouldBeOn == shouldBeOn) {
                // No boundary crossed — leave a manual override alone.
                continue
            }

            // Window tracking above still ran for a faulted device;
            // only the actual write is skipped here (see the matching
            // comment in checkDeviceLevelSchedule).
            if (!isControllable) {
                continue
            }

            val isOn = currentSwitchOnStates[switchId] ?: false

            if (shouldBeOn == isOn) {
                continue
            }

            changedSwitchStates[switchId] = shouldBeOn
        }

        if (changedSwitchStates.isEmpty()) {
            return
        }

        val deviceRef = homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")
            .child(roomId)
            .child("devices")
            .child(deviceId)

        val deviceName =
            deviceSnapshot.child("name").getValue(String::class.java) ?: "Device"

        val updates = mutableMapOf<String, Any>()

        for ((switchId, shouldBeOn) in changedSwitchStates) {

            Log.d(
                TAG,
                "$deviceName ($deviceId) switch $switchId schedule window " +
                        "changed -> " + (if (shouldBeOn) "ON" else "OFF")
            )

            updates["switches/$switchId/on"] = shouldBeOn
            updates["switches/$switchId/status"] =
                if (shouldBeOn) "ON" else "OFF"
        }

        val finalSwitchOnStates =
            currentSwitchOnStates + changedSwitchStates

        val anyOn = finalSwitchOnStates.values.any { it }

        updates["on"] = anyOn
        updates["status"] = if (anyOn) "ON" else "OFF"

        deviceRef.updateChildren(updates)
    }


    /*
     * Handles windows that wrap past midnight, e.g. 21:00 - 05:00
     * means "on" from 21:00 to 23:59 AND from 00:00 to 04:59.
     */
    private fun isWithinWindow(
        nowMinutes: Int,
        startMinutes: Int,
        endMinutes: Int
    ): Boolean {

        return if (startMinutes <= endMinutes) {
            nowMinutes in startMinutes until endMinutes
        } else {
            nowMinutes >= startMinutes || nowMinutes < endMinutes
        }
    }


    private fun currentMinuteOfDay(): Int {

        val calendar = Calendar.getInstance()

        return calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                calendar.get(Calendar.MINUTE)
    }


    /*
     * Parses "HH:mm" (24-hour, as produced by DeviceScreen's
     * TimePicker) into minutes-since-midnight. Returns null for
     * blank/malformed values so callers can skip the device.
     */
    private fun parseTimeToMinutes(time: String?): Int? {

        if (time.isNullOrBlank()) {
            return null
        }

        val parts = time.split(":")

        if (parts.size != 2) {
            return null
        }

        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null

        if (hour !in 0..23 || minute !in 0..59) {
            return null
        }

        return hour * 60 + minute
    }


    private suspend fun <T> Task<T>.awaitResult(): T =
        suspendCancellableCoroutine { continuation ->

            addOnSuccessListener { result ->
                continuation.resume(result)
            }

            addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }
}

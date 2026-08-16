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
     * deviceId -> "was this device inside its schedule window as of
     * the last check?". Rebuilt fresh every check cycle (see
     * checkAllDevices), so deleted/no-longer-scheduled devices drop
     * out on their own instead of leaking forever.
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

                        checkDevice(
                            homeId = homeId,
                            floorId = floorId,
                            roomId = roomId,
                            deviceId = deviceId,
                            deviceSnapshot = deviceSnapshot,
                            nowMinutes = nowMinutes,
                            newWindowState = newWindowState
                        )
                    }
                }
            }
        }

        lastWindowState = newWindowState
    }


    private fun checkDevice(
        homeId: String,
        floorId: String,
        roomId: String,
        deviceId: String,
        deviceSnapshot: DataSnapshot,
        nowMinutes: Int,
        newWindowState: MutableMap<String, Boolean>
    ) {

        val scheduleEnabled =
            deviceSnapshot.child("scheduleEnabled").getValue(Boolean::class.java) ?: false

        if (!scheduleEnabled) {
            return
        }

        val status =
            deviceSnapshot.child("status").getValue(String::class.java) ?: "OFF"

        // Don't fight a manually-simulated fault state.
        if (status == "ERROR" || status == "DISCONNECTED") {
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
            "turnedOnAt" to if (shouldBeOn) System.currentTimeMillis() else 0L
        )

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

package com.thanu.smarthome.worker

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/*
 * SAFETY MONITOR
 *
 * Client-side implementation of the project's "Server-Side Safety
 * Cutoff" requirement: safety-critical devices (irons, or any device
 * configured with a maxOnDurationMinutes limit) must be automatically
 * switched OFF — with an alert pushed to the database — if they stay
 * ON longer than their configured maximum duration.
 *
 * There is no deployed Cloud Function yet, so this worker plays that
 * role for now: it runs for as long as the app process is alive,
 * independently of which screen is visible, periodically scanning
 * every home/floor/room/device and enforcing the cutoff. A device
 * switched ON from this app OR from the web simulator is still
 * caught, because the check reads directly from Firebase rather than
 * from local UI state.
 *
 * (A follow-up improvement would be to move this exact logic into a
 * Firebase Cloud Function - see backend/cloud-functions - so the
 * cutoff still applies even when the app is fully closed.)
 */
object SafetyMonitor {

    private const val TAG = "SafetyMonitor"

    /*
     * How often the whole device tree is re-checked. Kept short so a
     * device that just crossed its limit is switched off quickly, but
     * not so short that it hammers the database.
     */
    private const val CHECK_INTERVAL_MS = 10_000L

    private val database = FirebaseDatabase.getInstance(
        "https://smart-home-monitoring-sy-f5e88-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    private val homesRef = database.getReference("homes")

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private var isRunning = false


    /*
     * START
     *
     * Safe to call multiple times (e.g. on Activity recreation) —
     * only the first call actually starts the background loop.
     */
    fun start() {

        if (isRunning) {
            return
        }

        isRunning = true

        Log.d(TAG, "Safety monitor started")

        scope.launch {

            while (true) {

                try {
                    checkAllDevices()
                } catch (exception: Exception) {

                    // Permission-denied before login, transient network
                    // issues, etc. Never let the worker loop die.
                    Log.e(TAG, "Safety check failed", exception)
                }

                delay(CHECK_INTERVAL_MS)
            }
        }
    }


    /*
     * WALK THE FULL DEVICE TREE
     *
     * homes -> floors -> rooms -> devices
     */
    private suspend fun checkAllDevices() {

        val homesSnapshot = homesRef.get().awaitResult()

        val now = System.currentTimeMillis()

        for (homeSnapshot in homesSnapshot.children) {

            val homeId = homeSnapshot.key ?: continue

            val floorsSnapshot = homeSnapshot.child("floors")

            for (floorSnapshot in floorsSnapshot.children) {

                val floorId = floorSnapshot.key ?: continue

                val roomsSnapshot = floorSnapshot.child("rooms")

                for (roomSnapshot in roomsSnapshot.children) {

                    val roomId = roomSnapshot.key ?: continue

                    val devicesSnapshot = roomSnapshot.child("devices")

                    for (deviceSnapshot in devicesSnapshot.children) {

                        val deviceId = deviceSnapshot.key ?: continue

                        checkDevice(
                            homeId = homeId,
                            floorId = floorId,
                            roomId = roomId,
                            deviceId = deviceId,
                            deviceSnapshot = deviceSnapshot,
                            now = now
                        )
                    }
                }
            }
        }
    }


    /*
     * CHECK / ENFORCE A SINGLE DEVICE
     */
    private fun checkDevice(
        homeId: String,
        floorId: String,
        roomId: String,
        deviceId: String,
        deviceSnapshot: DataSnapshot,
        now: Long
    ) {

        val isOn =
            deviceSnapshot.child("on").getValue(Boolean::class.java) ?: false

        val maxOnDurationMinutes =
            deviceSnapshot.child("maxOnDurationMinutes").getValue(Int::class.java) ?: 0

        // Only devices that are currently ON and have a safety duration
        // configured (irons, or any device the user set a limit on) are
        // monitored — everything else is left alone.
        if (!isOn || maxOnDurationMinutes <= 0) {
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

        val turnedOnAt =
            deviceSnapshot.child("turnedOnAt").getValue(Long::class.java) ?: 0L

        if (turnedOnAt <= 0L) {

            // Self-heal: device was switched ON before this timestamp
            // existed (or via an older code path). Start the clock now
            // instead of cutting it off immediately.
            deviceRef.child("turnedOnAt").setValue(now)
            return
        }

        val elapsedMinutes = (now - turnedOnAt) / 60000.0

        if (elapsedMinutes >= maxOnDurationMinutes) {

            val deviceName =
                deviceSnapshot.child("name").getValue(String::class.java) ?: "Device"

            Log.w(
                TAG,
                "$deviceName ($deviceId) exceeded ${maxOnDurationMinutes}min limit " +
                        "(on for ${"%.1f".format(elapsedMinutes)}min) — switching OFF"
            )

            val updates: Map<String, Any> = mapOf(
                "on" to false,
                "status" to "OFF",
                "condition" to "CRITICAL",
                "alert" to "$deviceName automatically turned OFF for safety " +
                        "(exceeded ${maxOnDurationMinutes} min limit)",
                "turnedOnAt" to 0L
            )

            deviceRef.updateChildren(updates)
        }
    }


    /*
     * Small bridge so we can `.get()` a Firebase reference from a
     * suspend function without pulling in the extra
     * kotlinx-coroutines-play-services dependency.
     */
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

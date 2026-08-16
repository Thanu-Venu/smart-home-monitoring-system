package com.thanu.smarthome.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thanu.smarthome.model.Device
import com.thanu.smarthome.model.DeviceAlert
import com.thanu.smarthome.model.HomeReport

class ReportRepository {

    private val database = FirebaseDatabase.getInstance(
        "https://smart-home-monitoring-sy-f5e88-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    private val homesRef =
        database.getReference("homes")


    /*
     * OBSERVE HOME REPORT (REAL-TIME)
     *
     * Aggregates the whole homes/{homeId} subtree (floors -> rooms
     * -> devices) into usage stats every time anything under it
     * changes — no separate polling or logging pipeline needed for
     * a live snapshot report.
     */
    fun observeHomeReport(
        homeId: String,
        onReport: (HomeReport) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {

        val homeRef = homesRef.child(homeId)

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                onReport(buildReport(snapshot))
            }

            override fun onCancelled(error: DatabaseError) {

                Log.e(
                    "ReportFirebase",
                    "Report listener cancelled",
                    error.toException()
                )

                onError(error.message)
            }
        }

        homeRef.addValueEventListener(listener)

        return listener
    }


    /*
     * STOP OBSERVING
     */
    fun removeHomeReportListener(
        homeId: String,
        listener: ValueEventListener
    ) {

        homesRef
            .child(homeId)
            .removeEventListener(listener)
    }


    private fun buildReport(homeSnapshot: DataSnapshot): HomeReport {

        var roomsCount = 0
        var totalDevices = 0
        var devicesOn = 0

        val typeCounts = mutableMapOf<String, Int>()
        val alerts = mutableListOf<DeviceAlert>()

        val floorsSnapshot = homeSnapshot.child("floors")

        for (floorSnapshot in floorsSnapshot.children) {

            val roomsSnapshot = floorSnapshot.child("rooms")

            for (roomSnapshot in roomsSnapshot.children) {

                roomsCount++

                val roomName =
                    roomSnapshot.child("name").getValue(String::class.java) ?: "Room"

                for (deviceSnapshot in roomSnapshot.child("devices").children) {

                    val device =
                        deviceSnapshot.getValue(Device::class.java) ?: continue

                    totalDevices++

                    if (device.on) {
                        devicesOn++
                    }

                    val typeLabel =
                        device.type.ifBlank { "OTHER" }

                    typeCounts[typeLabel] =
                        (typeCounts[typeLabel] ?: 0) + 1

                    if (
                        device.condition == "CRITICAL" &&
                        device.alert.isNotBlank()
                    ) {

                        alerts.add(
                            DeviceAlert(
                                roomName = roomName,
                                deviceName = device.name,
                                alert = device.alert
                            )
                        )
                    }
                }
            }
        }

        return HomeReport(
            floorsCount = floorsSnapshot.childrenCount.toInt(),
            roomsCount = roomsCount,
            totalDevices = totalDevices,
            devicesOn = devicesOn,
            devicesOff = totalDevices - devicesOn,
            deviceTypeCounts = typeCounts,
            activeAlerts = alerts
        )
    }
}

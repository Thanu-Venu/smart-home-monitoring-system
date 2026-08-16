package com.thanu.smarthome.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thanu.smarthome.model.Device

class DeviceRepository {

    private val database = FirebaseDatabase.getInstance(
        "https://smart-home-monitoring-sy-f5e88-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    private val homesRef =
        database.getReference("homes")


    /*
     * CREATE DEVICE
     *
     * Firebase path:
     *
     * homes
     *   └── homeId
     *        └── floors
     *             └── floorId
     *                  └── rooms
     *                       └── roomId
     *                            └── devices
     *                                 └── deviceId
     */
    fun createDevice(
        homeId: String,
        floorId: String,
        roomId: String,
        device: Device,
        onSuccess: (Device) -> Unit,
        onError: (String) -> Unit
    ) {

        val devicesRef = homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")
            .child(roomId)
            .child("devices")

        val deviceKey = devicesRef.push().key

        if (deviceKey == null) {

            onError("Failed to generate device ID")
            return
        }

        val deviceWithId = device.copy(
            id = deviceKey
        )

        devicesRef
            .child(deviceKey)
            .setValue(deviceWithId)
            .addOnSuccessListener {

                Log.d(
                    "DeviceFirebase",
                    "Device created successfully: $deviceKey"
                )

                onSuccess(deviceWithId)
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "DeviceFirebase",
                    "Failed to create device",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to create device"
                )
            }
    }


    /*
     * GET DEVICES
     *
     * Retrieves all devices belonging
     * to the selected room.
     */
    fun getDevices(
        homeId: String,
        floorId: String,
        roomId: String,
        onSuccess: (List<Device>) -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")
            .child(roomId)
            .child("devices")
            .get()
            .addOnSuccessListener { snapshot ->

                val devices =
                    snapshot.children.mapNotNull { child ->

                        child.getValue(Device::class.java)
                    }

                Log.d(
                    "DeviceFirebase",
                    "Devices retrieved: ${devices.size}"
                )

                onSuccess(devices)
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "DeviceFirebase",
                    "Failed to retrieve devices",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to retrieve devices"
                )
            }
    }


    /*
     * OBSERVE DEVICES (REAL-TIME)
     *
     * Attaches a live listener instead of doing a one-off fetch, so
     * ON/OFF and safety-cutoff changes made anywhere (this app, the
     * web simulator, or the SafetyMonitor worker) reflect on screen
     * immediately without a manual refresh. Caller is responsible for
     * detaching the returned listener via removeDevicesListener()
     * once it is no longer needed (e.g. in ViewModel.onCleared()).
     */
    fun observeDevices(
        homeId: String,
        floorId: String,
        roomId: String,
        onDevices: (List<Device>) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {

        val devicesRef = homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")
            .child(roomId)
            .child("devices")

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val devices = snapshot.children.mapNotNull { child ->
                    child.getValue(Device::class.java)
                }

                Log.d(
                    "DeviceFirebase",
                    "Devices updated (live): ${devices.size}"
                )

                onDevices(devices)
            }

            override fun onCancelled(error: DatabaseError) {

                Log.e(
                    "DeviceFirebase",
                    "Device listener cancelled",
                    error.toException()
                )

                onError(error.message)
            }
        }

        devicesRef.addValueEventListener(listener)

        return listener
    }


    /*
     * STOP OBSERVING DEVICES
     */
    fun removeDevicesListener(
        homeId: String,
        floorId: String,
        roomId: String,
        listener: ValueEventListener
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")
            .child(roomId)
            .child("devices")
            .removeEventListener(listener)
    }


    /*
     * UPDATE DEVICE
     */
    fun updateDevice(
        homeId: String,
        floorId: String,
        roomId: String,
        device: Device,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")
            .child(roomId)
            .child("devices")
            .child(device.id)
            .setValue(device)
            .addOnSuccessListener {

                Log.d(
                    "DeviceFirebase",
                    "Device updated successfully: ${device.id}"
                )

                onSuccess()
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "DeviceFirebase",
                    "Failed to update device",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to update device"
                )
            }
    }


    /*
     * DELETE DEVICE
     */
    fun deleteDevice(
        homeId: String,
        floorId: String,
        roomId: String,
        deviceId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")
            .child(roomId)
            .child("devices")
            .child(deviceId)
            .removeValue()
            .addOnSuccessListener {

                Log.d(
                    "DeviceFirebase",
                    "Device deleted successfully: $deviceId"
                )

                onSuccess()
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "DeviceFirebase",
                    "Failed to delete device",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to delete device"
                )
            }
    }
}
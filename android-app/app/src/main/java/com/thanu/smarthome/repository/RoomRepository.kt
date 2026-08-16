package com.thanu.smarthome.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thanu.smarthome.model.Room
import com.thanu.smarthome.model.RoomSummary

class RoomRepository {

    private val database = FirebaseDatabase.getInstance(
        "https://smart-home-monitoring-sy-f5e88-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    private val homesRef =
        database.getReference("homes")


    /*
     * CREATE ROOM
     *
     * Firebase path:
     *
     * homes
     *   └── homeId
     *        └── floors
     *             └── floorId
     *                  └── rooms
     *                       └── roomId
     */
    fun createRoom(
        homeId: String,
        floorId: String,
        room: Room,
        onSuccess: (Room) -> Unit,
        onError: (String) -> Unit
    ) {

        val roomsRef = homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")

        val roomKey = roomsRef.push().key

        if (roomKey == null) {

            onError("Failed to generate room ID")
            return
        }

        val roomWithId = room.copy(
            id = roomKey
        )

        roomsRef
            .child(roomKey)
            .setValue(roomWithId)
            .addOnSuccessListener {

                Log.d(
                    "RoomFirebase",
                    "Room created successfully: $roomKey"
                )

                onSuccess(roomWithId)
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "RoomFirebase",
                    "Failed to create room",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to create room"
                )
            }
    }


    /*
     * GET ALL ROOMS
     */
    fun getRooms(
        homeId: String,
        floorId: String,
        onSuccess: (List<Room>) -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")
            .get()
            .addOnSuccessListener { snapshot ->

                val rooms =
                    snapshot.children.mapNotNull { child ->

                        child.getValue(Room::class.java)
                    }

                Log.d(
                    "RoomFirebase",
                    "Rooms retrieved: ${rooms.size}"
                )

                onSuccess(rooms)
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "RoomFirebase",
                    "Failed to retrieve rooms",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to retrieve rooms"
                )
            }
    }


    /*
     * OBSERVE ROOMS (REAL-TIME)
     *
     * Also derives a lightweight per-room device summary (count,
     * how many are ON, whether any has an active CRITICAL alert)
     * straight from the same snapshot — the "devices" node is
     * nested under each room in Firebase, so no second query is
     * needed to power the room-grid tiles.
     */
    fun observeRooms(
        homeId: String,
        floorId: String,
        onRooms: (List<Room>, Map<String, RoomSummary>) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {

        val roomsRef = homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val rooms = mutableListOf<Room>()
                val summaries = mutableMapOf<String, RoomSummary>()

                for (roomSnapshot in snapshot.children) {

                    val room = roomSnapshot.getValue(Room::class.java)

                    if (room != null) {
                        rooms.add(room)
                    }

                    var deviceCount = 0
                    var devicesOn = 0
                    var hasCriticalAlert = false

                    for (deviceSnapshot in roomSnapshot.child("devices").children) {

                        deviceCount++

                        val isOn =
                            deviceSnapshot.child("on").getValue(Boolean::class.java) ?: false

                        if (isOn) {
                            devicesOn++
                        }

                        val condition =
                            deviceSnapshot.child("condition").getValue(String::class.java)

                        if (condition == "CRITICAL") {
                            hasCriticalAlert = true
                        }
                    }

                    val roomId = roomSnapshot.key

                    if (roomId != null) {

                        summaries[roomId] = RoomSummary(
                            deviceCount = deviceCount,
                            devicesOn = devicesOn,
                            hasCriticalAlert = hasCriticalAlert
                        )
                    }
                }

                Log.d(
                    "RoomFirebase",
                    "Rooms updated (live): ${rooms.size}"
                )

                onRooms(rooms, summaries)
            }

            override fun onCancelled(error: DatabaseError) {

                Log.e(
                    "RoomFirebase",
                    "Room listener cancelled",
                    error.toException()
                )

                onError(error.message)
            }
        }

        roomsRef.addValueEventListener(listener)

        return listener
    }


    /*
     * STOP OBSERVING ROOMS
     */
    fun removeRoomsListener(
        homeId: String,
        floorId: String,
        listener: ValueEventListener
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")
            .removeEventListener(listener)
    }


    /*
     * UPDATE ROOM
     */
    fun updateRoom(
        homeId: String,
        floorId: String,
        room: Room,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")
            .child(room.id)
            .setValue(room)
            .addOnSuccessListener {

                Log.d(
                    "RoomFirebase",
                    "Room updated successfully: ${room.id}"
                )

                onSuccess()
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "RoomFirebase",
                    "Failed to update room",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to update room"
                )
            }
    }


    /*
     * DELETE ROOM
     */
    fun deleteRoom(
        homeId: String,
        floorId: String,
        roomId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .child("rooms")
            .child(roomId)
            .removeValue()
            .addOnSuccessListener {

                Log.d(
                    "RoomFirebase",
                    "Room deleted successfully: $roomId"
                )

                onSuccess()
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "RoomFirebase",
                    "Failed to delete room",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to delete room"
                )
            }
    }
}
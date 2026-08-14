package com.thanu.smarthome.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.thanu.smarthome.model.Room

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
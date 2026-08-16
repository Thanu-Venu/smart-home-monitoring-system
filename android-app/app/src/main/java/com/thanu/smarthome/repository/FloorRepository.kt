package com.thanu.smarthome.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thanu.smarthome.model.Floor

class FloorRepository {

    private val database = FirebaseDatabase.getInstance(
        "https://smart-home-monitoring-sy-f5e88-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    /*
     * homes
     *   └── homeId
     *        └── floors
     */
    private val homesRef =
        database.getReference("homes")


    /*
     * CREATE FLOOR
     */
    fun createFloor(
        homeId: String,
        floor: Floor,
        onSuccess: (Floor) -> Unit,
        onError: (String) -> Unit
    ) {

        Log.d(
            "FirebaseFloor",
            "createFloor() called for home: $homeId"
        )

        val floorId =
            homesRef
                .child(homeId)
                .child("floors")
                .push()
                .key

        if (floorId == null) {

            onError("Failed to generate floor ID")
            return
        }

        val createdFloor = floor.copy(
            id = floorId
        )

        homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .setValue(createdFloor)
            .addOnSuccessListener {

                Log.d(
                    "FirebaseFloor",
                    "Floor created successfully: $createdFloor"
                )

                onSuccess(createdFloor)
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "FirebaseFloor",
                    "Failed to create floor",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to create floor"
                )
            }
    }


    /*
     * GET ALL FLOORS
     */
    fun getFloors(
        homeId: String,
        onSuccess: (List<Floor>) -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .get()
            .addOnSuccessListener { snapshot ->

                val floors = snapshot.children.mapNotNull { child ->

                    child.getValue(Floor::class.java)
                }

                Log.d(
                    "FirebaseFloor",
                    "Floors retrieved: $floors"
                )

                onSuccess(floors)
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "FirebaseFloor",
                    "Failed to retrieve floors",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to retrieve floors"
                )
            }
    }


    /*
     * OBSERVE FLOORS (REAL-TIME)
     */
    fun observeFloors(
        homeId: String,
        onFloors: (List<Floor>) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {

        val floorsRef = homesRef
            .child(homeId)
            .child("floors")

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val floors = snapshot.children.mapNotNull { child ->
                    child.getValue(Floor::class.java)
                }

                Log.d(
                    "FirebaseFloor",
                    "Floors updated (live): ${floors.size}"
                )

                onFloors(floors)
            }

            override fun onCancelled(error: DatabaseError) {

                Log.e(
                    "FirebaseFloor",
                    "Floor listener cancelled",
                    error.toException()
                )

                onError(error.message)
            }
        }

        floorsRef.addValueEventListener(listener)

        return listener
    }


    /*
     * STOP OBSERVING FLOORS
     */
    fun removeFloorsListener(
        homeId: String,
        listener: ValueEventListener
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .removeEventListener(listener)
    }


    /*
     * GET ONE FLOOR
     */
    fun getFloor(
        homeId: String,
        floorId: String,
        onSuccess: (Floor) -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .get()
            .addOnSuccessListener { snapshot ->

                val floor =
                    snapshot.getValue(Floor::class.java)

                if (floor != null) {

                    Log.d(
                        "FirebaseFloor",
                        "Floor retrieved: $floor"
                    )

                    onSuccess(floor)

                } else {

                    onError("Floor not found")
                }
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "FirebaseFloor",
                    "Failed to retrieve floor",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to retrieve floor"
                )
            }
    }


    /*
     * UPDATE FLOOR
     */
    fun updateFloor(
        homeId: String,
        floor: Floor,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .child(floor.id)
            .setValue(floor)
            .addOnSuccessListener {

                Log.d(
                    "FirebaseFloor",
                    "Floor updated successfully"
                )

                onSuccess()
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "FirebaseFloor",
                    "Failed to update floor",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to update floor"
                )
            }
    }


    /*
     * DELETE FLOOR
     */
    fun deleteFloor(
        homeId: String,
        floorId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef
            .child(homeId)
            .child("floors")
            .child(floorId)
            .removeValue()
            .addOnSuccessListener {

                Log.d(
                    "FirebaseFloor",
                    "Floor deleted successfully"
                )

                onSuccess()
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "FirebaseFloor",
                    "Failed to delete floor",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to delete floor"
                )
            }
    }
}
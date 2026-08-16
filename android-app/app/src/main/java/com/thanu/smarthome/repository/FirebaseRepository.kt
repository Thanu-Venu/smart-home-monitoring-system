package com.thanu.smarthome.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.thanu.smarthome.model.DashboardSummary
import com.thanu.smarthome.model.Home

class FirebaseRepository {

    private val database = FirebaseDatabase.getInstance(
        "https://smart-home-monitoring-sy-f5e88-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    private val homesRef = database.getReference("homes")

    fun createHome(
        name: String,
        ownerId: String,
        onSuccess: (Home) -> Unit,
        onError: (String) -> Unit
    ) {

        Log.d("FirebaseTest", "createHome() called")

        val homeKey = homesRef.push().key

        if (homeKey == null) {
            onError("Failed to generate home ID")
            return
        }

        val home = Home(
            id = homeKey,
            name = name,
            ownerId = ownerId
        )

        val homeUpdates = mapOf(
            "id" to home.id,
            "name" to home.name,
            "ownerId" to home.ownerId
        )

        homesRef
            .child(home.id)
            .updateChildren(homeUpdates)
            .addOnSuccessListener {

                Log.d(
                    "FirebaseTest",
                    "Home created successfully: ${home.id}"
                )

                onSuccess(home)
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "FirebaseTest",
                    "Failed to create home",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to create home"
                )
            }
    }

    fun getHome(
        homeId: String,
        onSuccess: (Home) -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef.child(homeId)
            .get()
            .addOnSuccessListener { snapshot ->

                val home = snapshot.getValue(Home::class.java)

                if (home != null) {

                    Log.d(
                        "FirebaseTest",
                        "Home retrieved: $home"
                    )

                    onSuccess(home)

                } else {

                    onError("Home not found")
                }
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "FirebaseTest",
                    "Failed to retrieve home",
                    exception
                )

                onError(
                    exception.message ?: "Failed to retrieve home"
                )
            }
    }

    fun getHomes(
        ownerId: String,
        onSuccess: (List<Home>) -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef
            .orderByChild("ownerId")
            .equalTo(ownerId)
            .get()
            .addOnSuccessListener { snapshot ->

                val homes = mutableListOf<Home>()

                for (childSnapshot in snapshot.children) {

                    val home = childSnapshot.getValue(Home::class.java)

                    if (home != null) {
                        homes.add(home)
                    }
                }

                Log.d(
                    "FirebaseTest",
                    "Homes retrieved: $homes"
                )

                onSuccess(homes)
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "FirebaseTest",
                    "Failed to retrieve homes",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to retrieve homes"
                )
            }
    }

    /*
     * OBSERVE HOMES (REAL-TIME)
     *
     * Live version of getHomes() — keeps the home list in sync
     * automatically instead of requiring a manual refresh. Returns
     * both the listener AND the exact Query it was attached to,
     * since removeEventListener() must be called on that same Query.
     */
    fun observeHomes(
        ownerId: String,
        onHomes: (List<Home>, DashboardSummary) -> Unit,
        onError: (String) -> Unit
    ): Pair<Query, ValueEventListener> {

        val query = homesRef
            .orderByChild("ownerId")
            .equalTo(ownerId)

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val homes = mutableListOf<Home>()

                for (childSnapshot in snapshot.children) {

                    val home = childSnapshot.getValue(Home::class.java)

                    if (home != null) {
                        homes.add(home)
                    }
                }

                Log.d(
                    "FirebaseTest",
                    "Homes updated (live): ${homes.size}"
                )

                onHomes(
                    homes,
                    buildDashboardSummary(snapshot)
                )
            }

            override fun onCancelled(error: DatabaseError) {

                Log.e(
                    "FirebaseTest",
                    "Home listener cancelled",
                    error.toException()
                )

                onError(error.message)
            }
        }

        query.addValueEventListener(listener)

        return query to listener
    }


    /*
     * BUILD DASHBOARD SUMMARY
     *
     * Walks floors -> rooms -> devices across every home in this
     * snapshot and totals them up. Each home node returned by the
     * ownerId query above already carries its full nested subtree,
     * so this is free — no extra Firebase read beyond the one the
     * home list itself already needed.
     */
    private fun buildDashboardSummary(
        homesSnapshot: DataSnapshot
    ): DashboardSummary {

        var floorsCount = 0
        var roomsCount = 0
        var devicesCount = 0
        var devicesOn = 0

        for (homeSnapshot in homesSnapshot.children) {

            val floorsSnapshot = homeSnapshot.child("floors")

            for (floorSnapshot in floorsSnapshot.children) {

                floorsCount++

                val roomsSnapshot = floorSnapshot.child("rooms")

                for (roomSnapshot in roomsSnapshot.children) {

                    roomsCount++

                    for (deviceSnapshot in roomSnapshot.child("devices").children) {

                        devicesCount++

                        val isOn =
                            deviceSnapshot.child("on").getValue(Boolean::class.java) ?: false

                        if (isOn) {
                            devicesOn++
                        }
                    }
                }
            }
        }

        return DashboardSummary(
            floorsCount = floorsCount,
            roomsCount = roomsCount,
            devicesCount = devicesCount,
            devicesOn = devicesOn
        )
    }


    /*
     * STOP OBSERVING HOMES
     */
    fun removeHomesListener(
        query: Query,
        listener: ValueEventListener
    ) {

        query.removeEventListener(listener)
    }


    fun updateHome(
        home: Home,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val homeUpdates = mapOf(
            "id" to home.id,
            "name" to home.name,
            "ownerId" to home.ownerId
        )

        homesRef
            .child(home.id)
            .updateChildren(homeUpdates)
            .addOnSuccessListener {

                Log.d(
                    "FirebaseTest",
                    "Home updated successfully"
                )

                onSuccess()
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "FirebaseTest",
                    "Failed to update home",
                    exception
                )

                onError(
                    exception.message
                        ?: "Failed to update home"
                )
            }
    }

    fun deleteHome(
        homeId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        homesRef.child(homeId)
            .removeValue()
            .addOnSuccessListener {

                Log.d(
                    "FirebaseTest",
                    "Home deleted successfully"
                )

                onSuccess()
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "FirebaseTest",
                    "Failed to delete home",
                    exception
                )

                onError(
                    exception.message ?: "Failed to delete home"
                )
            }
    }
}
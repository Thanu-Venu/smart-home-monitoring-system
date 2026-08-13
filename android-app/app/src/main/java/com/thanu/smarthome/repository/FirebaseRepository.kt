package com.thanu.smarthome.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
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
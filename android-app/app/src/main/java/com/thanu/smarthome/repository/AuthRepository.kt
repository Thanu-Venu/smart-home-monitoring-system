package com.thanu.smarthome.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    /*
     * Currently signed-in Firebase user, or null if signed out.
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /*
     * Convenience accessor used as the "ownerId" for homes created
     * by the signed-in user. Empty when nobody is signed in.
     */
    val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    fun isLoggedIn(): Boolean =
        auth.currentUser != null


    /*
     * SIGN UP
     */
    fun signUp(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val user = result.user

                if (user != null) {

                    Log.d(
                        "AuthRepository",
                        "Sign up successful: ${user.uid}"
                    )

                    onSuccess(user)

                } else {

                    onError("Sign up failed. Please try again.")
                }
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "AuthRepository",
                    "Sign up failed",
                    exception
                )

                onError(
                    exception.message
                        ?: "Sign up failed"
                )
            }
    }


    /*
     * SIGN IN
     */
    fun signIn(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val user = result.user

                if (user != null) {

                    Log.d(
                        "AuthRepository",
                        "Sign in successful: ${user.uid}"
                    )

                    onSuccess(user)

                } else {

                    onError("Sign in failed. Please try again.")
                }
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "AuthRepository",
                    "Sign in failed",
                    exception
                )

                onError(
                    exception.message
                        ?: "Invalid email or password"
                )
            }
    }


    /*
     * SIGN OUT
     */
    fun signOut() {

        auth.signOut()

        Log.d(
            "AuthRepository",
            "Signed out"
        )
    }
}

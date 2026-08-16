package com.thanu.smarthome.model

data class AuthUiState(

    // Loading state (sign in / sign up in progress)
    val isLoading: Boolean = false,

    // Whether a Firebase user is currently signed in
    val isAuthenticated: Boolean = false,

    // Signed-in user's email, if any
    val userEmail: String? = null,

    // Success message
    val message: String? = null,

    // Error message
    val errorMessage: String? = null
)

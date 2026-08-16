package com.thanu.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import com.thanu.smarthome.model.AuthUiState
import com.thanu.smarthome.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isAuthenticated = repository.isLoggedIn(),
            userEmail = repository.currentUser?.email
        )
    )

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()


    /*
     * SIGN UP
     */
    fun signUp(
        email: String,
        password: String,
        confirmPassword: String
    ) {

        if (
            email.isBlank() ||
            password.isBlank()
        ) {

            _uiState.value = _uiState.value.copy(
                errorMessage = "Email and password are required"
            )
            return
        }

        if (password.length < 6) {

            _uiState.value = _uiState.value.copy(
                errorMessage = "Password must be at least 6 characters"
            )
            return
        }

        if (password != confirmPassword) {

            _uiState.value = _uiState.value.copy(
                errorMessage = "Passwords do not match"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.signUp(
            email = email.trim(),
            password = password,

            onSuccess = { user ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    userEmail = user.email,
                    message = "Account created successfully!",
                    errorMessage = null
                )
            },

            onError = { message ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = null,
                    errorMessage = message
                )
            }
        )
    }


    /*
     * SIGN IN
     */
    fun signIn(
        email: String,
        password: String
    ) {

        if (
            email.isBlank() ||
            password.isBlank()
        ) {

            _uiState.value = _uiState.value.copy(
                errorMessage = "Email and password are required"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.signIn(
            email = email.trim(),
            password = password,

            onSuccess = { user ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    userEmail = user.email,
                    errorMessage = null
                )
            },

            onError = { message ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = null,
                    errorMessage = message
                )
            }
        )
    }


    /*
     * SIGN OUT
     */
    fun signOut() {

        repository.signOut()

        _uiState.value = AuthUiState(
            isAuthenticated = false
        )
    }


    /*
     * Clears any error banner, e.g. as the user starts typing again.
     */
    fun clearError() {

        if (_uiState.value.errorMessage != null) {

            _uiState.value = _uiState.value.copy(
                errorMessage = null
            )
        }
    }
}

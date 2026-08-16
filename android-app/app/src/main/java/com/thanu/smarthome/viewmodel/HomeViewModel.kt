package com.thanu.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.thanu.smarthome.model.Home
import com.thanu.smarthome.model.HomeUiState
import com.thanu.smarthome.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var homesQuery: Query? = null
    private var homesListener: ValueEventListener? = null


    /*
     * START LISTENING (REAL-TIME)
     */
    fun startListening(ownerId: String) {

        stopListening()

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        val (query, listener) = repository.observeHomes(
            ownerId = ownerId,

            onHomes = { homes ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    homes = homes,
                    errorMessage = null
                )
            },

            onError = { message ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
            }
        )

        homesQuery = query
        homesListener = listener
    }


    /*
     * STOP LISTENING
     */
    fun stopListening() {

        val query = homesQuery
        val listener = homesListener

        if (query != null && listener != null) {

            repository.removeHomesListener(
                query = query,
                listener = listener
            )
        }

        homesQuery = null
        homesListener = null
    }


    override fun onCleared() {
        super.onCleared()
        stopListening()
    }


    /*
     * CREATE HOME
     */
    fun createHome(
        name: String,
        ownerId: String
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.createHome(
            name = name,
            ownerId = ownerId,

            onSuccess = { home ->

                /*
                 * Don't append `home` to the local list here.
                 * A real-time listener (startListening) is already
                 * attached on this screen and will receive this same
                 * write from Firebase and update `homes` on its own.
                 * Appending it here too would create a duplicate card.
                 */

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedHome = home,
                    message = "Home Created Successfully!",
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
     * GET ALL HOMES
     */
    fun getHomes(ownerId: String) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.getHomes(

            ownerId = ownerId,

            onSuccess = { homes ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    homes = homes,
                    message = null,
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
     * SELECT HOME
     */
    fun selectHome(home: Home) {

        _uiState.value = _uiState.value.copy(
            selectedHome = home,
            message = null,
            errorMessage = null
        )
    }


    /*
     * UPDATE HOME
     */
    fun updateHome(home: Home) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.updateHome(

            home = home,

            onSuccess = {

                val updatedHomes =
                    _uiState.value.homes.map { existingHome ->

                        if (existingHome.id == home.id) {
                            home
                        } else {
                            existingHome
                        }
                    }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    homes = updatedHomes,
                    selectedHome = home,
                    message = "Home Updated Successfully!",
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
     * DELETE HOME
     */
    fun deleteHome(homeId: String) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.deleteHome(

            homeId = homeId,

            onSuccess = {

                val updatedHomes =
                    _uiState.value.homes.filter { home ->
                        home.id != homeId
                    }

                val selectedHome =
                    if (_uiState.value.selectedHome?.id == homeId) {
                        null
                    } else {
                        _uiState.value.selectedHome
                    }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    homes = updatedHomes,
                    selectedHome = selectedHome,
                    message = "Home Deleted Successfully!",
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
}
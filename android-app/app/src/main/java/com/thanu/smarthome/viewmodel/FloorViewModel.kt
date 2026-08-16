package com.thanu.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.ValueEventListener
import com.thanu.smarthome.model.Floor
import com.thanu.smarthome.model.FloorUiState
import com.thanu.smarthome.repository.FloorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FloorViewModel : ViewModel() {

    private val repository = FloorRepository()

    private val _uiState = MutableStateFlow(FloorUiState())

    val uiState: StateFlow<FloorUiState> =
        _uiState.asStateFlow()

    private var floorsListener: ValueEventListener? = null
    private var listeningHomeId: String? = null


    /*
     * START LISTENING (REAL-TIME)
     */
    fun startListening(homeId: String) {

        if (
            floorsListener != null &&
            listeningHomeId == homeId
        ) {
            return
        }

        stopListening()

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        listeningHomeId = homeId

        floorsListener = repository.observeFloors(
            homeId = homeId,

            onFloors = { floors ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    floors = floors,
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
    }


    /*
     * STOP LISTENING
     */
    fun stopListening() {

        val listener = floorsListener
        val homeId = listeningHomeId

        if (listener != null && homeId != null) {

            repository.removeFloorsListener(
                homeId = homeId,
                listener = listener
            )
        }

        floorsListener = null
    }


    override fun onCleared() {
        super.onCleared()
        stopListening()
    }


    /*
     * CREATE FLOOR
     */
    fun createFloor(
        homeId: String,
        floor: Floor
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.createFloor(
            homeId = homeId,
            floor = floor,

            onSuccess = { createdFloor ->

                /*
                 * Don't append createdFloor to the local list here.
                 * A real-time listener (startListening) is already
                 * attached on this screen and will receive this same
                 * write from Firebase and update `floors` on its own.
                 * Appending it here too would create a duplicate card
                 * for a brief moment (or permanently, depending on
                 * event ordering).
                 */

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    floor = createdFloor,
                    message = "Floor created successfully!",
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
     * GET ALL FLOORS
     */
    fun getFloors(
        homeId: String
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.getFloors(

            homeId = homeId,

            onSuccess = { floors ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    floors = floors,
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
     * GET ONE FLOOR
     */
    fun getFloor(
        homeId: String,
        floorId: String
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.getFloor(

            homeId = homeId,
            floorId = floorId,

            onSuccess = { floor ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    floor = floor,
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
     * UPDATE FLOOR
     */
    fun updateFloor(
        homeId: String,
        floor: Floor
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.updateFloor(

            homeId = homeId,
            floor = floor,

            onSuccess = {

                val updatedFloors =
                    _uiState.value.floors.map { existingFloor ->

                        if (existingFloor.id == floor.id) {
                            floor
                        } else {
                            existingFloor
                        }
                    }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    floors = updatedFloors,
                    floor = floor,
                    message = "Floor updated successfully!",
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
     * DELETE FLOOR
     */
    fun deleteFloor(
        homeId: String,
        floorId: String
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.deleteFloor(

            homeId = homeId,
            floorId = floorId,

            onSuccess = {

                val updatedFloors =
                    _uiState.value.floors.filter { floor ->
                        floor.id != floorId
                    }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    floors = updatedFloors,
                    floor = null,
                    message = "Floor deleted successfully!",
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
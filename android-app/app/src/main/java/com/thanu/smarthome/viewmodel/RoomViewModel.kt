package com.thanu.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import com.thanu.smarthome.model.Room
import com.thanu.smarthome.model.RoomUiState
import com.thanu.smarthome.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RoomViewModel : ViewModel() {

    private val repository = RoomRepository()

    private val _uiState = MutableStateFlow(RoomUiState())

    val uiState: StateFlow<RoomUiState> =
        _uiState.asStateFlow()


    /*
     * CREATE ROOM
     */
    fun createRoom(
        homeId: String,
        floorId: String,
        room: Room
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.createRoom(
            homeId = homeId,
            floorId = floorId,
            room = room,

            onSuccess = { createdRoom ->

                val updatedRooms =
                    _uiState.value.rooms + createdRoom

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    rooms = updatedRooms,
                    room = createdRoom,
                    message = "Room created successfully!",
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
     * GET ROOMS
     */
    fun getRooms(
        homeId: String,
        floorId: String
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.getRooms(

            homeId = homeId,
            floorId = floorId,

            onSuccess = { rooms ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    rooms = rooms,
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
     * UPDATE ROOM
     */
    fun updateRoom(
        homeId: String,
        floorId: String,
        room: Room
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.updateRoom(

            homeId = homeId,
            floorId = floorId,
            room = room,

            onSuccess = {

                val updatedRooms =
                    _uiState.value.rooms.map { existingRoom ->

                        if (existingRoom.id == room.id) {
                            room
                        } else {
                            existingRoom
                        }
                    }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    rooms = updatedRooms,
                    room = room,
                    message = "Room updated successfully!",
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
     * DELETE ROOM
     */
    fun deleteRoom(
        homeId: String,
        floorId: String,
        roomId: String
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.deleteRoom(

            homeId = homeId,
            floorId = floorId,
            roomId = roomId,

            onSuccess = {

                val updatedRooms =
                    _uiState.value.rooms.filter { room ->
                        room.id != roomId
                    }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    rooms = updatedRooms,
                    room = null,
                    message = "Room deleted successfully!",
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
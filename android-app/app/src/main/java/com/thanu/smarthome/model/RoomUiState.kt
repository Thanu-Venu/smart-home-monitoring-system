package com.thanu.smarthome.model

data class RoomUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val room: Room? = null,
    val rooms: List<Room> = emptyList(),
    val errorMessage: String? = null
)
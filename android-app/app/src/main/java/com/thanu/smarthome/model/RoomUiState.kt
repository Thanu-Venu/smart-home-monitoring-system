package com.thanu.smarthome.model

data class RoomUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val room: Room? = null,
    val rooms: List<Room> = emptyList(),

    // roomId -> live device summary, used by the room grid tiles.
    val roomSummaries: Map<String, RoomSummary> = emptyMap(),

    val errorMessage: String? = null
)
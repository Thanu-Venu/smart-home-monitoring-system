package com.thanu.smarthome.model

data class FloorUiState(

    val isLoading: Boolean = false,

    val floors: List<Floor> = emptyList(),

    val floor: Floor? = null,

    val message: String? = null,

    val errorMessage: String? = null
)
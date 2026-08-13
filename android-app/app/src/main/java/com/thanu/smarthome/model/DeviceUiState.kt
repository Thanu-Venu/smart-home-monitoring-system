package com.thanu.smarthome.model

data class DeviceUiState(

    // Loading state
    val isLoading: Boolean = false,

    // List of devices in the selected room
    val devices: List<Device> = emptyList(),

    // Currently selected/created device
    val device: Device? = null,

    // Success message
    val message: String? = null,

    // Error message
    val errorMessage: String? = null
)
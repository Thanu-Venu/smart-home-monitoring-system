package com.thanu.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import com.thanu.smarthome.model.Device
import com.thanu.smarthome.model.DeviceUiState
import com.thanu.smarthome.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceViewModel : ViewModel() {

    private val repository = DeviceRepository()

    private val _uiState =
        MutableStateFlow(DeviceUiState())

    val uiState: StateFlow<DeviceUiState> =
        _uiState.asStateFlow()


    /*
     * CREATE DEVICE
     */
    fun createDevice(
        homeId: String,
        floorId: String,
        roomId: String,
        device: Device
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.createDevice(
            homeId = homeId,
            floorId = floorId,
            roomId = roomId,
            device = device,

            onSuccess = { createdDevice ->

                val updatedDevices =
                    _uiState.value.devices + createdDevice

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    devices = updatedDevices,
                    device = createdDevice,
                    message = "Device created successfully!",
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
     * GET DEVICES
     */
    fun getDevices(
        homeId: String,
        floorId: String,
        roomId: String
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.getDevices(
            homeId = homeId,
            floorId = floorId,
            roomId = roomId,

            onSuccess = { devices ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    devices = devices,
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
     * UPDATE DEVICE
     */
    fun updateDevice(
        homeId: String,
        floorId: String,
        roomId: String,
        device: Device
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.updateDevice(
            homeId = homeId,
            floorId = floorId,
            roomId = roomId,
            device = device,

            onSuccess = {

                val updatedDevices =
                    _uiState.value.devices.map { existingDevice ->

                        if (existingDevice.id == device.id) {
                            device
                        } else {
                            existingDevice
                        }
                    }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    devices = updatedDevices,
                    device = device,
                    message = "Device updated successfully!",
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
     * DELETE DEVICE
     */
    fun deleteDevice(
        homeId: String,
        floorId: String,
        roomId: String,
        deviceId: String
    ) {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        repository.deleteDevice(
            homeId = homeId,
            floorId = floorId,
            roomId = roomId,
            deviceId = deviceId,

            onSuccess = {

                val updatedDevices =
                    _uiState.value.devices.filter { device ->
                        device.id != deviceId
                    }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    devices = updatedDevices,
                    device = null,
                    message = "Device deleted successfully!",
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
     * TOGGLE DEVICE
     *
     * This will be useful for the Smart Home
     * ON/OFF control later.
     */
    fun toggleDevice(
        homeId: String,
        floorId: String,
        roomId: String,
        device: Device
    ) {

        val updatedDevice = device.copy(
            on = !device.on,
            status = if (!device.on) {
                "ON"
            } else {
                "OFF"
            }
        )

        updateDevice(
            homeId = homeId,
            floorId = floorId,
            roomId = roomId,
            device = updatedDevice
        )
    }
}
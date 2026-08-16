package com.thanu.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.ValueEventListener
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
     * Live device listener bookkeeping, so we can detach it
     * (removeDevicesListener) once this ViewModel is cleared.
     */
    private var devicesListener: ValueEventListener? = null
    private var listeningHomeId: String? = null
    private var listeningFloorId: String? = null
    private var listeningRoomId: String? = null


    /*
     * START LISTENING (REAL-TIME)
     *
     * Replaces the one-off getDevices() fetch for screens that need
     * to stay in sync automatically, e.g. DeviceScreen.
     */
    fun startListening(
        homeId: String,
        floorId: String,
        roomId: String
    ) {

        if (
            devicesListener != null &&
            listeningHomeId == homeId &&
            listeningFloorId == floorId &&
            listeningRoomId == roomId
        ) {
            // Already listening to this exact room, nothing to do.
            return
        }

        stopListening()

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            message = null,
            errorMessage = null
        )

        listeningHomeId = homeId
        listeningFloorId = floorId
        listeningRoomId = roomId

        devicesListener = repository.observeDevices(
            homeId = homeId,
            floorId = floorId,
            roomId = roomId,

            onDevices = { devices ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    devices = devices,
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

        val listener = devicesListener
        val homeId = listeningHomeId
        val floorId = listeningFloorId
        val roomId = listeningRoomId

        if (
            listener != null &&
            homeId != null &&
            floorId != null &&
            roomId != null
        ) {

            repository.removeDevicesListener(
                homeId = homeId,
                floorId = floorId,
                roomId = roomId,
                listener = listener
            )
        }

        devicesListener = null
    }


    override fun onCleared() {
        super.onCleared()
        stopListening()
    }


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

        val turningOn = !device.on

        val updatedDevice = device.copy(
            on = turningOn,
            status = if (turningOn) {
                "ON"
            } else {
                "OFF"
            },

            /*
             * SAFETY CUTOFF TRACKING
             *
             * Stamp the moment a duration-limited device (e.g. iron) is
             * switched ON so SafetyMonitor can enforce maxOnDurationMinutes.
             * Manually switching a device OFF/ON also clears any previous
             * CRITICAL alert, since the user has taken control of it again.
             */
            turnedOnAt = if (turningOn) {
                System.currentTimeMillis()
            } else {
                0L
            },
            condition = "NORMAL",
            alert = ""
        )

        updateDevice(
            homeId = homeId,
            floorId = floorId,
            roomId = roomId,
            device = updatedDevice
        )
    }
}
package com.thanu.smarthome.ui

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thanu.smarthome.model.Device
import com.thanu.smarthome.model.DeviceSwitch
import com.thanu.smarthome.model.DeviceType
import com.thanu.smarthome.viewmodel.DeviceViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.mutableIntStateOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScreen(
    homeId: String,
    floorId: String,
    roomId: String,
    roomName: String = "Room",
    onBack: () -> Unit,
    deviceViewModel: DeviceViewModel = viewModel()
) {

    val uiState by deviceViewModel.uiState.collectAsState()

    /*
     * ------------------------------------------------
     * CREATE DEVICE STATE
     * ------------------------------------------------
     */

    var showCreateDialog by remember {
        mutableStateOf(false)
    }

    var selectedDeviceType by remember {
        mutableStateOf<DeviceType?>(null)
    }

    var deviceTypeExpanded by remember {
        mutableStateOf(false)
    }

    var selectedDeviceName by remember {
        mutableStateOf("")
    }

    var deviceNameExpanded by remember {
        mutableStateOf(false)
    }

    var customDeviceName by remember {
        mutableStateOf("")
    }

    /*
     * LIGHT SETTINGS
     */

    var scheduleEnabled by remember {
        mutableStateOf(false)
    }

    var scheduleStart by remember {
        mutableStateOf("")
    }

    var scheduleEnd by remember {
        mutableStateOf("")
    }

    var showStartTimePicker by remember {
        mutableStateOf(false)
    }

    var showEndTimePicker by remember {
        mutableStateOf(false)
    }

    /*
     * IRON SETTINGS
     */

    var maxOnDuration by remember {
        mutableIntStateOf(15)
    }

    var ironDurationExpanded by remember {
        mutableStateOf(false)
    }

    /*
     * MULTI-SWITCH SETTINGS
     */

    var switchCount by remember {
        mutableIntStateOf(2)
    }

    /*
     * CAMERA SETTINGS
     */

    var cameraUri by remember {
        mutableStateOf("")
    }


    /*
     * ------------------------------------------------
     * EDIT DEVICE STATE
     * ------------------------------------------------
     */

    var editingDevice by remember {
        mutableStateOf<Device?>(null)
    }

    var editDeviceName by remember {
        mutableStateOf("")
    }

    var editDeviceType by remember {
        mutableStateOf("")
    }

    var editScheduleEnabled by remember {
        mutableStateOf(false)
    }

    var editScheduleStart by remember {
        mutableStateOf("")
    }

    var editScheduleEnd by remember {
        mutableStateOf("")
    }

    /*
     * Both stored the same way the create-dialog versions are
     * (Int, clamped by UI controls rather than free text) so editing
     * a device can't produce a value creating one never could — see
     * the Iron/Multi-Switch settings blocks in the edit dialog below.
     */
    var editMaxOnDuration by remember {
        mutableIntStateOf(15)
    }

    var editIronDurationExpanded by remember {
        mutableStateOf(false)
    }

    var editSwitchCount by remember {
        mutableIntStateOf(2)
    }

    var editCameraUri by remember {
        mutableStateOf("")
    }

    var showEditStartTimePicker by remember {
        mutableStateOf(false)
    }

    var showEditEndTimePicker by remember {
        mutableStateOf(false)
    }


    /*
     * ------------------------------------------------
     * DELETE DEVICE
     * ------------------------------------------------
     */

    var deletingDevice by remember {
        mutableStateOf<Device?>(null)
    }


    /*
     * ------------------------------------------------
     * CAMERA PREVIEW
     * ------------------------------------------------
     */

    var viewingCameraUri by remember {
        mutableStateOf<String?>(null)
    }


    /*
     * ------------------------------------------------
     * LOAD DEVICES
     * ------------------------------------------------
     */

    LaunchedEffect(
        homeId,
        floorId,
        roomId
    ) {

        /*
         * Real-time listener instead of a one-off fetch, so toggles
         * made elsewhere (web simulator, another device) and the
         * SafetyMonitor's automatic cutoffs show up instantly here.
         */
        deviceViewModel.startListening(
            homeId = homeId,
            floorId = floorId,
            roomId = roomId
        )
    }


    /*
 * ------------------------------------------------
 * ROOM-AWARE DEVICE NAME OPTIONS
 * ------------------------------------------------
 */

    val deviceNameOptions = when (selectedDeviceType) {

        DeviceType.LIGHT -> listOf(
            "$roomName Light",
            "$roomName Ceiling Light",
            "$roomName Table Lamp",
            "$roomName Wall Light",
            "Other"
        )

        DeviceType.OUTLET -> listOf(
            "$roomName Outlet",
            "$roomName Power Outlet",
            "$roomName Appliance Outlet",
            "Other"
        )

        DeviceType.MULTI_SWITCH -> listOf(
            "$roomName Switch",
            "$roomName Wall Switch",
            "$roomName Multi Switch",
            "Other"
        )

        DeviceType.IRON -> listOf(
            "Clothing Iron",
            "Steam Iron",
            "Other"
        )

        DeviceType.CAMERA -> listOf(
            "$roomName Camera",
            "$roomName Security Camera",
            "$roomName Monitor Camera",
            "Other"
        )

        DeviceType.OTHER -> listOf(
            "Other"
        )

        null -> emptyList()
    }

    val ironDurationOptions = listOf(
        5,
        10,
        15,
        20,
        30,
        45,
        60
    )
    /*
     * ------------------------------------------------
     * MAIN SCREEN
     * ------------------------------------------------
     */

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        /*
         * HEADER
         */

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Column {

                        Text(
                            text = roomName,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            text = "Devices",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }


                /*
                 * ADD DEVICE
                 */

                IconButton(
                    onClick = {

                        selectedDeviceType = null
                        selectedDeviceName = ""
                        customDeviceName = ""

                        scheduleEnabled = false
                        scheduleStart = ""
                        scheduleEnd = ""

                        maxOnDuration = 15

                        switchCount = 2

                        cameraUri = ""

                        showCreateDialog = true
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Device"
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }


        /*
         * EMPTY STATE
         */

        if (
            uiState.devices.isEmpty() &&
            !uiState.isLoading
        ) {

            item {

                /*
                 * Wrapped in a Column: multiple direct Spacer/Text
                 * children in a single LazyColumn item slot have no
                 * arrangement of their own, so without this they
                 * render stacked on top of each other instead of
                 * top-to-bottom.
                 */
                Column {

                    Spacer(
                        modifier = Modifier.height(32.dp)
                    )

                    Text(
                        text = "No devices yet",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Tap + to add a device."
                    )
                }
            }
        }


        /*
         * DEVICE LIST
         */

        items(
            uiState.devices,
            key = { device -> device.id }
        ) { device ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    /*
                     * Disabled while ERROR/DISCONNECTED — a device
                     * that isn't reachable can't be toggled (as a
                     * whole, or switch-by-switch) until it's
                     * reconnected via the overflow menu. Declared
                     * here (not inside the Row below) so it's also
                     * visible to the per-switch controls further
                     * down in the device-specific section.
                     */
                    val isControllable =
                        device.status != "ERROR" &&
                                device.status != "DISCONNECTED"

                    /*
                     * Multi-Switch devices don't have a directly
                     * user-settable "on" of their own — it's derived
                     * from whichever individual switches (below) are
                     * on. So the top switch becomes a read-only "is
                     * anything in this gang-box on?" indicator for
                     * that type, and the real controls are the
                     * per-switch rows in the device-specific section.
                     */
                    val isMultiSwitch =
                        device.type == "MULTI_SWITCH"


                    /*
                     * DEVICE INFORMATION
                     */

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = device.type,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = "Status: ${device.status}",
                                style = MaterialTheme.typography.bodyMedium,
                                /*
                                 * Status is never conveyed by color
                                 * alone — this text label is always
                                 * present too — but distinct colors
                                 * per state (rather than just
                                 * error/neutral) make the four-state
                                 * model easier to scan at a glance.
                                 */
                                color = when (device.status) {
                                    "ON" -> MaterialTheme.colorScheme.tertiary
                                    "ERROR" -> MaterialTheme.colorScheme.error
                                    "DISCONNECTED" -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }


                        /*
                         * BASIC ON/OFF CONTROL
                         */

                        Switch(
                            checked = device.on,
                            onCheckedChange = {

                                deviceViewModel.toggleDevice(
                                    homeId = homeId,
                                    floorId = floorId,
                                    roomId = roomId,
                                    device = device
                                )
                            },
                            enabled =
                                !uiState.isLoading &&
                                        isControllable &&
                                        !isMultiSwitch
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )


                    /*
                     * DEVICE-SPECIFIC INFORMATION
                     */

                    when (device.type) {

                        "LIGHT" -> {

                            if (device.scheduleEnabled) {

                                Text(
                                    text = "Schedule: ${device.scheduleStart} - ${device.scheduleEnd}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        "IRON" -> {

                            if (device.maxOnDurationMinutes > 0) {

                                Text(
                                    text = "Maximum ON time: ${device.maxOnDurationMinutes} min",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        "MULTI_SWITCH" -> {

                            /*
                             * INDIVIDUALLY ADDRESSABLE SWITCHES
                             *
                             * Each switch in this gang-box gets its own
                             * row and its own Switch control — they're
                             * separate addressable entities in Firebase
                             * (device.switches), not just a count.
                             */

                            Text(
                                text = "${device.switches.size} switch" +
                                        if (device.switches.size == 1) "" else "es",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            device.switches.forEach { deviceSwitch ->

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = deviceSwitch.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Switch(
                                        checked = deviceSwitch.on,
                                        onCheckedChange = {

                                            deviceViewModel.toggleSwitch(
                                                homeId = homeId,
                                                floorId = floorId,
                                                roomId = roomId,
                                                device = device,
                                                switchId = deviceSwitch.id
                                            )
                                        },
                                        enabled =
                                            !uiState.isLoading && isControllable
                                    )
                                }
                            }
                        }

                        "CAMERA" -> {

                            if (device.cameraUri.isNotBlank()) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = "Camera configured",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Spacer(
                                        modifier = Modifier.width(8.dp)
                                    )

                                    TextButton(
                                        onClick = {
                                            viewingCameraUri = device.cameraUri
                                        }
                                    ) {

                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = "View ${device.name} stream",
                                            modifier = Modifier.size(16.dp)
                                        )

                                        Spacer(
                                            modifier = Modifier.width(4.dp)
                                        )

                                        Text("View")
                                    }
                                }

                            } else {

                                Text(
                                    text = "No camera URI configured",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }


                    /*
                     * SAFETY ALERT
                     *
                     * Shown when SafetyMonitor has auto-switched this
                     * device off for exceeding its maxOnDurationMinutes.
                     */

                    if (
                        device.condition == "CRITICAL" &&
                        device.alert.isNotBlank()
                    ) {

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "⚠ ${device.alert}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }


                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )


                    /*
                     * DEVICE ID
                     */

                    Text(
                        text = "ID: ${device.id}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )


                    /*
                     * ACTIONS
                     */

                    var showStatusMenu by remember(device.id) {
                        mutableStateOf(false)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {

                        /*
                         * SIMULATE CONNECTIVITY STATUS
                         *
                         * There's no real hardware feeding ERROR /
                         * DISCONNECTED states, so this lets the demo
                         * show all four statuses the spec asks for
                         * (ON, OFF, ERROR, DISCONNECTED).
                         */

                        Box {

                            IconButton(
                                onClick = {
                                    showStatusMenu = true
                                },
                                enabled = !uiState.isLoading
                            ) {

                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More actions for ${device.name}"
                                )
                            }

                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = {
                                    showStatusMenu = false
                                }
                            ) {

                                if (
                                    device.status == "ERROR" ||
                                    device.status == "DISCONNECTED"
                                ) {

                                    DropdownMenuItem(
                                        text = {
                                            Text("Reconnect (back to normal)")
                                        },
                                        onClick = {

                                            deviceViewModel.setDeviceStatus(
                                                homeId = homeId,
                                                floorId = floorId,
                                                roomId = roomId,
                                                device = device,
                                                newStatus = "NORMAL"
                                            )

                                            showStatusMenu = false
                                        }
                                    )

                                } else {

                                    DropdownMenuItem(
                                        text = {
                                            Text("Simulate: Disconnected")
                                        },
                                        onClick = {

                                            deviceViewModel.setDeviceStatus(
                                                homeId = homeId,
                                                floorId = floorId,
                                                roomId = roomId,
                                                device = device,
                                                newStatus = "DISCONNECTED"
                                            )

                                            showStatusMenu = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text("Simulate: Error")
                                        },
                                        onClick = {

                                            deviceViewModel.setDeviceStatus(
                                                homeId = homeId,
                                                floorId = floorId,
                                                roomId = roomId,
                                                device = device,
                                                newStatus = "ERROR"
                                            )

                                            showStatusMenu = false
                                        }
                                    )
                                }
                            }
                        }


                        /*
                         * EDIT
                         */

                        IconButton(
                            onClick = {

                                editingDevice = device

                                editDeviceName = device.name
                                editDeviceType = device.type

                                editScheduleEnabled = device.scheduleEnabled
                                editScheduleStart = device.scheduleStart
                                editScheduleEnd = device.scheduleEnd

                                editMaxOnDuration =
                                    if (device.maxOnDurationMinutes > 0) {
                                        device.maxOnDurationMinutes
                                    } else {
                                        // Same default the create dialog
                                        // starts a new Iron at.
                                        15
                                    }

                                editIronDurationExpanded = false

                                editSwitchCount =
                                    if (device.switchCount in 2..5) {
                                        device.switchCount
                                    } else {
                                        // Same default the create dialog
                                        // starts a new Multi-Switch at —
                                        // covers any pre-existing device
                                        // saved before this range was
                                        // enforced everywhere.
                                        2
                                    }

                                editCameraUri = device.cameraUri

                                showEditStartTimePicker = false
                                showEditEndTimePicker = false},
                            enabled = !uiState.isLoading
                        ) {

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit ${device.name}"
                            )
                        }


                        /*
                         * DELETE
                         */

                        IconButton(
                            onClick = {

                                deletingDevice = device
                            },
                            enabled = !uiState.isLoading
                        ) {

                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete ${device.name}"
                            )
                        }
                    }
                }
            }
        }


        /*
         * LOADING
         */

        item {

            if (uiState.isLoading) {

                Column {

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    CircularProgressIndicator()
                }
            }
        }


        /*
         * SUCCESS / ERROR MESSAGE
         */

        item {

            Column {

                uiState.message?.let { message ->

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = message
                    )
                }

                uiState.errorMessage?.let { message ->

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "Error: $message"
                    )
                }
            }
        }
    }

    if (showStartTimePicker) {

        val (startHour, startMinute) = parseTime(scheduleStart)

        val timePickerState = rememberTimePickerState(
            initialHour = startHour,
            initialMinute = startMinute,
            is24Hour = false
        )

        TimePickerDialog(
            onDismissRequest = {
                showStartTimePicker = false
            },

            title = {
                Text("Select Start Time")
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        scheduleStart = String.format(
                            java.util.Locale.getDefault(),
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )

                        showStartTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showStartTimePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {

            TimePicker(
                state = timePickerState
            )
        }
    }

    if (showEndTimePicker) {

        val (endHour, endMinute) = parseTime(scheduleEnd)

        val timePickerState = rememberTimePickerState(
            initialHour = endHour,
            initialMinute = endMinute,
            is24Hour = false
        )

        TimePickerDialog(
            onDismissRequest = {
                showEndTimePicker = false
            },

            title = {
                Text("Select End Time")
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        scheduleEnd = String.format(
                            java.util.Locale.getDefault(),
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )

                        showEndTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showEndTimePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {

            TimePicker(
                state = timePickerState
            )
        }
    }

    if (showEditStartTimePicker) {

        val (hour, minute) = parseTime(editScheduleStart)

        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = false
        )

        TimePickerDialog(
            onDismissRequest = {
                showEditStartTimePicker = false
            },

            title = {
                Text("Select Start Time")
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        editScheduleStart = String.format(
                            java.util.Locale.getDefault(),
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )

                        showEditStartTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showEditStartTimePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {

            TimePicker(
                state = timePickerState
            )
        }
    }

    if (showEditEndTimePicker) {

        val (hour, minute) = parseTime(editScheduleEnd)

        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = false
        )

        TimePickerDialog(
            onDismissRequest = {
                showEditEndTimePicker = false
            },

            title = {
                Text("Select End Time")
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        editScheduleEnd = String.format(
                            java.util.Locale.getDefault(),
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )

                        showEditEndTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showEditEndTimePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {

            TimePicker(
                state = timePickerState
            )
        }
    }


    /*
     * ==================================================
     * CREATE DEVICE DIALOG
     * ==================================================
     */

    if (showCreateDialog) {

        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
            },

            title = {
                Text("Add Device")
            },

            text = {

                Column {

                    /*
                     * DEVICE TYPE DROPDOWN
                     */

                    ExposedDropdownMenuBox(
                        expanded = deviceTypeExpanded,
                        onExpandedChange = {
                            deviceTypeExpanded = !deviceTypeExpanded
                        }
                    ) {

                        OutlinedTextField(
                            value = selectedDeviceType?.displayName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text("Device Type")
                            },
                            placeholder = {
                                Text("Select device type")
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = deviceTypeExpanded
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = deviceTypeExpanded,
                            onDismissRequest = {
                                deviceTypeExpanded = false
                            }
                        ) {

                            DeviceType.entries.forEach { type ->

                                DropdownMenuItem(
                                    text = {
                                        Text(type.displayName)
                                    },
                                    onClick = {

                                        selectedDeviceType = type

                                        selectedDeviceName = ""

                                        customDeviceName = ""

                                        deviceTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }


                    /*
                     * DEVICE NAME
                     */

                    if (selectedDeviceType != null) {

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = deviceNameExpanded,
                            onExpandedChange = {

                                deviceNameExpanded =
                                    !deviceNameExpanded
                            }
                        ) {

                            OutlinedTextField(
                                value = selectedDeviceName,
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Text("Device Name")
                                },
                                placeholder = {
                                    Text("Select device name")
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = deviceNameExpanded
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = deviceNameExpanded,
                                onDismissRequest = {
                                    deviceNameExpanded = false
                                }
                            ) {

                                deviceNameOptions.forEach { name ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(name)
                                        },
                                        onClick = {

                                            selectedDeviceName = name

                                            deviceNameExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }


                    /*
                     * CUSTOM NAME
                     */

                    if (selectedDeviceName == "Other") {

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        OutlinedTextField(
                            value = customDeviceName,
                            onValueChange = {
                                customDeviceName = it
                            },
                            label = {
                                Text("Custom Device Name")
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }


                    /*
                     * LIGHT SETTINGS
                     */

                    if (selectedDeviceType == DeviceType.LIGHT) {

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Light Settings",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Enable automatic schedule",
                                modifier = Modifier.weight(1f)
                            )

                            Switch(
                                checked = scheduleEnabled,
                                onCheckedChange = {
                                    scheduleEnabled = it
                                }
                            )
                        }

                        if (scheduleEnabled) {

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )


                            /*
 * START TIME
 */

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = scheduleStart.ifBlank {
                                        "Select start time"
                                    },

                                    onValueChange = {},

                                    readOnly = true,

                                    label = {
                                        Text("Start Time")
                                    },

                                    modifier = Modifier.fillMaxWidth()
                                )

                                /*
                                 * Transparent clickable layer over the
                                 * entire TextField.
                                 */
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            showStartTimePicker = true
                                        }
                                )
                            }


                            /*
                             * END TIME
                             */

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = scheduleEnd.ifBlank {
                                        "Select end time"
                                    },

                                    onValueChange = {},

                                    readOnly = true,

                                    label = {
                                        Text("End Time")
                                    },

                                    modifier = Modifier.fillMaxWidth()
                                )

                                /*
                                 * Transparent clickable layer over the
                                 * entire TextField.
                                 */
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            showEndTimePicker = true
                                        }
                                )
                            }
                        }
                    }


                    /*
                     * IRON SETTINGS
                     */

                    if (selectedDeviceType == DeviceType.IRON) {

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Safety Settings",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = ironDurationExpanded,

                            onExpandedChange = {
                                ironDurationExpanded =
                                    !ironDurationExpanded
                            }
                        ) {

                            OutlinedTextField(
                                value = "$maxOnDuration minutes",

                                onValueChange = {},

                                readOnly = true,

                                label = {
                                    Text("Maximum ON Duration")
                                },

                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = ironDurationExpanded
                                    )
                                },

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()                            )

                            ExposedDropdownMenu(
                                expanded = ironDurationExpanded,

                                onDismissRequest = {
                                    ironDurationExpanded = false
                                }
                            ) {

                                ironDurationOptions.forEach { minutes ->

                                    DropdownMenuItem(

                                        text = {
                                            Text("$minutes minutes")
                                        },

                                        onClick = {

                                            maxOnDuration = minutes

                                            ironDurationExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }


                    /*
                     * MULTI-SWITCH SETTINGS
                     */

                    if (
                        selectedDeviceType ==
                        DeviceType.MULTI_SWITCH
                    ) {

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Multi-Switch Settings",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Number of switches: $switchCount"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {

                            TextButton(
                                onClick = {

                                    if (switchCount > 2) {
                                        switchCount--
                                    }
                                }
                            ) {
                                Text("-")
                            }

                            TextButton(
                                onClick = {

                                    if (switchCount < 5) {
                                        switchCount++
                                    }
                                }
                            ) {
                                Text("+")
                            }
                        }

                        Text(
                            text = "Supported: 2 to 5 switches",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }


                    /*
                     * CAMERA SETTINGS
                     */

                    if (selectedDeviceType == DeviceType.CAMERA) {

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Camera Settings",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        OutlinedTextField(
                            value = cameraUri,
                            onValueChange = {
                                cameraUri = it
                            },
                            label = {
                                Text("Mock Camera URI")
                            },
                            placeholder = {
                                Text("mock://camera/living-room")
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },

            /*
             * CREATE
             */

            confirmButton = {

                val finalName =
                    if (selectedDeviceName == "Other") {
                        customDeviceName.trim()
                    } else {
                        selectedDeviceName.trim()
                    }

                TextButton(
                    onClick = {

                        if (
                            selectedDeviceType != null &&
                            finalName.isNotBlank()
                        ) {

                            val type =
                                selectedDeviceType!!

                            val switches =
                                if (
                                    type ==
                                    DeviceType.MULTI_SWITCH
                                ) {

                                    (1..switchCount).map { index ->

                                        DeviceSwitch(
                                            id = "switch$index",
                                            name = "Switch $index",
                                            on = false,
                                            status = "OFF"
                                        )
                                    }

                                } else {
                                    emptyList()
                                }


                            val device = Device(

                                name = finalName,

                                type = type.name,

                                status = "OFF",

                                on = false,

                                switchCount =
                                    if (
                                        type ==
                                        DeviceType.MULTI_SWITCH
                                    ) {
                                        switchCount
                                    } else {
                                        0
                                    },

                                switches = switches,

                                /*
                                 * Only fire-hazard appliances (IRON) get a
                                 * max-on-duration safety cutoff — this must
                                 * stay gated by type, otherwise every new
                                 * device (lights, cameras, outlets, etc.)
                                 * would inherit the "Maximum ON Duration"
                                 * field's default value and SafetyMonitor
                                 * would auto-turn them off after 15 minutes
                                 * too, which isn't what the field is for.
                                 */
                                maxOnDurationMinutes =
                                    if (
                                        type ==
                                        DeviceType.IRON
                                    ) {
                                        maxOnDuration
                                    } else {
                                        0
                                    },

                                scheduleEnabled =
                                    if (
                                        type ==
                                        DeviceType.LIGHT
                                    ) {
                                        scheduleEnabled
                                    } else {
                                        false
                                    },

                                scheduleStart =
                                    if (
                                        type ==
                                        DeviceType.LIGHT
                                    ) {
                                        scheduleStart
                                    } else {
                                        ""
                                    },

                                scheduleEnd =
                                    if (
                                        type ==
                                        DeviceType.LIGHT
                                    ) {
                                        scheduleEnd
                                    } else {
                                        ""
                                    },

                                cameraUri =
                                    if (
                                        type ==
                                        DeviceType.CAMERA
                                    ) {
                                        cameraUri.trim()
                                    } else {
                                        ""
                                    }
                            )


                            deviceViewModel.createDevice(
                                homeId = homeId,
                                floorId = floorId,
                                roomId = roomId,
                                device = device
                            )

                            /*
                             * RESET FORM
                             */

                            selectedDeviceType = null
                            selectedDeviceName = ""
                            customDeviceName = ""

                            scheduleEnabled = false
                            scheduleStart = ""
                            scheduleEnd = ""

                            maxOnDuration = 15

                            switchCount = 2

                            cameraUri = ""

                            showCreateDialog = false
                        }
                    },

                    enabled =
                        selectedDeviceType != null &&
                                (
                                        selectedDeviceName.isNotBlank() &&
                                                (
                                                        selectedDeviceName != "Other" ||
                                                                customDeviceName.isNotBlank()
                                                        )
                                        )
                ) {

                    Text("Create")
                }
            },

            /*
             * CANCEL
             */

            dismissButton = {

                TextButton(
                    onClick = {

                        showCreateDialog = false

                        selectedDeviceType = null
                        selectedDeviceName = ""
                        customDeviceName = ""

                        scheduleEnabled = false
                        scheduleStart = ""
                        scheduleEnd = ""

                        maxOnDuration = 15

                        switchCount = 2

                        cameraUri = ""
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }


    /*
     * ==================================================
     * EDIT DEVICE DIALOG
     * ==================================================
     */

    editingDevice?.let { device ->

        AlertDialog(
            onDismissRequest = {
                editingDevice = null
            },

            title = {
                Text("Edit Device")
            },

            text = {

                Column {

                    /*
                     * DEVICE NAME
                     */

                    OutlinedTextField(
                        value = editDeviceName,

                        onValueChange = {
                            editDeviceName = it
                        },

                        label = {
                            Text("Device Name")
                        },

                        singleLine = true,

                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )


                    /*
                     * DEVICE TYPE
                     *
                     * Type is read-only during editing.
                     */

                    OutlinedTextField(
                        value = editDeviceType,

                        onValueChange = {},

                        readOnly = true,

                        label = {
                            Text("Device Type")
                        },

                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )


                    /*
                     * ======================================
                     * LIGHT SETTINGS
                     * ======================================
                     */

                    if (
                        editDeviceType.equals(
                            "LIGHT",
                            ignoreCase = true
                        )
                    ) {

                        Text(
                            text = "Light Settings",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )


                        /*
                         * AUTOMATIC SCHEDULE
                         */

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = "Automatic Schedule"
                            )

                            Switch(
                                checked =
                                    editScheduleEnabled,

                                onCheckedChange = {
                                    editScheduleEnabled = it
                                }
                            )
                        }


                        if (editScheduleEnabled) {

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )


                            /*
                             * START TIME
                             */

                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = editScheduleStart.ifBlank {
                                        "Select start time"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = {
                                        Text("Start Time")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            showEditStartTimePicker = true
                                        }
                                )
                            }


                            /*
                             * END TIME
                             */

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = editScheduleEnd.ifBlank {
                                        "Select end time"
                                    },

                                    onValueChange = {},

                                    readOnly = true,

                                    label = {
                                        Text("End Time")
                                    },

                                    modifier = Modifier.fillMaxWidth()
                                )

                                /*
                                 * Transparent clickable layer over the
                                 * entire TextField.
                                 */
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            showEditEndTimePicker = true
                                        }
                                )
                            }
                        }
                    }


                    /*
                     * ======================================
                     * IRON SETTINGS
                     * ======================================
                     */

                    if (
                        editDeviceType.equals(
                            "IRON",
                            ignoreCase = true
                        )
                    ) {

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Safety Settings",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        /*
                         * Same fixed-options dropdown as the create
                         * dialog (ironDurationOptions), not a free-text
                         * field — otherwise editing an existing Iron
                         * could set an empty/zero/negative duration
                         * that disables the safety cutoff entirely,
                         * something creating a new Iron never allowed.
                         */
                        ExposedDropdownMenuBox(
                            expanded = editIronDurationExpanded,

                            onExpandedChange = {
                                editIronDurationExpanded =
                                    !editIronDurationExpanded
                            }
                        ) {

                            OutlinedTextField(
                                value = "$editMaxOnDuration minutes",

                                onValueChange = {},

                                readOnly = true,

                                label = {
                                    Text("Maximum ON Duration")
                                },

                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = editIronDurationExpanded
                                    )
                                },

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = editIronDurationExpanded,

                                onDismissRequest = {
                                    editIronDurationExpanded = false
                                }
                            ) {

                                ironDurationOptions.forEach { minutes ->

                                    DropdownMenuItem(

                                        text = {
                                            Text("$minutes minutes")
                                        },

                                        onClick = {

                                            editMaxOnDuration = minutes

                                            editIronDurationExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }


                    /*
                     * ======================================
                     * MULTI-SWITCH SETTINGS
                     * ======================================
                     */

                    if (
                        editDeviceType.equals(
                            "MULTI_SWITCH",
                            ignoreCase = true
                        )
                    ) {

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Switch Settings",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        /*
                         * Same +/- stepper as the create dialog,
                         * clamped to 2-5 — not a free-text field, so
                         * editing an existing device can't set a
                         * switch count creating one never allowed
                         * (0, 1, or an arbitrarily large number).
                         */
                        Text(
                            text = "Number of switches: $editSwitchCount"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {

                            TextButton(
                                onClick = {

                                    if (editSwitchCount > 2) {
                                        editSwitchCount--
                                    }
                                }
                            ) {
                                Text("-")
                            }

                            TextButton(
                                onClick = {

                                    if (editSwitchCount < 5) {
                                        editSwitchCount++
                                    }
                                }
                            ) {
                                Text("+")
                            }
                        }

                        Text(
                            text = "Supported: 2 to 5 switches",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }


                    /*
                     * ======================================
                     * CAMERA SETTINGS
                     * ======================================
                     */

                    if (
                        editDeviceType.equals(
                            "CAMERA",
                            ignoreCase = true
                        )
                    ) {

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Camera Settings",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        OutlinedTextField(
                            value = editCameraUri,

                            onValueChange = {
                                editCameraUri = it
                            },

                            label = {
                                Text("Camera URI")
                            },

                            singleLine = true,

                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },

            /*
             * ==========================================
             * SAVE
             * ==========================================
             */

            confirmButton = {

                TextButton(
                    onClick = {

                        /*
                         * MULTI SWITCH
                         *
                         * Resize the actual switches list to match the
                         * edited count (preserving existing switches'
                         * on/off state by id), then re-derive the
                         * device-level on/status from them — same
                         * logic as toggleSwitch, so a resize that drops
                         * a switch that was ON stays consistent with
                         * the room-grid summary and reports, which read
                         * the device-level "on" field directly.
                         */

                        val isMultiSwitchEdit =
                            device.type == "MULTI_SWITCH"

                        val resizedSwitches =
                            if (isMultiSwitchEdit) {
                                resizeSwitches(
                                    existingSwitches = device.switches,
                                    targetCount = editSwitchCount
                                )
                            } else {
                                device.switches
                            }

                        val anySwitchOn =
                            resizedSwitches.any { it.on }

                        val isEditControllable =
                            device.status != "ERROR" &&
                                    device.status != "DISCONNECTED"

                        val updatedDevice = device.copy(

                            name =
                                editDeviceName.trim(),

                            /*
                             * Preserve the original type.
                             */

                            type = device.type,

                            /*
                             * LIGHT
                             */

                            scheduleEnabled =
                                editScheduleEnabled,

                            scheduleStart =
                                editScheduleStart,

                            scheduleEnd =
                                editScheduleEnd,

                            /*
                             * IRON
                             *
                             * Same type gate as device creation — this
                             * also self-heals any device that was
                             * previously created before this fix (which
                             * incorrectly got a non-zero
                             * maxOnDurationMinutes regardless of type):
                             * saving an edit on a non-IRON device now
                             * always resets it back to 0.
                             */

                            maxOnDurationMinutes =
                                if (device.type == "IRON") {
                                    editMaxOnDuration
                                } else {
                                    0
                                },

                            /*
                             * MULTI SWITCH
                             */

                            switchCount =
                                editSwitchCount,

                            switches =
                                resizedSwitches,

                            on =
                                if (isMultiSwitchEdit) {
                                    anySwitchOn
                                } else {
                                    device.on
                                },

                            status =
                                if (isMultiSwitchEdit && isEditControllable) {
                                    if (anySwitchOn) "ON" else "OFF"
                                } else {
                                    device.status
                                },

                            /*
                             * CAMERA
                             */

                            cameraUri =
                                editCameraUri
                                    .trim()
                        )

                        deviceViewModel.updateDevice(
                            homeId = homeId,
                            floorId = floorId,
                            roomId = roomId,
                            device = updatedDevice
                        )

                        editingDevice = null
                    },

                    enabled =
                        editDeviceName.isNotBlank()
                ) {

                    Text("Save")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        editingDevice = null
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }

    /*
     * ==================================================
     * DELETE CONFIRMATION
     * ==================================================
     */

    deletingDevice?.let { device ->

        AlertDialog(
            onDismissRequest = {
                deletingDevice = null
            },

            title = {
                Text("Delete Device?")
            },

            text = {

                Text(
                    "Are you sure you want to delete \"${device.name}\"?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        deviceViewModel.deleteDevice(
                            homeId = homeId,
                            floorId = floorId,
                            roomId = roomId,
                            deviceId = device.id
                        )

                        deletingDevice = null
                    }
                ) {

                    Text("Delete")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        deletingDevice = null
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }


    /*
     * ==================================================
     * CAMERA PREVIEW DIALOG
     *
     * Plays the device's mock cameraUri (an mp4 stream/snapshot
     * URL) using the framework's VideoView — no extra media
     * dependency needed for a short looping demo clip.
     * ==================================================
     */

    viewingCameraUri?.let { uri ->

        Dialog(
            onDismissRequest = {
                viewingCameraUri = null
            }
        ) {

            Card {

                Column(
                    modifier = Modifier.padding(12.dp)
                ) {

                    Text(
                        text = "Camera Preview",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        factory = { context ->

                            VideoView(context).apply {

                                setVideoURI(Uri.parse(uri))

                                setOnPreparedListener { mediaPlayer ->
                                    mediaPlayer.isLooping = true
                                    start()
                                }
                            }
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {

                        TextButton(
                            onClick = {
                                viewingCameraUri = null
                            }
                        ) {

                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

fun parseTime(time: String): Pair<Int, Int> {

    if (time.isBlank()) {
        return 18 to 0
    }

    val parts = time.split(":")

    if (parts.size != 2) {
        return 18 to 0
    }

    val hour = parts[0].toIntOrNull() ?: 18
    val minute = parts[1].toIntOrNull() ?: 0

    return hour to minute
}


/*
 * RESIZE SWITCHES
 *
 * Used when editing a Multi-Switch device's switch count. Keeps
 * existing switches (same id -> same on/off state preserved) for
 * indexes that still fall within the new count, and adds fresh
 * (off) switches for any new indexes. Anything beyond the new
 * count is simply dropped. Ids follow the same "switch$index"
 * scheme used when a Multi-Switch device is first created, so
 * this lines up with the existing switches by id correctly.
 */
fun resizeSwitches(
    existingSwitches: List<DeviceSwitch>,
    targetCount: Int
): List<DeviceSwitch> {

    val existingById =
        existingSwitches.associateBy { it.id }

    return (1..targetCount).map { index ->

        val id = "switch$index"

        existingById[id] ?: DeviceSwitch(
            id = id,
            name = "Switch $index",
            on = false,
            status = "OFF"
        )
    }
}
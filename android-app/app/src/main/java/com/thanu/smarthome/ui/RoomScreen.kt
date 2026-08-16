package com.thanu.smarthome.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Balcony
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thanu.smarthome.model.Room
import com.thanu.smarthome.model.RoomSummary
import com.thanu.smarthome.viewmodel.RoomViewModel


/*
 * Maps a free-text room "type" (e.g. "Bedroom", "bathroom",
 * "Kitchen Utility Area") to a representative icon for the
 * abstract floor grid. Falls back to a generic house icon.
 */
private fun roomTypeIcon(type: String): ImageVector {

    val normalized = type.lowercase()

    return when {
        normalized.contains("bed") -> Icons.Default.Hotel
        normalized.contains("bath") -> Icons.Default.Bathtub
        normalized.contains("kitchen") -> Icons.Default.Kitchen
        normalized.contains("living") -> Icons.Default.Weekend
        normalized.contains("dining") -> Icons.Default.Weekend
        normalized.contains("study") || normalized.contains("office") -> Icons.Default.MenuBook
        normalized.contains("garage") -> Icons.Default.DirectionsCar
        normalized.contains("laundry") || normalized.contains("utility") -> Icons.Default.LocalLaundryService
        normalized.contains("balcony") -> Icons.Default.Balcony
        normalized.contains("garden") -> Icons.Default.Yard
        else -> Icons.Default.Home
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    homeId: String,
    floorId: String,
    onOpenDevices: (String, String, String, String) -> Unit,
    onBack: () -> Unit,
    roomViewModel: RoomViewModel = viewModel()
) {

    val uiState by roomViewModel.uiState.collectAsState()

    /*
     * ------------------------------------------------
     * CREATE ROOM STATE
     * ------------------------------------------------
     */

    var showCreateDialog by remember {
        mutableStateOf(false)
    }

    var selectedRoomType by remember {
        mutableStateOf("")
    }

    var selectedRoomName by remember {
        mutableStateOf("")
    }

    var customRoomName by remember {
        mutableStateOf("")
    }

    var roomTypeExpanded by remember {
        mutableStateOf(false)
    }

    var roomNameExpanded by remember {
        mutableStateOf(false)
    }


    /*
     * ------------------------------------------------
     * EDIT ROOM STATE
     * ------------------------------------------------
     */

    var editingRoom by remember {
        mutableStateOf<Room?>(null)
    }

    var editRoomName by remember {
        mutableStateOf("")
    }

    var editRoomType by remember {
        mutableStateOf("")
    }


    /*
     * ------------------------------------------------
     * DELETE ROOM STATE
     * ------------------------------------------------
     */

    var deletingRoom by remember {
        mutableStateOf<Room?>(null)
    }


    /*
     * ------------------------------------------------
     * ROOM TYPES
     * ------------------------------------------------
     */

    val roomTypes = listOf(
        "Living Room",
        "Bedroom",
        "Kitchen",
        "Bathroom",
        "Dining Room",
        "Study Room",
        "Garage",
        "Laundry Room",
        "Balcony",
        "Garden",
        "Other"
    )


    /*
     * ------------------------------------------------
     * ROOM NAME OPTIONS
     * ------------------------------------------------
     */

    val roomNameOptions = when (selectedRoomType) {

        "Living Room" -> listOf(
            "Living Room",
            "Main Living Room",
            "Family Living Room",
            "Other"
        )

        "Bedroom" -> listOf(
            "Master Bedroom",
            "Guest Bedroom",
            "Kids Bedroom",
            "Bedroom",
            "Other"
        )

        "Kitchen" -> listOf(
            "Kitchen",
            "Main Kitchen",
            "Kitchen Utility Area",
            "Other"
        )

        "Bathroom" -> listOf(
            "Bathroom",
            "Master Bathroom",
            "Guest Bathroom",
            "Common Bathroom",
            "Other"
        )

        "Dining Room" -> listOf(
            "Dining Room",
            "Main Dining Room",
            "Other"
        )

        "Study Room" -> listOf(
            "Study Room",
            "Home Office",
            "Study Area",
            "Other"
        )

        "Garage" -> listOf(
            "Garage",
            "Main Garage",
            "Other"
        )

        "Laundry Room" -> listOf(
            "Laundry Room",
            "Utility Room",
            "Other"
        )

        "Balcony" -> listOf(
            "Balcony",
            "Main Balcony",
            "Other"
        )

        "Garden" -> listOf(
            "Garden",
            "Front Garden",
            "Back Garden",
            "Other"
        )

        "Other" -> listOf(
            "Other"
        )

        else -> emptyList()
    }


    /*
     * ------------------------------------------------
     * LOAD ROOMS (REAL-TIME)
     * ------------------------------------------------
     */

    LaunchedEffect(homeId, floorId) {

        roomViewModel.startListening(
            homeId = homeId,
            floorId = floorId
        )
    }


    /*
     * ------------------------------------------------
     * MAIN SCREEN
     *
     * An abstract grid of room tiles, per the spec's "abstract
     * (simple) grid mapping overlaid onto specific floor layouts."
     * Non-grid content (header, empty state, loading, messages)
     * spans the full grid width via GridItemSpan.
     * ------------------------------------------------
     */

    val fullWidthSpan: LazyGridItemSpanScope.() -> GridItemSpan = {
        GridItemSpan(maxLineSpan)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        /*
         * HEADER
         */

        item(span = fullWidthSpan) {

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
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Column {

                        Text(
                            text = "Rooms",
                            style =
                                MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            text = "Manage rooms",
                            style =
                                MaterialTheme.typography.bodyMedium
                        )
                    }
                }


                /*
                 * ADD ROOM
                 */

                IconButton(
                    onClick = {

                        selectedRoomType = ""
                        selectedRoomName = ""
                        customRoomName = ""

                        roomTypeExpanded = false
                        roomNameExpanded = false

                        showCreateDialog = true
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Room",
                        modifier = Modifier.size(28.dp)
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
            uiState.rooms.isEmpty() &&
            !uiState.isLoading
        ) {

            item(span = fullWidthSpan) {

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Text(
                    text = "No rooms yet",
                    style =
                        MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Tap + to create your first room."
                )
            }
        }


        /*
         * ROOM GRID
         *
         * Abstract grid tile per room: type icon, name, and a
         * live device summary badge (count / ON count / alert)
         * derived from roomSummaries. Tapping the tile opens its
         * devices; Edit/Delete stay as small icons at the bottom.
         */

        items(
            items = uiState.rooms,
            key = { room -> room.id }
        ) { room ->

            val summary =
                uiState.roomSummaries[room.id] ?: RoomSummary()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .padding(6.dp)
                    .clickable {

                        onOpenDevices(
                            homeId,
                            floorId,
                            room.id,
                            room.name
                        )
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {

                    /*
                     * ROOM TYPE ICON
                     */

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = roomTypeIcon(room.type),
                            contentDescription = room.type,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2
                    )

                    Text(
                        text = room.type,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )


                    /*
                     * DEVICE SUMMARY BADGE
                     */

                    if (summary.hasCriticalAlert) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alert",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(4.dp)
                            )

                            Text(
                                text = "Safety alert",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                    } else if (summary.deviceCount > 0) {

                        Text(
                            text = "${summary.devicesOn}/${summary.deviceCount} devices ON",
                            style = MaterialTheme.typography.bodySmall
                        )

                    } else {

                        Text(
                            text = "No devices yet",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )


                    /*
                     * ROOM ACTIONS
                     */

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {

                        /*
                         * EDIT
                         */

                        IconButton(
                            onClick = {

                                editingRoom = room

                                editRoomName = room.name
                                editRoomType = room.type
                            },

                            enabled = !uiState.isLoading,
                            modifier = Modifier.size(32.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription =
                                    "Edit ${room.name}",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )


                        /*
                         * DELETE
                         */

                        IconButton(
                            onClick = {

                                deletingRoom = room
                            },

                            enabled = !uiState.isLoading,
                            modifier = Modifier.size(32.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription =
                                    "Delete ${room.name}",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }


        /*
         * LOADING
         */

        item(span = fullWidthSpan) {

            if (uiState.isLoading) {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                CircularProgressIndicator()
            }
        }


        /*
         * SUCCESS / ERROR
         */

        item(span = fullWidthSpan) {

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


    /*
     * ==================================================
     * CREATE ROOM DIALOG
     * ==================================================
     */

    if (showCreateDialog) {

        AlertDialog(
            onDismissRequest = {

                showCreateDialog = false
            },

            title = {

                Text("Create Room")
            },

            text = {

                Column {

                    /*
                     * ROOM TYPE DROPDOWN
                     */

                    ExposedDropdownMenuBox(
                        expanded = roomTypeExpanded,

                        onExpandedChange = {

                            roomTypeExpanded =
                                !roomTypeExpanded
                        }
                    ) {

                        OutlinedTextField(
                            value = selectedRoomType,

                            onValueChange = {},

                            readOnly = true,

                            label = {
                                Text("Room Type")
                            },

                            placeholder = {
                                Text("Select room type")
                            },

                            trailingIcon = {

                                ExposedDropdownMenuDefaults
                                    .TrailingIcon(
                                        expanded =
                                            roomTypeExpanded
                                    )
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = roomTypeExpanded,

                            onDismissRequest = {

                                roomTypeExpanded = false
                            }
                        ) {

                            roomTypes.forEach { type ->

                                DropdownMenuItem(

                                    text = {
                                        Text(type)
                                    },

                                    onClick = {

                                        selectedRoomType = type

                                        selectedRoomName = ""

                                        customRoomName = ""

                                        roomTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }


                    /*
                     * ROOM NAME DROPDOWN
                     */

                    if (selectedRoomType.isNotBlank()) {

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = roomNameExpanded,

                            onExpandedChange = {

                                roomNameExpanded =
                                    !roomNameExpanded
                            }
                        ) {

                            OutlinedTextField(
                                value = selectedRoomName,

                                onValueChange = {},

                                readOnly = true,

                                label = {
                                    Text("Room Name")
                                },

                                placeholder = {
                                    Text("Select room name")
                                },

                                trailingIcon = {

                                    ExposedDropdownMenuDefaults
                                        .TrailingIcon(
                                            expanded =
                                                roomNameExpanded
                                        )
                                },

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = roomNameExpanded,

                                onDismissRequest = {

                                    roomNameExpanded = false
                                }
                            ) {

                                roomNameOptions.forEach { name ->

                                    DropdownMenuItem(

                                        text = {
                                            Text(name)
                                        },

                                        onClick = {

                                            selectedRoomName =
                                                name

                                            roomNameExpanded =
                                                false
                                        }
                                    )
                                }
                            }
                        }
                    }


                    /*
                     * CUSTOM ROOM NAME
                     */

                    if (selectedRoomName == "Other") {

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        OutlinedTextField(
                            value = customRoomName,

                            onValueChange = {

                                customRoomName = it
                            },

                            label = {
                                Text("Custom Room Name")
                            },

                            placeholder = {
                                Text("e.g. Home Office")
                            },

                            singleLine = true,

                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },


            /*
             * CREATE BUTTON
             */

            confirmButton = {

                val finalRoomName =
                    if (selectedRoomName == "Other") {

                        customRoomName.trim()

                    } else {

                        selectedRoomName.trim()
                    }


                TextButton(
                    onClick = {

                        if (
                            selectedRoomType.isNotBlank() &&
                            finalRoomName.isNotBlank()
                        ) {

                            val room = Room(
                                name = finalRoomName,
                                type = selectedRoomType
                            )

                            roomViewModel.createRoom(
                                homeId = homeId,
                                floorId = floorId,
                                room = room
                            )

                            /*
                             * RESET FORM
                             */

                            selectedRoomType = ""
                            selectedRoomName = ""
                            customRoomName = ""

                            roomTypeExpanded = false
                            roomNameExpanded = false

                            showCreateDialog = false
                        }
                    },

                    enabled =
                        selectedRoomType.isNotBlank() &&
                                selectedRoomName.isNotBlank() &&
                                (
                                        selectedRoomName != "Other" ||
                                                customRoomName.isNotBlank()
                                        )
                ) {

                    Text("Create")
                }
            },


            /*
             * CANCEL BUTTON
             */

            dismissButton = {

                TextButton(
                    onClick = {

                        showCreateDialog = false

                        selectedRoomType = ""
                        selectedRoomName = ""
                        customRoomName = ""

                        roomTypeExpanded = false
                        roomNameExpanded = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }


    /*
     * ==================================================
     * EDIT ROOM DIALOG
     * ==================================================
     */

    editingRoom?.let { room ->

        AlertDialog(
            onDismissRequest = {

                editingRoom = null
            },

            title = {

                Text("Edit Room")
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = editRoomName,

                        onValueChange = {

                            editRoomName = it
                        },

                        label = {
                            Text("Room Name")
                        },

                        singleLine = true,

                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = editRoomType,

                        onValueChange = {

                            editRoomType = it
                        },

                        label = {
                            Text("Room Type")
                        },

                        singleLine = true,

                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        if (
                            editRoomName.isNotBlank() &&
                            editRoomType.isNotBlank()
                        ) {

                            val updatedRoom = room.copy(
                                name =
                                    editRoomName.trim(),

                                type =
                                    editRoomType.trim()
                            )

                            roomViewModel.updateRoom(
                                homeId = homeId,
                                floorId = floorId,
                                room = updatedRoom
                            )

                            editingRoom = null
                        }
                    }
                ) {

                    Text("Save")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        editingRoom = null
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

    deletingRoom?.let { room ->

        AlertDialog(
            onDismissRequest = {

                deletingRoom = null
            },

            title = {

                Text("Delete Room?")
            },

            text = {

                Text(
                    "Are you sure you want to delete \"${room.name}\"?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        roomViewModel.deleteRoom(
                            homeId = homeId,
                            floorId = floorId,
                            roomId = room.id
                        )

                        deletingRoom = null
                    }
                ) {

                    Text("Delete")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        deletingRoom = null
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}
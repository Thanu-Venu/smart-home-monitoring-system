package com.thanu.smarthome.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thanu.smarthome.model.Floor
import com.thanu.smarthome.viewmodel.FloorViewModel

@Composable
fun FloorScreen(
    homeId: String,
    onOpenRooms: (String, String) -> Unit,
    onBack: () -> Unit,
    floorViewModel: FloorViewModel = viewModel()
) {

    val uiState by floorViewModel.uiState.collectAsState()

    var showCreateDialog by remember {
        mutableStateOf(false)
    }

    var floorName by remember {
        mutableStateOf("")
    }

    var floorNumber by remember {
        mutableStateOf("")
    }

    var editingFloor by remember {
        mutableStateOf<Floor?>(null)
    }

    var editFloorName by remember {
        mutableStateOf("")
    }

    var editFloorNumber by remember {
        mutableStateOf("")
    }

    var deletingFloor by remember {
        mutableStateOf<Floor?>(null)
    }

    /*
     * LOAD FLOORS FOR SELECTED HOME (REAL-TIME)
     */
    LaunchedEffect(homeId) {

        floorViewModel.startListening(homeId)
    }

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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Column {

                        Text(
                            text = "Floors",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            text = "Manage floors",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                IconButton(
                    onClick = {
                        showCreateDialog = true
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Floor",
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
        if (uiState.floors.isEmpty() && !uiState.isLoading) {

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
                        text = "No floors yet",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Tap + to create your first floor."
                    )
                }
            }
        }


        /*
         * FLOOR LIST
         */
        items(
            uiState.floors,
            key = { floor -> floor.id }
        ) { floor ->

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

                    Text(
                        text = floor.name,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Floor ${floor.floorNumber}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {

                        /*
                         * EDIT
                         */
                        IconButton(
                            onClick = {

                                editingFloor = floor

                                editFloorName = floor.name
                                editFloorNumber =
                                    floor.floorNumber.toString()
                            },
                            enabled = !uiState.isLoading
                        ) {

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit ${floor.name}"
                            )
                        }

                        /*
                         * DELETE
                         */
                        IconButton(
                            onClick = {

                                deletingFloor = floor
                            },
                            enabled = !uiState.isLoading
                        ) {

                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete ${floor.name}"
                            )
                        }

                        /*
                         * OPEN ROOMS
                         */
                        IconButton(
                            onClick = {

                                onOpenRooms(homeId, floor.id)
                            },
                            enabled = !uiState.isLoading
                        ) {

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Open ${floor.name}"
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
         * MESSAGE / ERROR
         */
        item {

            Column {

                uiState.message?.let { message ->

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(message)
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


    /*
     * CREATE FLOOR DIALOG
     */
    if (showCreateDialog) {

        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
            },

            title = {
                Text("Create Floor")
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = floorName,
                        onValueChange = {
                            floorName = it
                        },
                        label = {
                            Text("Floor Name")
                        },
                        singleLine = true
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = floorNumber,
                        onValueChange = {
                            floorNumber = it
                        },
                        label = {
                            Text("Floor Number")
                        },
                        singleLine = true
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        val number =
                            floorNumber.toIntOrNull()

                        if (
                            floorName.isNotBlank() &&
                            number != null
                        ) {

                            val floor = Floor(
                                name = floorName.trim(),
                                floorNumber = number
                            )

                            floorViewModel.createFloor(
                                homeId = homeId,
                                floor = floor
                            )

                            floorName = ""
                            floorNumber = ""

                            showCreateDialog = false
                        }
                    }
                ) {

                    Text("Create")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        showCreateDialog = false
                        floorName = ""
                        floorNumber = ""
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }


    /*
     * EDIT FLOOR DIALOG
     */
    editingFloor?.let { floor ->

        AlertDialog(
            onDismissRequest = {
                editingFloor = null
            },

            title = {
                Text("Edit Floor")
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = editFloorName,
                        onValueChange = {
                            editFloorName = it
                        },
                        label = {
                            Text("Floor Name")
                        },
                        singleLine = true
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = editFloorNumber,
                        onValueChange = {
                            editFloorNumber = it
                        },
                        label = {
                            Text("Floor Number")
                        },
                        singleLine = true
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        val number =
                            editFloorNumber.toIntOrNull()

                        if (
                            editFloorName.isNotBlank() &&
                            number != null
                        ) {

                            val updatedFloor = floor.copy(
                                name = editFloorName.trim(),
                                floorNumber = number
                            )

                            floorViewModel.updateFloor(
                                homeId = homeId,
                                floor = updatedFloor
                            )

                            editingFloor = null
                        }
                    }
                ) {

                    Text("Save")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        editingFloor = null
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }


    /*
     * DELETE CONFIRMATION
     */
    deletingFloor?.let { floor ->

        AlertDialog(
            onDismissRequest = {
                deletingFloor = null
            },

            title = {
                Text("Delete Floor?")
            },

            text = {
                Text(
                    "Are you sure you want to delete \"${floor.name}\"?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        floorViewModel.deleteFloor(
                            homeId = homeId,
                            floorId = floor.id
                        )

                        deletingFloor = null
                    }
                ) {

                    Text("Delete")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        deletingFloor = null
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}
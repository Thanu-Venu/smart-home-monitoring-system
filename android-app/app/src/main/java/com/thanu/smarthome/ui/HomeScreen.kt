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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thanu.smarthome.model.Home
import com.thanu.smarthome.repository.AuthRepository
import com.thanu.smarthome.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onOpenFloors: (String) -> Unit,
    onOpenReports: (String) -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {

    val uiState by homeViewModel.uiState.collectAsState()

    /*
     * Homes are now scoped to the signed-in user instead of a
     * hardcoded "user001" placeholder.
     */
    val authRepository = remember {
        AuthRepository()
    }

    val currentUserId = authRepository.currentUserId


    var homeName by remember {
        mutableStateOf("")
    }

    var showCreateDialog by remember {
        mutableStateOf(false)
    }

    var editingHome by remember {
        mutableStateOf<Home?>(null)
    }

    var editHomeName by remember {
        mutableStateOf("")
    }

    var deletingHome by remember {
        mutableStateOf<Home?>(null)
    }

    /*
     * LOAD HOMES (REAL-TIME)
     */
    LaunchedEffect(currentUserId) {
        homeViewModel.startListening(currentUserId)
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

                Column {

                    Text(
                        text = "Smart Home",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "My Homes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row {

                    IconButton(
                        onClick = {
                            showCreateDialog = true
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Home",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = {

                            authRepository.signOut()

                            onLogout()
                        }
                    ) {

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }


        /*
         * DASHBOARD SUMMARY
         *
         * Totals across every home this user owns — gives the
         * screen something to say at a glance instead of jumping
         * straight into a plain list. Counts come from the same
         * real-time listener that already loads the home list, so
         * this updates live as floors/rooms/devices change anywhere.
         */
        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                DashboardStatCard(
                    label = "Floors",
                    value = uiState.summary.floorsCount.toString(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )

                DashboardStatCard(
                    label = "Rooms",
                    value = uiState.summary.roomsCount.toString(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )

                DashboardStatCard(
                    label = "Devices",
                    value = uiState.summary.devicesCount.toString(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }


        /*
         * HOME LIST
         */
        items(
            uiState.homes,
            key = { home -> home.id }
        ) { home ->

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
                     * HOME INFORMATION
                     */
                    Text(
                        text = home.name,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Owner: ${home.ownerId}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "ID: ${home.id}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    /*
                     * ACTIONS
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

                                editingHome = home
                                editHomeName = home.name
                            },
                            enabled = !uiState.isLoading
                        ) {

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit ${home.name}"
                            )
                        }

                        /*
                         * DELETE
                         */
                        IconButton(
                            onClick = {

                                deletingHome = home
                            },
                            enabled = !uiState.isLoading
                        ) {

                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete ${home.name}"
                            )
                        }

                        /*
                         * REPORTS
                         */
                        IconButton(
                            onClick = {

                                onOpenReports(home.id)
                            },
                            enabled = !uiState.isLoading
                        ) {

                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "View report for ${home.name}"
                            )
                        }

                        /*
                         * OPEN
                         */
                        IconButton(
                            onClick = {

                                homeViewModel.selectHome(home)

                                onOpenFloors(home.id)
                            },
                            enabled = !uiState.isLoading
                        ) {

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Open ${home.name}"
                            )
                        }
                    }
                }
            }
        }


        /*
         * EMPTY STATE
         */
        if (uiState.homes.isEmpty() && !uiState.isLoading) {

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
                        text = "No homes yet",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Tap + to create your first home."
                    )
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
         * MESSAGE
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


    /*
     * CREATE HOME DIALOG
     */
    if (showCreateDialog) {

        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
            },

            title = {
                Text("Create Home")
            },

            text = {

                OutlinedTextField(
                    value = homeName,
                    onValueChange = {
                        homeName = it
                    },
                    label = {
                        Text("Home Name")
                    },
                    singleLine = true
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        if (homeName.isNotBlank()) {

                            homeViewModel.createHome(
                                name = homeName.trim(),
                                ownerId = currentUserId
                            )

                            homeName = ""
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
                        homeName = ""
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }


    /*
     * EDIT HOME DIALOG
     */
    editingHome?.let { home ->

        AlertDialog(
            onDismissRequest = {
                editingHome = null
            },

            title = {
                Text("Edit Home")
            },

            text = {

                OutlinedTextField(
                    value = editHomeName,
                    onValueChange = {
                        editHomeName = it
                    },
                    label = {
                        Text("Home Name")
                    },
                    singleLine = true
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        if (editHomeName.isNotBlank()) {

                            val updatedHome = home.copy(
                                name = editHomeName.trim()
                            )

                            homeViewModel.updateHome(
                                updatedHome
                            )

                            editingHome = null
                        }
                    }
                ) {

                    Text("Save")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        editingHome = null
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
    deletingHome?.let { home ->

        AlertDialog(
            onDismissRequest = {
                deletingHome = null
            },

            title = {
                Text("Delete Home?")
            },

            text = {
                Text(
                    "Are you sure you want to delete \"${home.name}\"?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        homeViewModel.deleteHome(
                            home.id
                        )

                        deletingHome = null
                    }
                ) {

                    Text("Delete")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        deletingHome = null
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}


/*
 * DASHBOARD STAT CARD
 *
 * A single colorful "at a glance" tile for the Home screen summary
 * row (Floors / Rooms / Devices). Takes explicit container/content
 * colors rather than picking its own, so each of the three cards can
 * use a different theme role (primary/secondary/tertiary container)
 * while still automatically adapting to light/dark mode.
 */
@Composable
private fun DashboardStatCard(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
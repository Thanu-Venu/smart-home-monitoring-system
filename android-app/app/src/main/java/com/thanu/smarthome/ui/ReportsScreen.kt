package com.thanu.smarthome.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thanu.smarthome.model.DeviceAlert
import com.thanu.smarthome.model.DeviceType
import com.thanu.smarthome.viewmodel.ReportViewModel

/*
 * In-app usage report for a home: current device counts, an ON/OFF
 * breakdown, a per-type breakdown, and any active CRITICAL safety
 * alerts. Backed by a real-time listener (ReportViewModel), so it
 * updates live as devices are toggled or auto-switched by
 * SafetyMonitor / ScheduleMonitor.
 */
@Composable
fun ReportsScreen(
    homeId: String,
    onBack: () -> Unit,
    reportViewModel: ReportViewModel = viewModel()
) {

    val uiState by reportViewModel.uiState.collectAsState()

    LaunchedEffect(homeId) {
        reportViewModel.startListening(homeId)
    }

    val report = uiState.report

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
                        text = "Usage Report",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "Live snapshot of this home",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            if (uiState.isLoading) {

                CircularProgressIndicator()

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            uiState.errorMessage?.let { message ->

                Text(
                    text = "Error: $message",
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }
        }


        /*
         * STAT TILES
         */

        item {

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                StatTile(
                    label = "Devices",
                    value = report.totalDevices.toString(),
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatTile(
                    label = "ON",
                    value = report.devicesOn.toString(),
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatTile(
                    label = "OFF",
                    value = report.devicesOff.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                StatTile(
                    label = "Floors",
                    value = report.floorsCount.toString(),
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatTile(
                    label = "Rooms",
                    value = report.roomsCount.toString(),
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatTile(
                    label = "Alerts",
                    value = report.activeAlerts.size.toString(),
                    modifier = Modifier.weight(1f),
                    containerColor = if (report.activeAlerts.isNotEmpty()) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (report.activeAlerts.isNotEmpty()) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }


        /*
         * BY DEVICE TYPE
         */

        item {

            Text(
                text = "By Device Type",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (report.deviceTypeCounts.isEmpty()) {

                Text(
                    text = "No devices yet",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        items(
            items = report.deviceTypeCounts.entries.toList(),
            key = { entry -> entry.key }
        ) { entry ->

            val displayName =
                DeviceType.entries
                    .find { type -> type.name == entry.key }
                    ?.displayName
                    ?: entry.key

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(displayName)
                    Text(entry.value.toString())
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }


        /*
         * ACTIVE ALERTS
         */

        item {

            Text(
                text = "Active Safety Alerts",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (report.activeAlerts.isEmpty()) {

                Text(
                    text = "No active alerts",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        items(
            items = report.activeAlerts,
            key = { alert -> alert.roomName + alert.deviceName }
        ) { alert ->

            AlertCard(alert)
        }
    }
}


@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (containerColor == Color.Unspecified) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                containerColor
            },
            contentColor = if (contentColor == Color.Unspecified) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                contentColor
            }
        )
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
private fun AlertCard(alert: DeviceAlert) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alert",
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {

                Text(
                    text = "${alert.deviceName} (${alert.roomName})",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = alert.alert,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

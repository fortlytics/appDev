package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.NetworkReading
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoverageViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: CoverageViewModel,
    modifier: Modifier = Modifier
) {
    val readings by viewModel.allReadings.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedReadingForDetail by remember { mutableStateOf<NetworkReading?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val filteredReadings = remember(readings, searchQuery) {
        if (searchQuery.isBlank()) readings
        else readings.filter {
            it.operatorName.contains(searchQuery, ignoreCase = true) ||
                    it.networkType.contains(searchQuery, ignoreCase = true) ||
                    it.signalStrengthDbm.toString().contains(searchQuery)
        }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("history_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Row: Count + CSV Export Action Button
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Coverage Records",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${readings.size} Stored Telemetry Entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.exportAndShareCsv() },
                        modifier = Modifier.testTag("export_csv_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export CSV",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", style = MaterialTheme.typography.labelMedium)
                    }

                    if (readings.isNotEmpty()) {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .testTag("clear_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_history_field"),
            placeholder = { Text("Filter by carrier, 5G, or dBm...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // Reading History List
        if (filteredReadings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "No data",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (readings.isEmpty()) "No coverage logs stored yet." else "No records match search filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filteredReadings,
                    key = { it.id }
                ) { item ->
                    ReadingHistoryCard(
                        reading = item,
                        dateFormat = dateFormat,
                        onSelect = { selectedReadingForDetail = item },
                        onDelete = { viewModel.deleteReading(item.id) }
                    )
                }
            }
        }
    }

    // Detail Modal Dialog
    selectedReadingForDetail?.let { reading ->
        AlertDialog(
            onDismissRequest = { selectedReadingForDetail = null },
            confirmButton = {
                TextButton(onClick = { selectedReadingForDetail = null }) {
                    Text("Close")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(reading.signalQuality.hexColor)
                    ) {
                        Text(
                            text = "${reading.signalStrengthDbm} dBm",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(reading.operatorName, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Time: ${dateFormat.format(Date(reading.timestamp))}")
                    Text("Network Type: ${reading.networkType}")
                    Text("Location: ${reading.latitude}, ${reading.longitude}")
                    if (reading.rsrp != null) Text("RSRP: ${reading.rsrp} dBm")
                    if (reading.rsrq != null) Text("RSRQ: ${reading.rsrq} dB")
                    if (reading.rssnr != null) Text("RSSNR: ${reading.rssnr} dB")
                    if (!reading.cellId.isNullOrBlank()) Text("Cell ID: ${reading.cellId}")
                    Text("Download: ${reading.downloadMbps} Mbps")
                    Text("Upload: ${reading.uploadMbps} Mbps")
                    Text("Ping: ${reading.pingMs} ms")
                    Text("Mode: ${if (reading.isSimulated) "Simulated Telemetry" else "Physical Radio"}")
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Clear All Readings?") },
            text = { Text("This will permanently delete all stored coverage readings from the local database.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllReadings()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReadingHistoryCard(
    reading: NetworkReading,
    dateFormat: SimpleDateFormat,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(reading.signalQuality.hexColor)
                    ) {
                        Text(
                            text = "${reading.signalStrengthDbm} dBm",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reading.networkType,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${reading.operatorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateFormat.format(Date(reading.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (reading.downloadMbps > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "DL: ${reading.downloadMbps} Mbps | UL: ${reading.uploadMbps} Mbps | Ping: ${reading.pingMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechCyan
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CustomMapView
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoverageViewModel

@Composable
fun MapCoverageScreen(
    viewModel: CoverageViewModel,
    modifier: Modifier = Modifier
) {
    val readings by viewModel.allReadings.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("map_coverage_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Stats Header Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Map",
                        tint = TechBlue
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Coverage Heatmap",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${readings.size} Logged Geographical Points",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Legend Pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SignalExcellent
                    ) {
                        Text(
                            text = "Good",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SignalFair
                    ) {
                        Text(
                            text = "Fair",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SignalPoor
                    ) {
                        Text(
                            text = "Poor",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }

        // Full Interactive Map Component
        Box(modifier = Modifier.weight(1f)) {
            CustomMapView(
                readings = readings,
                currentLat = currentLocation.latitude,
                currentLng = currentLocation.longitude
            )
        }
    }
}

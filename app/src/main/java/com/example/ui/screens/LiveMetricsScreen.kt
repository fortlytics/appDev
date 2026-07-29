package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.MetricTile
import com.example.ui.components.SignalGaugeCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoverageViewModel

@Composable
fun LiveMetricsScreen(
    viewModel: CoverageViewModel,
    onNavigateToSpeedTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val teleSnapshot by viewModel.telephonySnapshot.collectAsState()
    val locationSnapshot by viewModel.currentLocation.collectAsState()
    val isSimulation by viewModel.isSimulationMode.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("live_metrics_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simulation Mode Card Banner (for evaluators on emulator)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSimulation) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isSimulation) Icons.Default.Tune else Icons.Default.Smartphone,
                        contentDescription = "Simulation Mode",
                        tint = if (isSimulation) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isSimulation) "Emulator Simulation Active" else "Physical Radio Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimulation) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isSimulation) "Generates live signal telemetry for evaluation" else "Reading live telephony hardware state",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSimulation) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isSimulation,
                    onCheckedChange = { viewModel.toggleSimulationMode(it) },
                    modifier = Modifier.testTag("simulation_toggle")
                )
            }
        }

        // Signal Gauge Hero
        SignalGaugeCard(
            dbm = teleSnapshot.signalStrengthDbm,
            networkType = teleSnapshot.networkType,
            operatorName = teleSnapshot.operatorName,
            cellId = teleSnapshot.cellId,
            isSimulated = teleSnapshot.isSimulated
        )

        // Location Info Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = TechBlue
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GPS Location Tag",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format("%.4f", locationSnapshot.latitude)}, ${String.format("%.4f", locationSnapshot.longitude)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.refreshLocation() },
                    modifier = Modifier.testTag("refresh_location_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Location")
                }
            }
        }

        // Cellular Metric Cards Grid
        Text(
            text = "Radio Telemetry Metrics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                title = "RSRP (LTE Power)",
                value = teleSnapshot.rsrp?.toString() ?: "--",
                unit = "dBm",
                icon = Icons.Default.BarChart,
                iconColor = TechBlue,
                subtext = "Reference Signal Power",
                modifier = Modifier.weight(1f)
            )

            MetricTile(
                title = "RSRQ (Quality)",
                value = teleSnapshot.rsrq?.toString() ?: "--",
                unit = "dB",
                icon = Icons.Default.HighQuality,
                iconColor = SignalFair,
                subtext = "Received Signal Quality",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                title = "RSSNR / SNR",
                value = teleSnapshot.rssnr?.toString() ?: "--",
                unit = "dB",
                icon = Icons.Default.GraphicEq,
                iconColor = SignalExcellent,
                subtext = "Signal Noise Ratio",
                modifier = Modifier.weight(1f)
            )

            MetricTile(
                title = "Cellular Tech",
                value = teleSnapshot.networkType,
                unit = "",
                icon = Icons.Default.CellTower,
                iconColor = TechCyan,
                subtext = "Network Generation",
                modifier = Modifier.weight(1f)
            )
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.quickSaveCurrentReading() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("quick_save_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Reading", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onNavigateToSpeedTest,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("go_to_speed_test_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Speed, contentDescription = "Speed Test")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Speed Test", fontWeight = FontWeight.Bold)
            }
        }
    }
}

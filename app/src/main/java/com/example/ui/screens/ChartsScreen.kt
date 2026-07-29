package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.MetricTile
import com.example.ui.components.SignalTrendChart
import com.example.ui.components.SpeedTrendChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoverageViewModel

@Composable
fun ChartsScreen(
    viewModel: CoverageViewModel,
    modifier: Modifier = Modifier
) {
    val readings by viewModel.allReadings.collectAsState()
    val summary by viewModel.analyticsSummary.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("charts_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Performance Summary Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Historical Quality Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Averaged telemetry across ${summary.totalReadings} recorded samples",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${summary.avgDbm} dBm Avg",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quality Ratio Progress Bar
                if (summary.totalReadings > 0) {
                    val excellentFrac = summary.excellentCount.toFloat() / summary.totalReadings
                    val goodFrac = summary.goodCount.toFloat() / summary.totalReadings
                    val fairFrac = summary.fairCount.toFloat() / summary.totalReadings
                    val poorFrac = summary.poorCount.toFloat() / summary.totalReadings

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        if (excellentFrac > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(excellentFrac)
                                    .fillMaxHeight()
                                    .background(SignalExcellent)
                            )
                        }
                        if (goodFrac > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(goodFrac)
                                    .fillMaxHeight()
                                    .background(SignalGood)
                            )
                        }
                        if (fairFrac > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(fairFrac)
                                    .fillMaxHeight()
                                    .background(SignalFair)
                            )
                        }
                        if (poorFrac > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(poorFrac)
                                    .fillMaxHeight()
                                    .background(SignalPoor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QualityLegendItem("Exc: ${summary.excellentCount}", SignalExcellent)
                        QualityLegendItem("Good: ${summary.goodCount}", SignalGood)
                        QualityLegendItem("Fair: ${summary.fairCount}", SignalFair)
                        QualityLegendItem("Poor: ${summary.poorCount}", SignalPoor)
                    }
                }
            }
        }

        // Grid Summary Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                title = "Avg Download",
                value = "${summary.avgDownloadMbps}",
                unit = "Mbps",
                icon = Icons.Default.Download,
                iconColor = TechBlue,
                modifier = Modifier.weight(1f)
            )

            MetricTile(
                title = "Avg Upload",
                value = "${summary.avgUploadMbps}",
                unit = "Mbps",
                icon = Icons.Default.Upload,
                iconColor = SignalFair,
                modifier = Modifier.weight(1f)
            )
        }

        // Trend Charts
        SignalTrendChart(readings = readings)

        SpeedTrendChart(readings = readings)
    }
}

@Composable
private fun QualityLegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

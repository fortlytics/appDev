package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NetworkReading
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMapView(
    readings: List<NetworkReading>,
    currentLat: Double,
    currentLng: Double,
    modifier: Modifier = Modifier
) {
    var isSatelliteMode by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var zoomLevel by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedReading by remember { mutableStateOf<NetworkReading?>(null) }

    val filteredReadings = remember(readings, selectedFilter) {
        when (selectedFilter) {
            "5G" -> readings.filter { it.networkType == "5G" }
            "4G LTE" -> readings.filter { it.networkType.contains("4G") }
            "High Speed" -> readings.filter { it.downloadMbps >= 25.0 }
            else -> readings
        }
    }

    // Determine bounding box
    val centerLat = remember(readings, currentLat) {
        if (readings.isNotEmpty()) readings.map { it.latitude }.average() else currentLat
    }
    val centerLng = remember(readings, currentLng) {
        if (readings.isNotEmpty()) readings.map { it.longitude }.average() else currentLng
    }

    val mapBgColor = if (isSatelliteMode) Color(0xFF0B1320) else Color(0xFF1E293B)
    val gridLineColor = if (isSatelliteMode) Color(0xFF1E293B) else Color(0xFF334155)

    Card(
        modifier = modifier
            .fillMaxSize()
            .testTag("custom_map_view"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = mapBgColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Interactive Map Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomLevel = (zoomLevel * zoom).coerceIn(0.5f, 4.0f)
                            panOffset += pan
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                // Draw Grid & Coordinate Lines
                val gridSpacing = 60.dp.toPx() * zoomLevel
                var x = (panOffset.x % gridSpacing)
                while (x < width) {
                    drawLine(
                        color = gridLineColor,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                    x += gridSpacing
                }

                var y = (panOffset.y % gridSpacing)
                while (y < height) {
                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += gridSpacing
                }

                // Function to map (lat, lng) -> Screen Offset
                fun mapToScreen(lat: Double, lng: Double): Offset {
                    val scale = 18000f * zoomLevel
                    val dx = ((lng - centerLng) * scale).toFloat() + width / 2f + panOffset.x
                    val dy = (-(lat - centerLat) * scale).toFloat() + height / 2f + panOffset.y
                    return Offset(dx, dy)
                }

                // 1. Draw Heatmap Radial Glow Blobs
                for (reading in filteredReadings) {
                    val pos = mapToScreen(reading.latitude, reading.longitude)
                    val signalColor = when {
                        reading.signalStrengthDbm >= -85 -> SignalExcellent
                        reading.signalStrengthDbm >= -98 -> SignalGood
                        reading.signalStrengthDbm >= -110 -> SignalFair
                        else -> SignalPoor
                    }

                    // Outer Heatmap Radial Gradient
                    val glowRadius = 45.dp.toPx() * zoomLevel
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                signalColor.copy(alpha = 0.40f),
                                signalColor.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = pos,
                            radius = glowRadius
                        ),
                        radius = glowRadius,
                        center = pos
                    )
                }

                // 2. Draw Connection Trajectory Lines between chronological readings
                if (filteredReadings.size > 1) {
                    val sorted = filteredReadings.sortedBy { it.timestamp }
                    val path = Path()
                    var first = true
                    for (r in sorted) {
                        val pt = mapToScreen(r.latitude, r.longitude)
                        if (first) {
                            path.moveTo(pt.x, pt.y)
                            first = false
                        } else {
                            path.lineTo(pt.x, pt.y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = TechCyan.copy(alpha = 0.35f),
                        style = Stroke(width = 2.dp.toPx() * zoomLevel)
                    )
                }

                // 3. Draw Coverage Point Pins
                for (reading in filteredReadings) {
                    val pos = mapToScreen(reading.latitude, reading.longitude)
                    val isSelected = selectedReading?.id == reading.id

                    val signalColor = when {
                        reading.signalStrengthDbm >= -85 -> SignalExcellent
                        reading.signalStrengthDbm >= -98 -> SignalGood
                        reading.signalStrengthDbm >= -110 -> SignalFair
                        else -> SignalPoor
                    }

                    // Solid Inner Marker
                    val nodeRadius = if (isSelected) 12.dp.toPx() else 7.dp.toPx()
                    drawCircle(
                        color = Color.White,
                        radius = nodeRadius + 3.dp.toPx(),
                        center = pos
                    )
                    drawCircle(
                        color = signalColor,
                        radius = nodeRadius,
                        center = pos
                    )
                }

                // 4. Draw Current User Location Pulse Marker
                val userPos = mapToScreen(currentLat, currentLng)
                drawCircle(
                    color = TechBlueLight.copy(alpha = 0.3f),
                    radius = 20.dp.toPx(),
                    center = userPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = userPos
                )
                drawCircle(
                    color = TechBlue,
                    radius = 6.dp.toPx(),
                    center = userPos
                )
            }

            // Map Controls Overlay (Top)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "5G", "4G LTE", "High Speed").forEach { chip ->
                            FilterChip(
                                selected = selectedFilter == chip,
                                onClick = { selectedFilter = chip },
                                label = { Text(chip, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TechBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Map Style Switcher
                    IconButton(
                        onClick = { isSatelliteMode = !isSatelliteMode },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (isSatelliteMode) Icons.Default.Layers else Icons.Default.Map,
                            contentDescription = "Map Style",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Zoom & Recenter Floating Buttons (Right side)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { zoomLevel = (zoomLevel * 1.3f).coerceAtMost(4.0f) },
                    modifier = Modifier.size(40.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }

                FloatingActionButton(
                    onClick = { zoomLevel = (zoomLevel / 1.3f).coerceAtLeast(0.5f) },
                    modifier = Modifier.size(40.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }

                FloatingActionButton(
                    onClick = {
                        panOffset = Offset.Zero
                        zoomLevel = 1.0f
                    },
                    modifier = Modifier.size(40.dp),
                    containerColor = TechBlue
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Recenter",
                        tint = Color.White
                    )
                }
            }

            // Selected Reading Popup Card (Bottom)
            if (filteredReadings.isNotEmpty()) {
                val activeReading = selectedReading ?: filteredReadings.first()
                val dateFormat = remember { SimpleDateFormat("HH:mm - MMM dd", Locale.getDefault()) }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                ) {
                    Card(
                        onClick = {
                            // Cycle to next point
                            val idx = filteredReadings.indexOf(activeReading)
                            val nextIdx = (idx + 1) % filteredReadings.size
                            selectedReading = filteredReadings[nextIdx]
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(activeReading.signalQuality.hexColor)
                                    ) {
                                        Text(
                                            text = "${activeReading.signalStrengthDbm} dBm",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = activeReading.networkType,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${activeReading.operatorName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Lat: ${String.format("%.4f", activeReading.latitude)}, Lng: ${String.format("%.4f", activeReading.longitude)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "DL: ${activeReading.downloadMbps} Mbps | UL: ${activeReading.uploadMbps} Mbps | Ping: ${activeReading.pingMs}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TechCyan
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = dateFormat.format(Date(activeReading.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Tap next",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "Next",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

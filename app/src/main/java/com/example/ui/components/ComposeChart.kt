package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NetworkReading
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SignalTrendChart(
    readings: List<NetworkReading>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("signal_trend_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Signal Strength Over Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "dBm level trends across historical readings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SignalExcellent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Signal (dBm)",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = SignalExcellent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (readings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No readings logged yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val sorted = remember(readings) { readings.sortedBy { it.timestamp } }
                val minDbm = -120f
                val maxDbm = -50f

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val padLeft = 40.dp.toPx()
                    val padBottom = 24.dp.toPx()
                    val chartW = w - padLeft
                    val chartH = h - padBottom

                    // Draw Horizontal Reference Lines (-60, -85, -100 dBm)
                    val refLevels = listOf(-60, -85, -100)
                    for (level in refLevels) {
                        val normY = (level - minDbm) / (maxDbm - minDbm)
                        val y = chartH * (1f - normY)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(padLeft, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Build Chart Path
                    val points = mutableListOf<Offset>()
                    val stepX = if (sorted.size > 1) chartW / (sorted.size - 1) else chartW / 2f

                    for (i in sorted.indices) {
                        val dbm = sorted[i].signalStrengthDbm.toFloat().coerceIn(minDbm, maxDbm)
                        val normY = (dbm - minDbm) / (maxDbm - minDbm)
                        val x = padLeft + (i * stepX)
                        val y = chartH * (1f - normY)
                        points.add(Offset(x, y))
                    }

                    if (points.isNotEmpty()) {
                        val linePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }

                        // Gradient Area Fill
                        val fillPath = Path().apply {
                            addPath(linePath)
                            lineTo(points.last().x, chartH)
                            lineTo(points[0].x, chartH)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    TechCyan.copy(alpha = 0.35f),
                                    TechCyan.copy(alpha = 0.02f)
                                )
                            )
                        )

                        // Line Stroke
                        drawPath(
                            path = linePath,
                            color = TechCyan,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Point Markers
                        for (pt in points) {
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = TechCyan,
                                radius = 2.5.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpeedTrendChart(
    readings: List<NetworkReading>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("speed_trend_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Throughput Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Download & Upload speed trends (Mbps)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TechBlue.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Download",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TechBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SignalFair.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Upload",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = SignalFair,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (readings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No speed tests recorded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val sorted = remember(readings) { readings.sortedBy { it.timestamp } }
                val maxSpeed = remember(sorted) {
                    val highest = sorted.maxOfOrNull { maxOf(it.downloadMbps, it.uploadMbps) } ?: 50.0
                    (highest * 1.2).toFloat().coerceAtLeast(10f)
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val padLeft = 10.dp.toPx()
                    val padBottom = 10.dp.toPx()
                    val chartW = w - padLeft
                    val chartH = h - padBottom

                    val stepX = if (sorted.size > 1) chartW / (sorted.size - 1) else chartW / 2f

                    val dlPoints = mutableListOf<Offset>()
                    val ulPoints = mutableListOf<Offset>()

                    for (i in sorted.indices) {
                        val x = padLeft + (i * stepX)
                        val dlY = chartH * (1f - (sorted[i].downloadMbps.toFloat() / maxSpeed).coerceIn(0f, 1f))
                        val ulY = chartH * (1f - (sorted[i].uploadMbps.toFloat() / maxSpeed).coerceIn(0f, 1f))
                        dlPoints.add(Offset(x, dlY))
                        ulPoints.add(Offset(x, ulY))
                    }

                    // Draw Download Line
                    if (dlPoints.size > 1) {
                        val dlPath = Path().apply {
                            moveTo(dlPoints[0].x, dlPoints[0].y)
                            for (p in dlPoints) lineTo(p.x, p.y)
                        }
                        drawPath(
                            path = dlPath,
                            color = TechBlue,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Draw Upload Line
                    if (ulPoints.size > 1) {
                        val ulPath = Path().apply {
                            moveTo(ulPoints[0].x, ulPoints[0].y)
                            for (p in ulPoints) lineTo(p.x, p.y)
                        }
                        drawPath(
                            path = ulPath,
                            color = SignalFair,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Dots
                    for (p in dlPoints) {
                        drawCircle(color = TechBlue, radius = 3.5.dp.toPx(), center = p)
                    }
                    for (p in ulPoints) {
                        drawCircle(color = SignalFair, radius = 3.dp.toPx(), center = p)
                    }
                }
            }
        }
    }
}

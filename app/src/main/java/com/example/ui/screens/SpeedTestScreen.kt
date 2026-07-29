package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.collector.SpeedTestState
import com.example.ui.components.MetricTile
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoverageViewModel

@Composable
fun SpeedTestScreen(
    viewModel: CoverageViewModel,
    modifier: Modifier = Modifier
) {
    val speedState by viewModel.speedTestState.collectAsState()

    val isRunning = speedState is SpeedTestState.TestingPing ||
            speedState is SpeedTestState.TestingDownload ||
            speedState is SpeedTestState.TestingUpload

    val currentMbps = when (val s = speedState) {
        is SpeedTestState.TestingDownload -> s.currentMbps
        is SpeedTestState.TestingUpload -> s.currentMbps
        is SpeedTestState.Completed -> s.downloadMbps
        else -> 0.0
    }

    val progress = when (val s = speedState) {
        is SpeedTestState.TestingPing -> s.progress
        is SpeedTestState.TestingDownload -> s.progress
        is SpeedTestState.TestingUpload -> s.progress
        is SpeedTestState.Completed -> 1.0f
        else -> 0.0f
    }

    val animatedSpeed by animateFloatAsState(
        targetValue = currentMbps.toFloat(),
        animationSpec = tween(durationMillis = 400),
        label = "speedometer"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("speed_test_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Speed Gauge Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (speedState) {
                        is SpeedTestState.TestingPing -> "Measuring Latency (Ping)..."
                        is SpeedTestState.TestingDownload -> "Testing Download Speed..."
                        is SpeedTestState.TestingUpload -> "Testing Upload Speed..."
                        is SpeedTestState.Completed -> "Speed Test Completed & Saved!"
                        else -> "Network Speed Test"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Circular Speedometer Gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp, 160.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val stroke = 22.dp.toPx()
                        val arcSize = Size(w - stroke, h * 2 - stroke)
                        val topLeft = Offset(stroke / 2, stroke / 2)

                        // Track
                        drawArc(
                            color = Color.Gray.copy(alpha = 0.15f),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        // Active Arc (0 to 150 Mbps max scale)
                        val frac = (animatedSpeed / 150f).coerceIn(0f, 1f)
                        drawArc(
                            brush = Brush.horizontalGradient(
                                colors = listOf(TechCyan, TechBlue, SignalExcellent)
                            ),
                            startAngle = 180f,
                            sweepAngle = 180f * frac,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 36.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", animatedSpeed),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Mbps",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Test Progress Bar
                if (isRunning) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = TechBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start Test Button
                Button(
                    onClick = {
                        if (isRunning) viewModel.resetSpeedTest() else viewModel.startSpeedTest()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_speed_test_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.error else TechBlue
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Cancel else Icons.Default.PlayArrow,
                        contentDescription = "Run Test"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Cancel Test" else "Start Speed Test",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Metrics Breakdown Grid
        val completedState = speedState as? SpeedTestState.Completed
        val currentPing = when (val s = speedState) {
            is SpeedTestState.TestingPing -> s.currentPingMs
            is SpeedTestState.Completed -> s.pingMs
            else -> 0
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                title = "Download",
                value = completedState?.downloadMbps?.toString() ?: if (speedState is SpeedTestState.TestingDownload) String.format("%.1f", (speedState as SpeedTestState.TestingDownload).currentMbps) else "--",
                unit = "Mbps",
                icon = Icons.Default.Download,
                iconColor = TechBlue,
                modifier = Modifier.weight(1f)
            )

            MetricTile(
                title = "Upload",
                value = completedState?.uploadMbps?.toString() ?: if (speedState is SpeedTestState.TestingUpload) String.format("%.1f", (speedState as SpeedTestState.TestingUpload).currentMbps) else "--",
                unit = "Mbps",
                icon = Icons.Default.Upload,
                iconColor = SignalFair,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                title = "Latency (Ping)",
                value = if (currentPing > 0) "$currentPing" else "--",
                unit = "ms",
                icon = Icons.Default.Timer,
                iconColor = SignalExcellent,
                modifier = Modifier.weight(1f)
            )

            MetricTile(
                title = "Jitter",
                value = completedState?.jitterMs?.toString() ?: "--",
                unit = "ms",
                icon = Icons.Default.GraphicEq,
                iconColor = TechCyan,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

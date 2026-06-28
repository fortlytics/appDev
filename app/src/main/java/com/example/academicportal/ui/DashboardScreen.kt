package com.example.academicportal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicportal.model.GradeUtils
import com.example.academicportal.model.GradingSystem
import com.example.academicportal.model.Semester

@Composable
fun DashboardScreen(
    studentName: String,
    gradingSystem: GradingSystem,
    semesters: List<Semester>,
    cgpa: Double,
    totalCredits: Int,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val classification = GradeUtils.getClassification(cgpa, gradingSystem)
    val maxScale = if (gradingSystem == GradingSystem.UNIVERSAL_5_0) 5.0 else 4.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome Back, $studentName",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Here's your current academic overview under the ${gradingSystem.name} standard",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Metrics Grid (2x2 or row based)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // CGPA Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "CUMULATIVE CGPA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = String.format("%.2f", cgpa),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color(0xFF4F46E5),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Scale Limit: ${String.format("%.1f", maxScale)}",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Total Credits Completed Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TOTAL CREDITS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$totalCredits CU",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color(0xFF0F172A),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Units Completed",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Diploma Standing Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "DIPLOMA CLASS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = classification.className,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(android.graphics.Color.parseColor(classification.colorHex))
                )
                Text(
                    text = "Academic Tier Award",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Academic Standing Alert Box
            val isProbation = cgpa < 2.0
            val standingText = if (isProbation) "ACADEMIC PROBATION" else "GOOD STANDING"
            val standingDesc = if (isProbation) "GPA under 2.00 standard!" else "Cleared of all warning bars."
            val standingColor = if (isProbation) Color(0xFFDC2626) else Color(0xFF059669)
            val standingBg = if (isProbation) Color(0xFFFEF2F2) else Color(0xFFECFDF5)
            val standingBorder = if (isProbation) Color(0xFFFCA5A5) else Color(0xFFA7F3D0)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(standingBg, RoundedCornerShape(12.dp))
                    .border(1.dp, standingBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isProbation) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = standingColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "ACADEMIC STANDING",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = standingText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = standingColor
                )
                Text(
                    text = standingDesc,
                    fontSize = 10.sp,
                    color = standingColor.copy(alpha = 0.8f)
                )
            }
        }

        // GPA Trend Custom Canvas Area Curve
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SEMESTER GPA TREND",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF1E293B),
                letterSpacing = 0.5.sp
            )

            if (semesters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No semester records found. Add semesters and courses to generate trend charts.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            } else {
                // Let's draw an elegant Area Chart with Canvas API
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    
                    // Margins
                    val leftPadding = 50f
                    val rightPadding = 30f
                    val topPadding = 20f
                    val bottomPadding = 40f
                    
                    val plotWidth = canvasWidth - leftPadding - rightPadding
                    val plotHeight = canvasHeight - topPadding - bottomPadding
                    
                    // Draw grid lines (horizontal representing GPAs 1.0, 2.0, 3.0, 4.0, 5.0)
                    val linesCount = maxScale.toInt()
                    for (i in 0..linesCount) {
                        val y = topPadding + plotHeight - (i * plotHeight / maxScale).toFloat()
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(leftPadding, y),
                            end = Offset(canvasWidth - rightPadding, y),
                            strokeWidth = 1f
                        )
                    }

                    // Map coordinate points
                    val points = semesters.mapIndexed { idx, sem ->
                        val x = if (semesters.size > 1) {
                            leftPadding + (idx * plotWidth / (semesters.size - 1))
                        } else {
                            leftPadding + (plotWidth / 2)
                        }
                        val y = topPadding + plotHeight - (sem.gpa * plotHeight / maxScale).toFloat()
                        Offset(x, y)
                    }

                    // Draw filled Area with smooth brush
                    if (points.isNotEmpty()) {
                        val fillPath = Path().apply {
                            moveTo(points.first().x, topPadding + plotHeight)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, topPadding + plotHeight)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF4F46E5).copy(alpha = 0.35f), Color.White.copy(alpha = 0.05f)),
                                startY = topPadding,
                                endY = topPadding + plotHeight
                            )
                        )

                        // Draw Curve Stroke
                        val strokePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = strokePath,
                            color = Color(0xFF4F46E5),
                            style = Stroke(width = 4f)
                        )

                        // Draw coordinate dot nodes
                        points.forEach { point ->
                            drawCircle(
                                color = Color.White,
                                radius = 8f,
                                center = point
                            )
                            drawCircle(
                                color = Color(0xFF4F46E5),
                                radius = 5f,
                                center = point
                            )
                        }
                    }
                }
                
                // Semester X labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    semesters.forEachIndexed { idx, sem ->
                        val shortName = if (sem.name.length > 10) sem.name.substring(0, 8) + ".." else sem.name
                        Text(
                            text = shortName,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.width(55.dp),
                            lineHeight = 10.sp
                        )
                    }
                }
            }
        }

        // Grade Distribution Widget
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "GRADE DISTRIBUTION",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF1E293B),
                letterSpacing = 0.5.sp
            )

            // Aggregate course grades
            val allCourses = semesters.flatMap { it.courses }
            if (allCourses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No grade records logged yet.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            } else {
                val gradeCounts = allCourses.groupBy { it.grade }.mapValues { it.value.size }
                val maxCount = gradeCounts.values.maxOrNull() ?: 1

                val sortedGrades = listOf("A", "AB", "B", "BC", "C", "CD", "D", "E", "F")
                    .filter { gradeCounts.containsKey(it) || it in listOf("A", "B", "C", "D", "F") }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortedGrades.forEach { grade ->
                        val count = gradeCounts[grade] ?: 0
                        val percentage = if (allCourses.isNotEmpty()) count.toFloat() / allCourses.size else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = grade,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier.width(30.dp),
                                fontFamily = FontFamily.Monospace
                            )

                            // Horizontal progress line representing frequency
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(12.dp)
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = if (maxCount > 0) count.toFloat() / maxCount else 0f)
                                        .background(
                                            color = when (grade) {
                                                "A", "AB" -> Color(0xFF10B981)
                                                "B", "BC" -> Color(0xFF3B82F6)
                                                "C", "CD" -> Color(0xFFF59E0B)
                                                "D", "E" -> Color(0xFF94A3B8)
                                                else -> Color(0xFFEF4444)
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                )
                            }

                            Text(
                                text = "$count Courses",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                modifier = Modifier.width(60.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        // Side by side semester comparison table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "DETAILED SEMESTER SIDE-BY-SIDE COMPARISON",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF1E293B),
                letterSpacing = 0.5.sp
            )

            if (semesters.isEmpty()) {
                Text(
                    text = "No semesters recorded yet. Please head to Semesters tab to log course scores.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Semester Period", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.5f))
                        Text("Courses", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.weight(0.8f))
                        Text("Credits", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.weight(0.9f))
                        Text("GPA", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.weight(0.8f))
                        Text("Class", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.2f))
                    }

                    // Rows
                    semesters.forEach { sem ->
                        val semClass = GradeUtils.getClassification(sem.gpa, gradingSystem)
                        val totalCreditsInSem = sem.courses.sumOf { it.credits }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sem.name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), modifier = Modifier.weight(1.5f))
                            Text("${sem.courses.size} Logged", fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.8f))
                            Text("$totalCreditsInSem CU", fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.9f))
                            Text(String.format("%.2f", sem.gpa), fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0F172A), modifier = Modifier.weight(0.8f))
                            
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .background(Color(android.graphics.Color.parseColor(semClass.badgeBgHex)).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(vertical = 2.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = semClass.className,
                                    color = Color(android.graphics.Color.parseColor(semClass.colorHex)),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

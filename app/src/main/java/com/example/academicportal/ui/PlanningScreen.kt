package com.example.academicportal.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicportal.model.GradeUtils
import com.example.academicportal.model.GradingSystem

@Composable
fun PlanningScreen(
    gradingSystem: GradingSystem,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val thresholds = GradeUtils.getThresholds(gradingSystem)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Proposed System Scope & Case Study Goals
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "PROPOSED SYSTEM SCOPE & CASE STUDY GOALS",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B),
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "This academic application is tailored to transition from simple single-GPA calculators to a comprehensive academic portfolio tracker, structured around the actual standard of the National Board for Technical Education (NBTE) of Nigeria, used at the Federal Polytechnic Offa.",
                    color = Color(0xFF475569),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                // Features Box
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Core Features Implemented",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color(0xFF1E293B)
                        )

                        val features = listOf(
                            "Automated GPA & CGPA: Inputs automatically convert from numerical scores to letter grades and GP values, updating stats immediately.",
                            "Interactive Course Managers: Easily organize academic history by semester. Add, edit, or delete items on-the-fly.",
                            "Rich Trend Analytics: Visual charts trace GP progress, showing peaks and areas requiring effort.",
                            "AI Academic Advising: Seamlessly consults Gemini AI models to analyze scores and suggest tailored study blueprints."
                        )

                        features.forEach { feature ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(5.dp)
                                        .background(Color(0xFF4F46E5), RoundedCornerShape(50))
                                )
                                Text(
                                    text = feature,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Target Audience Box
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Target Audience Segments",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color(0xFF1E293B)
                        )

                        val audiences = listOf(
                            "Polytechnic Students: Actively manage scores, run projections (e.g., \"What score is needed to secure Distinction?\").",
                            "Academic Advisors & Counselors: Conduct progress audits, write statements of results, track student trajectory.",
                            "Departmental Registrars: Quick local GPA computations without demanding full central server access."
                        )

                        audiences.forEach { aud ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(5.dp)
                                        .background(Color(0xFF4F46E5), RoundedCornerShape(50))
                                )
                                Text(
                                    text = aud,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Academic Setup & Grade Scheme Mapping
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "GRADE SCHEME MAPPING (${gradingSystem.name})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E293B),
                    letterSpacing = 0.5.sp
                )

                // Horizontal flowing/grid listing of thresholds
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    thresholds.chunked(3).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { t ->
                                OutlinedCard(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = t.grade,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF0F172A),
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "GP: ${String.format("%.2f", t.gp)}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Min: ${t.minScore}%",
                                            fontSize = 9.sp,
                                            color = Color(0xFF94A3B8),
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            // Pad row with empty spacers if less than 3 elements
                            repeat(3 - chunk.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Graduation Requirements Box
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "GRADUATION REQUIREMENTS",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B),
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Under the National Board for Technical Education (NBTE) standard for polytechnic programmes (ND/HND):",
                    color = Color(0xFF475569),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2F6)),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "\"To qualify for graduation, a student must score a minimum Cumulative GPA of 2.00 in all course credits registered.\"",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1B4B),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(start = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val rules = listOf(
                        "Standard Grade Letter weight values (A=4.00, AB=3.50, B=3.25, BC=3.00, C=2.75, CD=2.50, D=2.25, E=2.00, F=0.00).",
                        "Cumulative CGPA determines the Class of Diploma: Distinction (3.50+), Upper Credit (3.00 - 3.49), Lower Credit (2.50 - 2.99), Pass (2.00 - 2.49).",
                        "A CGPA below 2.00 triggers active Academic Probation, requiring warning notifications."
                    )

                    rules.forEach { rule ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(4.dp)
                                    .background(Color(0xFF64748B), RoundedCornerShape(50))
                            )
                            Text(
                                text = rule,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

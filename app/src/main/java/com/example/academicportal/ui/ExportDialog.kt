package com.example.academicportal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.academicportal.model.GradeUtils
import com.example.academicportal.model.GradingSystem
import com.example.academicportal.model.Semester

@Composable
fun ExportDialog(
    studentName: String,
    matricNo: String,
    department: String,
    level: String,
    institution: String,
    gradingSystem: GradingSystem,
    semesters: List<Semester>,
    cgpa: Double,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val totalCredits = semesters.flatMap { it.courses }.sumOf { it.credits }
    val totalGradePoints = semesters.flatMap { it.courses }.sumOf { it.gp * it.credits }
    val diplomaClass = GradeUtils.getClassification(cgpa, gradingSystem).className
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Statement of Result Preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Close Document", fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Printable Certificate Content (Scrollable if screen size is small)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFCBD5E1))
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // School Letterhead Banner
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = institution.uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E1B4B),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "KWARA STATE, NIGERIA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color(0xFF1E1B4B))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "OFFICIAL STATEMENT OF ACADEMIC RESULT SUMMARY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF1E293B),
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Student Identification Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0))
                            .background(Color(0xFFF8FAFC))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("STUDENT NAME: ", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.width(110.dp))
                            Text(studentName.uppercase(), fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF0F172A))
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("MATRICULATION NO: ", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.width(110.dp))
                            Text(matricNo.uppercase(), fontWeight = FontWeight.Bold, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF0F172A))
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("DEPARTMENT: ", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.width(110.dp))
                            Text(department.uppercase(), fontSize = 9.sp, color = Color(0xFF0F172A))
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("CURRENT LEVEL: ", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.width(110.dp))
                            Text(level.uppercase(), fontSize = 9.sp, color = Color(0xFF0F172A))
                        }
                    }

                    // Course Transcripts Loop
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        semesters.forEach { sem ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = sem.name.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = Color(0xFF1E1B4B)
                                )
                                
                                // Table Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFEEF2F6))
                                        .padding(4.dp)
                                ) {
                                    Text("Code", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFF334155), modifier = Modifier.weight(1.2f))
                                    Text("Course Title", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFF334155), modifier = Modifier.weight(3f))
                                    Text("CU", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFF334155), modifier = Modifier.weight(0.6f))
                                    Text("Score", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFF334155), modifier = Modifier.weight(0.8f))
                                    Text("Grade", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFF334155), modifier = Modifier.weight(0.8f))
                                }

                                // Table Rows
                                sem.courses.forEach { course ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 4.dp)
                                    ) {
                                        Text(course.code, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0F172A), modifier = Modifier.weight(1.2f))
                                        Text(course.title, fontSize = 8.sp, color = Color(0xFF334155), modifier = Modifier.weight(3f))
                                        Text("${course.credits}", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF334155), modifier = Modifier.weight(0.6f))
                                        Text("${course.score}%", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF334155), modifier = Modifier.weight(0.8f))
                                        Text("${course.grade} (${String.format("%.1f", course.gp)})", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF0F172A), modifier = Modifier.weight(0.8f))
                                    }
                                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                                }
                            }
                        }
                    }

                    // Performance Certification Block
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF1E1B4B))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "OFFICIAL GRADUATION RATING CERTIFICATION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color(0xFF1E1B4B),
                            letterSpacing = 0.3.sp
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Total Registered Credits (Units):", fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.5f))
                            Text("$totalCredits CU", fontWeight = FontWeight.Bold, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Cumulative Grade Points (GP):", fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.5f))
                            Text(String.format("%.2f", totalGradePoints), fontWeight = FontWeight.Bold, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Cumulative CGPA:", fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.5f))
                            Text(String.format("%.2f", cgpa), fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color(0xFF4F46E5), fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Class of Degree/Diploma:", fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.5f))
                            Text(diplomaClass.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color(0xFF1E1B4B), modifier = Modifier.weight(1f))
                        }
                    }

                    // Official Stamp Signatures Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(1.dp)
                                    .background(Color(0xFF94A3B8))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Student Signature", fontSize = 8.sp, color = Color(0xFF64748B))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "APPROVED OFFICIAL",
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 8.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(1.dp)
                                    .background(Color(0xFF94A3B8))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Dean / Academic Registrar", fontSize = 8.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }
}

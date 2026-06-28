package com.example.academicportal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicportal.model.Course
import com.example.academicportal.model.GradeUtils
import com.example.academicportal.model.GradingSystem
import com.example.academicportal.model.Semester

@Composable
fun CoursesScreen(
    gradingSystem: GradingSystem,
    semesters: List<Semester>,
    onAddSemester: (String) -> Unit,
    onDeleteSemester: (String) -> Unit,
    onAddCourse: (String, String, String, Int, Int) -> Unit,
    onDeleteCourse: (String, String) -> Unit,
    onUpdateCourse: (String, String, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    // Dialog states
    var showAddSemesterDialog by remember { mutableStateOf(false) }
    var newSemesterName by remember { mutableStateOf("") }

    var showCourseDialog by remember { mutableStateOf(false) }
    var activeSemesterIdForCourse by remember { mutableStateOf<String?>(null) }
    var editingCourse by remember { mutableStateOf<Course?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MANAGE SEMESTER PORTFOLIOS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E293B),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Add new terms and click cards below to edit courses & scores",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Button(
                onClick = { showAddSemesterDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Semester", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // Semesters loop
        if (semesters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No Semester logs registered. Click 'Add Semester' to begin.", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
        } else {
            semesters.forEach { sem ->
                val semClass = GradeUtils.getClassification(sem.gpa, gradingSystem)
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Semester Bar Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = sem.name.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = 0.5.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(android.graphics.Color.parseColor(semClass.badgeBgHex)).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "GPA: ${String.format("%.2f", sem.gpa)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = Color(android.graphics.Color.parseColor(semClass.colorHex))
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Add Course Action Button
                            IconButton(
                                onClick = {
                                    activeSemesterIdForCourse = sem.id
                                    editingCourse = null
                                    showCourseDialog = true
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White, RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Course", tint = Color(0xFF4F46E5), modifier = Modifier.size(16.dp))
                            }

                            // Delete Semester Button
                            IconButton(
                                onClick = { onDeleteSemester(sem.id) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White, RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Semester", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                    // Semester Courses Table
                    if (sem.courses.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No courses registered in this semester. Click '+' to add.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Headers
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            ) {
                                Text("Code", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.weight(1.2f))
                                Text("Course Title", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.weight(2.5f))
                                Text("CU", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.5f))
                                Text("Score", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.8f))
                                Text("Grade", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.8f))
                                Text("Actions", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.weight(1.0f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }

                            // Course Rows
                            sem.courses.forEach { course ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(course.code, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E293B), fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.2f))
                                    Text(course.title, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.weight(2.5f))
                                    Text("${course.credits}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF475569), modifier = Modifier.weight(0.5f))
                                    Text("${course.score}%", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF475569), modifier = Modifier.weight(0.8f))
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(0.8f)
                                            .padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = "${course.grade} (${String.format("%.2f", course.gp)})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = when (course.grade) {
                                                "A", "AB" -> Color(0xFF059669)
                                                "B", "BC" -> Color(0xFF2563EB)
                                                "C", "CD" -> Color(0xFFD97706)
                                                "D", "E" -> Color(0xFF64748B)
                                                else -> Color(0xFFDC2626)
                                            }
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.weight(1.0f),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Edit Icon Button
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable {
                                                    activeSemesterIdForCourse = sem.id
                                                    editingCourse = course
                                                    showCourseDialog = true
                                                }
                                                .padding(4.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        // Delete Icon Button
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable {
                                                    onDeleteCourse(sem.id, course.id)
                                                }
                                                .padding(4.dp)
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
    }

    // Modal Dialog: Add Semester
    if (showAddSemesterDialog) {
        AlertDialog(
            onDismissRequest = { showAddSemesterDialog = false },
            title = { Text("New Semester Setup", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the name or registration term for the semester portfolio:", fontSize = 11.sp, color = Color(0xFF64748B))
                    OutlinedTextField(
                        value = newSemesterName,
                        onValueChange = { newSemesterName = it },
                        placeholder = { Text("e.g. ND II - 2nd Semester", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSemesterName.isNotBlank()) {
                            onAddSemester(newSemesterName)
                            newSemesterName = ""
                            showAddSemesterDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Save", fontSize = 11.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSemesterDialog = false }) {
                    Text("Cancel", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Modal Dialog: Add/Edit Course
    if (showCourseDialog && activeSemesterIdForCourse != null) {
        var courseCode by remember { mutableStateOf(editingCourse?.code ?: "") }
        var courseTitle by remember { mutableStateOf(editingCourse?.title ?: "") }
        var courseCredits by remember { mutableStateOf(editingCourse?.credits ?: 3) }
        var courseScoreStr by remember { mutableStateOf(editingCourse?.score?.toString() ?: "75") }

        // Live prediction computation
        val scoreInt = courseScoreStr.toIntOrNull() ?: 0
        val (predGrade, predGp) = GradeUtils.computeGradeAndGp(scoreInt, gradingSystem)

        AlertDialog(
            onDismissRequest = { showCourseDialog = false },
            title = {
                Text(
                    text = if (editingCourse == null) "Add Academic Course Log" else "Edit Academic Course Log",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Course Code", fontSize = 10.sp) },
                        placeholder = { Text("e.g. COM 213", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    )

                    OutlinedTextField(
                        value = courseTitle,
                        onValueChange = { courseTitle = it },
                        label = { Text("Course Title", fontSize = 10.sp) },
                        placeholder = { Text("e.g. Operating Systems", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Credit Units", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Simple selection slider or credit counter
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { if (courseCredits > 1) courseCredits-- },
                                    modifier = Modifier.size(28.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold)
                                }
                                Text("$courseCredits", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                IconButton(
                                    onClick = { if (courseCredits < 6) courseCredits++ },
                                    modifier = Modifier.size(28.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Score (%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = courseScoreStr,
                                onValueChange = {
                                    if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..100)) {
                                        courseScoreStr = it
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    // Live Grade Projection Panel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEF2F6), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grade Projection:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                        Text(
                            text = "$predGrade (GP: ${String.format("%.2f", predGp)})",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF4F46E5),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (courseCode.isNotBlank() && courseTitle.isNotBlank()) {
                            val targetSemId = activeSemesterIdForCourse!!
                            val targetCourse = editingCourse
                            if (targetCourse == null) {
                                onAddCourse(targetSemId, courseCode, courseTitle, courseCredits, scoreInt)
                            } else {
                                onUpdateCourse(targetSemId, targetCourse.id, courseCredits, scoreInt)
                            }
                            showCourseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Save", fontSize = 11.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCourseDialog = false }) {
                    Text("Cancel", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

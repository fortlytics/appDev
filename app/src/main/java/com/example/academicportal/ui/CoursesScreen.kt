package com.example.academicportal.ui

import androidx.compose.foundation.BorderStroke
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
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false
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
            Column(modifier = if (isReadOnly) Modifier.fillMaxWidth() else Modifier.weight(1f)) {
                Text(
                    text = if (isReadOnly) "OFFICIAL ACADEMIC LEDGER" else "MANAGE SEMESTER PORTFOLIOS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E293B),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (isReadOnly) "Certified academic records logged by your designated Exam Officer" else "Add new terms and click cards below to edit courses & scores",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (!isReadOnly) {
                Spacer(modifier = Modifier.width(8.dp))
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
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Semester Bar Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E1B4B),
                                        letterSpacing = 0.5.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(Color(android.graphics.Color.parseColor(semClass.badgeBgHex)).copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "GPA: ${String.format("%.2f", sem.gpa)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp,
                                            color = Color(android.graphics.Color.parseColor(semClass.colorHex))
                                        )
                                    }
                                }
                            }

                            if (!isReadOnly) {
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
                                            .size(32.dp)
                                            .background(Color.White, RoundedCornerShape(50))
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Course", tint = Color(0xFF4F46E5), modifier = Modifier.size(18.dp))
                                    }

                                    // Delete Semester Button
                                    IconButton(
                                        onClick = { onDeleteSemester(sem.id) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color.White, RoundedCornerShape(50))
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Semester", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                        // Semester Courses List
                        if (sem.courses.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No courses registered in this semester. Click '+' to add.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                sem.courses.forEach { course ->
                                    val gradeColor = when (course.grade) {
                                        "A", "AB" -> Color(0xFF059669)
                                        "B", "BC" -> Color(0xFF2563EB)
                                        "C", "CD" -> Color(0xFFD97706)
                                        "D", "E" -> Color(0xFF64748B)
                                        else -> Color(0xFFDC2626)
                                    }

                                    OutlinedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = course.code.uppercase(),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = Color(0xFF4F46E5),
                                                        letterSpacing = 0.5.sp
                                                    )
                                                    Text(
                                                        text = course.title,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                }

                                                // Grade Label Badge
                                                Box(
                                                    modifier = Modifier
                                                        .background(gradeColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                                        .border(1.dp, gradeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = "${course.grade} (${String.format("%.2f", course.gp)} GP)",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 10.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = gradeColor
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Credit Badge
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFFEEF2F6), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "${course.credits} CU",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = FontFamily.Monospace,
                                                            color = Color(0xFF475569)
                                                        )
                                                    }

                                                    // Score Badge
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFFEEF2F6), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "Score: ${course.score}%",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = FontFamily.Monospace,
                                                            color = Color(0xFF475569)
                                                        )
                                                    }
                                                }

                                                if (!isReadOnly) {
                                                    // Actions
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                activeSemesterIdForCourse = sem.id
                                                                editingCourse = course
                                                                showCourseDialog = true
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = "Edit",
                                                                tint = Color(0xFF4F46E5),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }

                                                        IconButton(
                                                            onClick = {
                                                                onDeleteCourse(sem.id, course.id)
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Delete",
                                                                tint = Color(0xFFDC2626),
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

package com.example.academicportal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicportal.model.GradingSystem
import com.example.academicportal.model.UserRole
import com.example.academicportal.ui.*
import com.example.academicportal.viewmodel.AcademicViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AcademicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AcademicPortalTheme {
                MainAppEntry(viewModel)
            }
        }
    }
}

@Composable
fun AcademicPortalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF4F46E5),
            secondary = Color(0xFF818CF8),
            background = Color(0xFFF8FAFC),
            surface = Color.White,
            error = Color(0xFFDC2626)
        ),
        content = content
    )
}

@Composable
fun MainAppEntry(viewModel: AcademicViewModel) {
    val session by viewModel.currentSession

    if (session == null) {
        LoginScreen(
            errorMessage = viewModel.errorMessage.value,
            onLogin = { username, password -> viewModel.login(username, password) }
        )
    } else {
        MainLayout(viewModel)
    }
}

@Composable
fun LoginScreen(
    errorMessage: String?,
    onLogin: (String, String) -> Boolean
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var selectedRoleTab by remember { mutableStateOf(0) } // 0 = Student, 1 = Admin

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Elegant App Logo & Title
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF4F46E5), Color(0xFF818CF8))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ACADEMIC PORTAL",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1B4B),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Verify your credentials to access records",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }

            // Elevated Card containing the Login Form
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Role TabRow
                    TabRow(
                        selectedTabIndex = selectedRoleTab,
                        containerColor = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        indicator = { Box(Modifier) }, // Hide default line indicator for clean bento pill style
                        divider = { Box(Modifier) }
                    ) {
                        val tabs = listOf("Student", "Administrator")
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedRoleTab == index
                            Tab(
                                selected = isSelected,
                                onClick = {
                                    selectedRoleTab = index
                                    // Autofill demo accounts based on selected tab for testing convenience
                                    if (index == 0) {
                                        username = "student1"
                                        password = "password123"
                                    } else {
                                        username = "admin"
                                        password = "adminsecure"
                                    }
                                },
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent),
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF64748B)
                                    )
                                }
                            )
                        }
                    }

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (selectedRoleTab == 0) Icons.Default.Person else Icons.Default.AdminPanelSettings,
                                contentDescription = "User Icon",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Icon",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Visibility",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    // Error Message
                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = errorMessage,
                                    color = Color(0xFFDC2626),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Log In Button
                    Button(
                        onClick = { onLogin(username, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Credentials helper card
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFEEF2F6)),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Credentials Info",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Demo Accounts (File-Stored)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "• Student 1: student1 / password123\n• Student 2: student2 / password123\n• Registrar Admin: admin / adminsecure",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF475569),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MainLayout(viewModel: AcademicViewModel) {
    val student by viewModel.studentState
    val semesters by viewModel.semestersState
    val advice by viewModel.advisorAdvice
    val isLoadingAdvisor by viewModel.loadingAdvisor
    val advisorError by viewModel.errorMessage
    val cgpa = viewModel.getCumulativeCGPA()
    val totalCredits = viewModel.getTotalCreditsCompleted()

    val session by viewModel.currentSession
    val selectedAdminStudent by viewModel.adminSelectedStudent
    val allStudentsList by viewModel.allStudentsList

    // Navigation and screen gating based on Role-Based Access Control
    val navItems = if (session?.role == UserRole.ADMIN) {
        listOf(
            Triple(0, "Students", Icons.Default.People),
            Triple(1, "Analytics", Icons.Default.TrendingUp),
            Triple(2, "Grades Ledger", Icons.Default.Book),
            Triple(3, "Exam Office", Icons.Default.SupportAgent)
        )
    } else {
        listOf(
            Triple(0, "Planning", Icons.Default.School),
            Triple(1, "Analytics", Icons.Default.TrendingUp),
            Triple(2, "Official Grades", Icons.Default.Book),
            Triple(3, "Exam Officer", Icons.Default.SupportAgent)
        )
    }

    var selectedTab by remember { mutableStateOf(1) } // Default to Dashboard (Analytics)
    
    // Dialog and Modal triggers
    var showEditProfile by remember { mutableStateOf(false) }
    var showStatementOfResult by remember { mutableStateOf(false) }
    var expandedAdminMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                // Native Styled Top Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ACADEMIC PORTAL",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E1B4B),
                                letterSpacing = 0.5.sp
                            )
                            if (session?.role == UserRole.ADMIN) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEEF2F6), RoundedCornerShape(4.dp))
                                        .padding(vertical = 2.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "ADMIN",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF4F46E5)
                                    )
                                }
                            }
                        }
                        Text(
                            text = student.institution.ifBlank { "Academic Registry" },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Top Toolbar Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Transcript Export Button
                        Button(
                            onClick = { showStatementOfResult = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = "Statement", tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Logout Button
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign Out",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(14.dp)
                              )
                        }
                    }
                }

                // ADMIN SPECIFIC STUDENT SWITCHER BAR (Only render when controlling ledger portfolio)
                if (session?.role == UserRole.ADMIN && allStudentsList.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEF2F6))
                            .clickable { expandedAdminMenu = true }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = "Switch Student",
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Controlling: ${student.name} (${student.matricNo.ifBlank { "No Matric" }})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SWITCH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4F46E5)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color(0xFF4F46E5), modifier = Modifier.size(14.dp))
                        }

                        DropdownMenu(
                            expanded = expandedAdminMenu,
                            onDismissRequest = { expandedAdminMenu = false }
                        ) {
                            allStudentsList.forEach { cred ->
                                val isSelected = selectedAdminStudent == cred.username
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${cred.name} [${cred.matricNo}]",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF1E293B)
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectAdminStudent(cred.username)
                                        expandedAdminMenu = false
                                    }
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                }

                // PROFILE SUMMARY BANNER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .clickable { showEditProfile = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color(0xFF4F46E5), modifier = Modifier.size(14.dp))
                        Text(
                            text = "${student.name} • ${student.matricNo.ifBlank { "No Matric" }} [${student.level}]",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EDIT PROFILE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                        Icon(Icons.Default.ChevronRight, contentDescription = "Edit", tint = Color(0xFF4F46E5), modifier = Modifier.size(10.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier.height(56.dp)
            ) {
                navItems.forEach { (index, title, icon) ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = title, modifier = Modifier.size(20.dp)) },
                        label = { Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF4F46E5),
                            selectedTextColor = Color(0xFF4F46E5),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8),
                            indicatorColor = Color(0xFFEEF2F6)
                        )
                    )
                }
            }
        },
        containerColor = Color(0xFFF1F5F9)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (session?.role == UserRole.ADMIN) {
                when (selectedTab) {
                    0 -> StudentManagementScreen(viewModel = viewModel)
                    1 -> DashboardScreen(
                        studentName = student.name,
                        gradingSystem = student.gradingSystem,
                        semesters = semesters,
                        cgpa = cgpa,
                        totalCredits = totalCredits
                    )
                    2 -> CoursesScreen(
                        gradingSystem = student.gradingSystem,
                        semesters = semesters,
                        onAddSemester = { viewModel.addSemester(it) },
                        onDeleteSemester = { viewModel.deleteSemester(it) },
                        onAddCourse = { semId, code, title, credits, score ->
                            viewModel.addCourse(semId, code, title, credits, score)
                        },
                        onDeleteCourse = { semId, courseId ->
                            viewModel.deleteCourse(semId, courseId)
                        },
                        onUpdateCourse = { semId, courseId, credits, score ->
                            viewModel.updateCourse(semId, courseId, credits, score)
                        },
                        isReadOnly = false
                    )
                    3 -> AdvisorScreen(
                        student = student
                    )
                }
            } else {
                when (selectedTab) {
                    0 -> PlanningScreen(gradingSystem = student.gradingSystem)
                    1 -> DashboardScreen(
                        studentName = student.name,
                        gradingSystem = student.gradingSystem,
                        semesters = semesters,
                        cgpa = cgpa,
                        totalCredits = totalCredits
                    )
                    2 -> CoursesScreen(
                        gradingSystem = student.gradingSystem,
                        semesters = semesters,
                        onAddSemester = {},
                        onDeleteSemester = {},
                        onAddCourse = { _, _, _, _, _ -> },
                        onDeleteCourse = { _, _ -> },
                        onUpdateCourse = { _, _, _, _ -> },
                        isReadOnly = true
                    )
                    3 -> AdvisorScreen(
                        student = student
                    )
                }
            }
        }
    }

    // Modal Dialog: Edit Profile
    if (showEditProfile) {
        var tempName by remember { mutableStateOf(student.name) }
        var tempMatric by remember { mutableStateOf(student.matricNo) }
        var tempDept by remember { mutableStateOf(student.department) }
        var tempLevel by remember { mutableStateOf(student.level) }
        var tempSystem by remember { mutableStateOf(student.gradingSystem) }
        var expandedSystemMenu by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Student Credentials", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Full Name", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )

                    OutlinedTextField(
                        value = tempMatric,
                        onValueChange = { tempMatric = it },
                        label = { Text("Matriculation No", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    )

                    OutlinedTextField(
                        value = tempDept,
                        onValueChange = { tempDept = it },
                        label = { Text("Department", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )

                    OutlinedTextField(
                        value = tempLevel,
                        onValueChange = { tempLevel = it },
                        label = { Text("Current Academic Level", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )

                    // Grading System Selection Dropdown Box
                    Column {
                        Text("Grading Standard System", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEEF2F6), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .clickable { expandedSystemMenu = true }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = when (tempSystem) {
                                    GradingSystem.NBTE -> "NBTE Nigerian Polytechnic Scale (4.0)"
                                    GradingSystem.UNIVERSAL_5_0 -> "NUC Nigerian University Scale (5.0)"
                                    GradingSystem.STANDARD_4_0 -> "Standard US Academic Scale (4.0)"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                             )
                        }

                        DropdownMenu(
                            expanded = expandedSystemMenu,
                            onDismissRequest = { expandedSystemMenu = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("NBTE Polytechnic Standard (4.0)", fontSize = 11.sp) },
                                onClick = { tempSystem = GradingSystem.NBTE; expandedSystemMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("NUC University Standard (5.0)", fontSize = 11.sp) },
                                onClick = { tempSystem = GradingSystem.UNIVERSAL_5_0; expandedSystemMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Standard US Academic Standard (4.0)", fontSize = 11.sp) },
                                onClick = { tempSystem = GradingSystem.STANDARD_4_0; expandedSystemMenu = false }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(tempName, tempMatric, tempDept, tempLevel, tempSystem)
                        showEditProfile = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Save Changes", fontSize = 11.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) {
                    Text("Cancel", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Modal: Statement of Result Printable Export Sheet
    if (showStatementOfResult) {
        ExportDialog(
            studentName = student.name,
            matricNo = student.matricNo,
            department = student.department,
            level = student.level,
            institution = student.institution,
            gradingSystem = student.gradingSystem,
            semesters = semesters,
            cgpa = cgpa,
            onDismiss = { showStatementOfResult = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(
    viewModel: AcademicViewModel,
    modifier: Modifier = Modifier
) {
    val allStudentsList by viewModel.allStudentsList
    val selectedAdminStudent by viewModel.adminSelectedStudent
    
    var searchQuery by remember { mutableStateOf("") }
    var showEnrollDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteUsername by remember { mutableStateOf<String?>(null) }
    
    // Enroll dialog state
    var enrollUsername by remember { mutableStateOf("") }
    var enrollPassword by remember { mutableStateOf("") }
    var enrollName by remember { mutableStateOf("") }
    var enrollMatric by remember { mutableStateOf("") }
    var enrollDept by remember { mutableStateOf("") }
    var enrollLevel by remember { mutableStateOf("ND I") }
    var enrollSystem by remember { mutableStateOf(GradingSystem.NBTE) }
    var enrollInstitution by remember { mutableStateOf("Federal Polytechnic Offa") }
    
    val filteredStudents = allStudentsList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.matricNo.contains(searchQuery, ignoreCase = true) ||
        it.department.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "STUDENT ACCOUNTS DIRECTORY",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF1E293B),
                letterSpacing = 0.5.sp
            )
            
            // Search Input Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, matriculation, or dept...", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, "Search", modifier = Modifier.size(16.dp)) },
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4F46E5),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )
            
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No student accounts found.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filteredStudents.forEach { cred ->
                        val isActive = selectedAdminStudent == cred.username
                        
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isActive) Modifier.border(2.dp, Color(0xFF4F46E5), RoundedCornerShape(12.dp))
                                    else Modifier
                                )
                                .clickable { viewModel.selectAdminStudent(cred.username) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (isActive) Color(0xFFEEF2F6) else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Status dot
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (isActive) Color(0xFF4F46E5) else Color(0xFFCBD5E1),
                                                CircleShape
                                            )
                                    )
                                    Column {
                                        Text(
                                            text = cred.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${cred.matricNo} • ${cred.department} (${cred.level})",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = "Grading: ${cred.gradingSystem}",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF4F46E5), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("CONTROL ACTIVE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                    
                                    IconButton(
                                        onClick = { showConfirmDeleteUsername = cred.username },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Floating trigger to enroll new student
        FloatingActionButton(
            onClick = { showEnrollDialog = true },
            containerColor = Color(0xFF4F46E5),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "Enroll Student")
        }
    }
    
    // Enroll Dialog
    if (showEnrollDialog) {
        AlertDialog(
            onDismissRequest = { showEnrollDialog = false },
            title = { Text("Enroll New Student Account", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = enrollUsername,
                        onValueChange = { enrollUsername = it },
                        label = { Text("Login Username", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )
                    OutlinedTextField(
                        value = enrollPassword,
                        onValueChange = { enrollPassword = it },
                        label = { Text("Login Password", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )
                    OutlinedTextField(
                        value = enrollName,
                        onValueChange = { enrollName = it },
                        label = { Text("Full Student Name", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )
                    OutlinedTextField(
                        value = enrollMatric,
                        onValueChange = { enrollMatric = it },
                        label = { Text("Matriculation Number", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    )
                    OutlinedTextField(
                        value = enrollDept,
                        onValueChange = { enrollDept = it },
                        label = { Text("Academic Department", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )
                    OutlinedTextField(
                        value = enrollLevel,
                        onValueChange = { enrollLevel = it },
                        label = { Text("Current Level (e.g. ND I)", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )
                    OutlinedTextField(
                        value = enrollInstitution,
                        onValueChange = { enrollInstitution = it },
                        label = { Text("Institution Name", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )
                    
                    Column {
                        Text("Grading Standard System", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GradingSystem.values().forEach { sys ->
                                FilterChip(
                                    selected = enrollSystem == sys,
                                    onClick = { enrollSystem = sys },
                                    label = { Text(sys.name, fontSize = 9.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enrollUsername.isNotBlank() && enrollPassword.isNotBlank() && enrollName.isNotBlank()) {
                            val newCred = com.example.academicportal.model.StudentCredential(
                                username = enrollUsername.trim(),
                                password = enrollPassword,
                                name = enrollName.trim(),
                                matricNo = enrollMatric.trim().uppercase(),
                                department = enrollDept.trim(),
                                level = enrollLevel.trim(),
                                gradingSystem = enrollSystem,
                                institution = enrollInstitution.trim()
                            )
                            viewModel.addStudent(newCred)
                            
                            // Clear form
                            enrollUsername = ""
                            enrollPassword = ""
                            enrollName = ""
                            enrollMatric = ""
                            enrollDept = ""
                            enrollLevel = "ND I"
                            
                            showEnrollDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Enroll Portfolio", fontSize = 11.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnrollDialog = false }) {
                    Text("Cancel", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }
    
    // Confirm Delete Dialog
    showConfirmDeleteUsername?.let { deleteUser ->
        val targetStudent = allStudentsList.find { it.username == deleteUser }
        AlertDialog(
            onDismissRequest = { showConfirmDeleteUsername = null },
            title = { Text("Expel Student Account?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
            text = {
                Text(
                    text = "Are you sure you want to expel ${targetStudent?.name ?: deleteUser} and permanently delete all their recorded semester course results? This action is irreversible.",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeStudent(deleteUser)
                        showConfirmDeleteUsername = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Expel & Delete", fontSize = 11.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteUsername = null }) {
                    Text("Cancel", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }
}


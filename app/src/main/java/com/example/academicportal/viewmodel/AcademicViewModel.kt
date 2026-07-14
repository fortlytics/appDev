package com.example.academicportal.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.academicportal.BuildConfig
import com.example.academicportal.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AcademicViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("AcademicPortalPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Authentication States
    private val _currentSession = mutableStateOf<UserSession?>(getSavedSession())
    val currentSession: State<UserSession?> = _currentSession

    private val _adminSelectedStudent = mutableStateOf<String?>(getSavedAdminSelectedStudent())
    val adminSelectedStudent: State<String?> = _adminSelectedStudent

    private val _allStudentsList = mutableStateOf<List<StudentCredential>>(emptyList())
    val allStudentsList: State<List<StudentCredential>> = _allStudentsList

    // Primary Academic States (bound to the active student)
    private val _studentState = mutableStateOf<Student>(getGuestStudent())
    val studentState: State<Student> = _studentState

    private val _semestersState = mutableStateOf<List<Semester>>(emptyList())
    val semestersState: State<List<Semester>> = _semestersState

    private val _advisorAdvice = mutableStateOf<String>("")
    val advisorAdvice: State<String> = _advisorAdvice

    private val _loadingAdvisor = mutableStateOf(false)
    val loadingAdvisor: State<Boolean> = _loadingAdvisor

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        loadCredentialsFromAssets()
        syncStateWithSession()
    }

    private fun getGuestStudent(): Student {
        return Student(
            name = "Guest Student",
            matricNo = "",
            department = "",
            level = "",
            gradingSystem = GradingSystem.NBTE,
            institution = ""
        )
    }

    private fun loadCredentialsFromAssets() {
        try {
            val savedStudentsJson = sharedPrefs.getString("all_students_json", null)
            if (savedStudentsJson != null) {
                val type = object : TypeToken<List<StudentCredential>>() {}.type
                _allStudentsList.value = gson.fromJson(savedStudentsJson, type)
            } else {
                val jsonStr = getApplication<Application>().assets.open("credentials.json").bufferedReader().use { it.readText() }
                val fileData = gson.fromJson(jsonStr, CredentialsFile::class.java)
                _allStudentsList.value = fileData.students
                sharedPrefs.edit().putString("all_students_json", gson.toJson(fileData.students)).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addStudent(student: StudentCredential) {
        val updated = _allStudentsList.value + student
        _allStudentsList.value = updated
        sharedPrefs.edit().putString("all_students_json", gson.toJson(updated)).apply()
    }

    fun removeStudent(username: String) {
        val updated = _allStudentsList.value.filter { it.username != username }
        _allStudentsList.value = updated
        sharedPrefs.edit().putString("all_students_json", gson.toJson(updated)).apply()

        // Purge student-specific academic databases in shared prefs
        sharedPrefs.edit()
            .remove("student_profile_$username")
            .remove("semesters_records_$username")
            .remove("advisor_advice_$username")
            .apply()

        // If currently controlling this student, switch to first available or fallback
        if (_adminSelectedStudent.value == username) {
            val nextAvail = updated.firstOrNull()?.username
            _adminSelectedStudent.value = nextAvail
            if (nextAvail != null) {
                sharedPrefs.edit().putString("admin_selected_student", nextAvail).apply()
            } else {
                sharedPrefs.edit().remove("admin_selected_student").apply()
            }
            syncStateWithSession()
        }
    }

    private fun getSavedSession(): UserSession? {
        val sessionJson = sharedPrefs.getString("user_session", null)
        return if (sessionJson != null) {
            try {
                gson.fromJson(sessionJson, UserSession::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    private fun getSavedAdminSelectedStudent(): String? {
        return sharedPrefs.getString("admin_selected_student", null)
    }

    fun syncStateWithSession() {
        val session = _currentSession.value
        if (session != null) {
            val username = if (session.role == UserRole.ADMIN) {
                _adminSelectedStudent.value ?: _allStudentsList.value.firstOrNull()?.username ?: ""
            } else {
                session.username
            }
            if (username.isNotEmpty()) {
                _studentState.value = getStudentByUsername(username)
                _semestersState.value = getSemestersByUsername(username)
                _advisorAdvice.value = getCachedAdviceByUsername(username)
            } else {
                _studentState.value = getGuestStudent()
                _semestersState.value = emptyList()
                _advisorAdvice.value = ""
            }
        } else {
            _studentState.value = getGuestStudent()
            _semestersState.value = emptyList()
            _advisorAdvice.value = ""
        }
    }

    private fun getStudentByUsername(username: String): Student {
        val studentJson = sharedPrefs.getString("student_profile_$username", null)
        return if (studentJson != null) {
            gson.fromJson(studentJson, Student::class.java)
        } else {
            val cred = _allStudentsList.value.find { it.username == username }
            if (cred != null) {
                Student(
                    name = cred.name,
                    matricNo = cred.matricNo,
                    department = cred.department,
                    level = cred.level,
                    gradingSystem = cred.gradingSystem,
                    institution = cred.institution
                )
            } else {
                getGuestStudent()
            }
        }
    }

    private fun getSemestersByUsername(username: String): List<Semester> {
        val semestersJson = sharedPrefs.getString("semesters_records_$username", null)
        if (semestersJson != null) {
            val type = object : TypeToken<List<Semester>>() {}.type
            return gson.fromJson(semestersJson, type)
        } else {
            val student = getStudentByUsername(username)
            val system = student.gradingSystem
            
            // Default demo dataset loaded when a student logging in first time
            val sem1Courses = if (system == GradingSystem.NBTE) {
                listOf(
                    Course(code = "COM 111", title = "Introduction to Computing", credits = 3, score = 78),
                    Course(code = "COM 112", title = "Digital Electronics", credits = 2, score = 68),
                    Course(code = "COM 113", title = "Computer Programming", credits = 3, score = 72),
                    Course(code = "MTH 111", title = "Algebra", credits = 2, score = 58),
                    Course(code = "GNS 101", title = "Use of English I", credits = 2, score = 82)
                )
            } else {
                listOf(
                    Course(code = "MTH 101", title = "General Mathematics I", credits = 4, score = 82),
                    Course(code = "CHM 101", title = "General Chemistry I", credits = 4, score = 71),
                    Course(code = "PHY 101", title = "General Physics I", credits = 4, score = 64),
                    Course(code = "GST 111", title = "Communication in English I", credits = 2, score = 78)
                )
            }.map { c ->
                val (grade, gp) = GradeUtils.computeGradeAndGp(c.score, system)
                c.copy(grade = grade, gp = gp)
            }
            val sem1Gpa = GradeUtils.calculateGPA(sem1Courses, system)

            val sem2Courses = if (system == GradingSystem.NBTE) {
                listOf(
                    Course(code = "COM 121", title = "Programming with Fortran", credits = 3, score = 80),
                    Course(code = "COM 122", title = "Object-Oriented Programming", credits = 3, score = 75),
                    Course(code = "COM 123", title = "Data Structures & Algorithms", credits = 3, score = 62),
                    Course(code = "MTH 121", title = "Calculus", credits = 2, score = 48),
                    Course(code = "GNS 102", title = "Use of English II", credits = 2, score = 71)
                )
            } else {
                listOf(
                    Course(code = "MTH 102", title = "General Mathematics II", credits = 4, score = 68),
                    Course(code = "CHM 102", title = "General Chemistry II", credits = 4, score = 85),
                    Course(code = "PHY 102", title = "General Physics II", credits = 4, score = 59),
                    Course(code = "GST 112", title = "Logic & Philosophy", credits = 2, score = 73)
                )
            }.map { c ->
                val (grade, gp) = GradeUtils.computeGradeAndGp(c.score, system)
                c.copy(grade = grade, gp = gp)
            }
            val sem2Gpa = GradeUtils.calculateGPA(sem2Courses, system)

            val sem3Courses = if (system == GradingSystem.NBTE) {
                listOf(
                    Course(code = "COM 211", title = "Systems Analysis & Design", credits = 3, score = 85),
                    Course(code = "COM 212", title = "Database Design & Management", credits = 4, score = 76),
                    Course(code = "COM 213", title = "Operating Systems", credits = 3, score = 67),
                    Course(code = "COM 214", title = "Java Programming", credits = 3, score = 54),
                    Course(code = "EED 216", title = "Entrepreneurship Development", credits = 2, score = 73)
                )
            } else {
                listOf(
                    Course(code = "COM 201", title = "Computer Programming I", credits = 3, score = 88),
                    Course(code = "COM 203", title = "Database Systems", credits = 3, score = 79),
                    Course(code = "MTH 211", title = "Linear Algebra", credits = 3, score = 61),
                    Course(code = "GST 211", title = "Nigerian Peoples and Culture", credits = 2, score = 84)
                )
            }.map { c ->
                val (grade, gp) = GradeUtils.computeGradeAndGp(c.score, system)
                c.copy(grade = grade, gp = gp)
            }
            val sem3Gpa = GradeUtils.calculateGPA(sem3Courses, system)

            return listOf(
                Semester(name = "1st Year - 1st Semester", courses = sem1Courses, gpa = sem1Gpa),
                Semester(name = "1st Year - 2nd Semester", courses = sem2Courses, gpa = sem2Gpa),
                Semester(name = "2nd Year - 1st Semester", courses = sem3Courses, gpa = sem3Gpa)
            )
        }
    }

    private fun getCachedAdviceByUsername(username: String): String {
        return sharedPrefs.getString("advisor_advice_$username", "") ?: ""
    }

    private fun getActiveUsername(): String {
        val session = _currentSession.value ?: return "guest"
        return if (session.role == UserRole.ADMIN) {
            _adminSelectedStudent.value ?: "guest"
        } else {
            session.username
        }
    }

    private fun saveStudentProfile(student: Student) {
        val username = getActiveUsername()
        if (username != "guest") {
            sharedPrefs.edit().putString("student_profile_$username", gson.toJson(student)).apply()
            _studentState.value = student
            recalculateAllGPAs()
        }
    }

    private fun saveSemesters(semesters: List<Semester>) {
        val username = getActiveUsername()
        if (username != "guest") {
            sharedPrefs.edit().putString("semesters_records_$username", gson.toJson(semesters)).apply()
            _semestersState.value = semesters
        }
    }

    fun login(usernameInput: String, passwordInput: String): Boolean {
        try {
            // Check dynamic students list from our "mini db" state
            val matchedStudent = _allStudentsList.value.find { 
                it.username.equals(usernameInput, ignoreCase = true) && it.password == passwordInput 
            }
            if (matchedStudent != null) {
                val session = UserSession(
                    username = matchedStudent.username,
                    role = UserRole.STUDENT,
                    displayName = matchedStudent.name
                )
                _currentSession.value = session
                sharedPrefs.edit().putString("user_session", gson.toJson(session)).apply()
                syncStateWithSession()
                _errorMessage.value = null
                return true
            }

            // Check admins list from credentials.json
            val jsonStr = getApplication<Application>().assets.open("credentials.json").bufferedReader().use { it.readText() }
            val fileData = gson.fromJson(jsonStr, CredentialsFile::class.java)
            val matchedAdmin = fileData.admins.find { 
                it.username.equals(usernameInput, ignoreCase = true) && it.password == passwordInput 
            }
            if (matchedAdmin != null) {
                val session = UserSession(
                    username = matchedAdmin.username,
                    role = UserRole.ADMIN,
                    displayName = matchedAdmin.name
                )
                _currentSession.value = session
                sharedPrefs.edit().putString("user_session", gson.toJson(session)).apply()
                
                if (_adminSelectedStudent.value == null) {
                    val firstStudent = _allStudentsList.value.firstOrNull()?.username
                    _adminSelectedStudent.value = firstStudent
                    sharedPrefs.edit().putString("admin_selected_student", firstStudent).apply()
                }
                
                syncStateWithSession()
                _errorMessage.value = null
                return true
            }
            
            _errorMessage.value = "Invalid username or password"
            return false
        } catch (e: Exception) {
            _errorMessage.value = "Authentication error: ${e.localizedMessage}"
            return false
        }
    }

    fun logout() {
        _currentSession.value = null
        _adminSelectedStudent.value = null
        sharedPrefs.edit()
            .remove("user_session")
            .remove("admin_selected_student")
            .apply()
        _studentState.value = getGuestStudent()
        _semestersState.value = emptyList()
        _advisorAdvice.value = ""
        _errorMessage.value = null
    }

    fun selectAdminStudent(username: String) {
        _adminSelectedStudent.value = username
        sharedPrefs.edit().putString("admin_selected_student", username).apply()
        syncStateWithSession()
    }

    fun updateProfile(name: String, matricNo: String, department: String, level: String, system: GradingSystem) {
        val updated = _studentState.value.copy(
            name = name,
            matricNo = matricNo,
            department = department,
            level = level,
            gradingSystem = system
        )
        saveStudentProfile(updated)
    }

    fun addSemester(name: String) {
        val newSem = Semester(name = name)
        val updatedList = _semestersState.value + newSem
        saveSemesters(updatedList)
    }

    fun deleteSemester(semesterId: String) {
        val updatedList = _semestersState.value.filter { it.id != semesterId }
        saveSemesters(updatedList)
    }

    fun addCourse(semesterId: String, code: String, title: String, credits: Int, score: Int) {
        val system = _studentState.value.gradingSystem
        val (grade, gp) = GradeUtils.computeGradeAndGp(score, system)
        val newCourse = Course(code = code, title = title, credits = credits, score = score, grade = grade, gp = gp)
        
        val updatedList = _semestersState.value.map { sem ->
            if (sem.id == semesterId) {
                val updatedCourses = sem.courses + newCourse
                sem.copy(courses = updatedCourses, gpa = GradeUtils.calculateGPA(updatedCourses, system))
            } else {
                sem
            }
        }
        saveSemesters(updatedList)
    }

    fun deleteCourse(semesterId: String, courseId: String) {
        val system = _studentState.value.gradingSystem
        val updatedList = _semestersState.value.map { sem ->
            if (sem.id == semesterId) {
                val updatedCourses = sem.courses.filter { it.id != courseId }
                sem.copy(courses = updatedCourses, gpa = GradeUtils.calculateGPA(updatedCourses, system))
            } else {
                sem
            }
        }
        saveSemesters(updatedList)
    }

    fun updateCourse(semesterId: String, courseId: String, credits: Int, score: Int) {
        val system = _studentState.value.gradingSystem
        val (grade, gp) = GradeUtils.computeGradeAndGp(score, system)
        
        val updatedList = _semestersState.value.map { sem ->
            if (sem.id == semesterId) {
                val updatedCourses = sem.courses.map { c ->
                    if (c.id == courseId) {
                        c.copy(credits = credits, score = score, grade = grade, gp = gp)
                    } else {
                        c
                    }
                }
                sem.copy(courses = updatedCourses, gpa = GradeUtils.calculateGPA(updatedCourses, system))
            } else {
                sem
            }
        }
        saveSemesters(updatedList)
    }

    fun resetDemoData() {
        val username = getActiveUsername()
        if (username != "guest") {
            sharedPrefs.edit()
                .remove("student_profile_$username")
                .remove("semesters_records_$username")
                .remove("advisor_advice_$username")
                .apply()
            _studentState.value = getStudentByUsername(username)
            _semestersState.value = getSemestersByUsername(username)
            _advisorAdvice.value = getCachedAdviceByUsername(username)
            _errorMessage.value = null
        }
    }

    fun clearAllData() {
        val username = getActiveUsername()
        if (username != "guest") {
            val clearedStudent = _studentState.value.copy(name = "New Student", matricNo = "", department = "", level = "ND I")
            saveStudentProfile(clearedStudent)
            saveSemesters(emptyList())
            sharedPrefs.edit().remove("advisor_advice_$username").apply()
            _advisorAdvice.value = ""
        }
    }

    private fun recalculateAllGPAs() {
        val system = _studentState.value.gradingSystem
        val updatedList = _semestersState.value.map { sem ->
            val updatedCourses = sem.courses.map { c ->
                val (grade, gp) = GradeUtils.computeGradeAndGp(c.score, system)
                c.copy(grade = grade, gp = gp)
            }
            sem.copy(courses = updatedCourses, gpa = GradeUtils.calculateGPA(updatedCourses, system))
        }
        saveSemesters(updatedList)
    }

    fun getCumulativeCGPA(): Double {
        val semesters = _semestersState.value
        if (semesters.isEmpty()) return 0.0
        var totalPoints = 0.0
        var totalCredits = 0
        for (sem in semesters) {
            for (c in sem.courses) {
                totalPoints += c.gp * c.credits
                totalCredits += c.credits
            }
        }
        return if (totalCredits > 0) totalPoints / totalCredits else 0.0
    }

    fun getTotalCreditsCompleted(): Int {
        return _semestersState.value.sumOf { sem ->
            sem.courses.sumOf { it.credits }
        }
    }

    fun askAIAdvisor() {
        val student = _studentState.value
        val semesters = _semestersState.value
        val cgpa = getCumulativeCGPA()
        
        if (semesters.isEmpty()) {
            _errorMessage.value = "Please log at least one semester with courses to consult the advisor."
            return
        }

        _loadingAdvisor.value = true
        _errorMessage.value = null

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            _loadingAdvisor.value = false
            _errorMessage.value = "Gemini API key is not configured in the environment. Please configure it in your Secrets."
            return
        }

        val transcriptBuilder = StringBuilder()
        transcriptBuilder.append("Student Profile:\n")
        transcriptBuilder.append("- Name: ${student.name}\n")
        transcriptBuilder.append("- Matric No: ${student.matricNo}\n")
        transcriptBuilder.append("- Department: ${student.department}\n")
        transcriptBuilder.append("- Institution: ${student.institution}\n")
        transcriptBuilder.append("- Grading Scale: ${student.gradingSystem} (${if (student.gradingSystem == GradingSystem.UNIVERSAL_5_0) "5.0" else "4.0"})\n")
        transcriptBuilder.append("- Cumulative CGPA: ${String.format("%.2f", cgpa)}\n\n")
        
        transcriptBuilder.append("Detailed Semester Records:\n")
        for (sem in semesters) {
            transcriptBuilder.append("### Semester: ${sem.name} (GPA: ${String.format("%.2f", sem.gpa)})\n")
            for (c in sem.courses) {
                transcriptBuilder.append("  - ${c.code} (${c.title}): ${c.credits} credit units, Score: ${c.score}%, Grade: ${c.grade}, GP: ${c.gp}\n")
            }
            transcriptBuilder.append("\n")
        }

        val prompt = """
            You are an expert Academic Advisor specializing in technical education and polytechnic/university standards (including the Nigerian NBTE and NUC systems).
            Analyse the student's transcript details below and generate a professional, highly encouraging, and specific "Academic Strategy & Evaluation Report".
            
            $transcriptBuilder
            
            Please organize your response into these exact Markdown sections:
            
            ## Academic Performance Analysis
            (Analyze the semester-over-semester GPA trends, identifying whether performance is improving, declining, or stable. Point out any key statistics.)
            
            ## Strengths & Practical Skills
            (Look at the high scoring courses, especially core practical computer labs like Database COM, Java, programming, or systems courses. Highlight their career and skills strengths.)
            
            ## Theoretical & Performance Gaps
            (Highlight lower scores, mathematics courses, or potential pain points that dragged their GPA down.)
            
            ## High-Impact Improvement Strategy (Distinction Blueprint)
            (Provide concrete, actionable study recommendations. Highlight the "Credit Weighting" strategy—why protecting high credit load courses (e.g. 3 or 4 units) matters. Give targeted grade projections for future semesters to help them reach or secure their desired class of degree.)
            
            Keep the tone professional, motivating, and specific to their curriculum.
        """.trimIndent()

        viewModelScope.launch {
            try {
                val advice = callGeminiApi(apiKey, prompt)
                if (advice != null) {
                    _advisorAdvice.value = advice
                    val username = getActiveUsername()
                    if (username != "guest") {
                        sharedPrefs.edit().putString("advisor_advice_$username", advice).apply()
                    }
                } else {
                    _errorMessage.value = "Failed to communicate with Gemini API. Please try again."
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _loadingAdvisor.value = false
            }
        }
    }

    private suspend fun callGeminiApi(apiKey: String, prompt: String): String? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            
            val jsonRequest = JSONObject().apply {
                val contentsArray = org.json.JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = org.json.JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext null
                }
                val responseBodyStr = response.body?.string() ?: return@withContext null
                val responseJson = JSONObject(responseBodyStr)
                
                val candidates = responseJson.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.getJSONObject("content")
                    val parts = contentObj.getJSONArray("parts")
                    if (parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).getString("text")
                    }
                }
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}

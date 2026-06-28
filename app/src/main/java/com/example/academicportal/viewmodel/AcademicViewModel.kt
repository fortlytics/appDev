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

    private val _studentState = mutableStateOf<Student>(getInitialStudent())
    val studentState: State<Student> = _studentState

    private val _semestersState = mutableStateOf<List<Semester>>(getInitialSemesters())
    val semestersState: State<List<Semester>> = _semestersState

    private val _advisorAdvice = mutableStateOf<String>(getCachedAdvice())
    val advisorAdvice: State<String> = _advisorAdvice

    private val _loadingAdvisor = mutableStateOf(false)
    val loadingAdvisor: State<Boolean> = _loadingAdvisor

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private fun getInitialStudent(): Student {
        val studentJson = sharedPrefs.getString("student_profile", null)
        return if (studentJson != null) {
            gson.fromJson(studentJson, Student::class.java)
        } else {
            Student(
                name = "Adebayo Kolawole",
                matricNo = "FPO/ND/COM/24/089",
                department = "Computer Science",
                level = "ND II",
                gradingSystem = GradingSystem.NBTE,
                institution = "Federal Polytechnic Offa"
            )
        }
    }

    private fun getInitialSemesters(): List<Semester> {
        val semestersJson = sharedPrefs.getString("semesters_records", null)
        if (semestersJson != null) {
            val type = object : TypeToken<List<Semester>>() {}.type
            return gson.fromJson(semestersJson, type)
        } else {
            // Load Demo Data matching the React App
            val system = GradingSystem.NBTE
            
            val sem1Courses = listOf(
                Course(code = "COM 111", title = "Introduction to Computing", credits = 3, score = 78),
                Course(code = "COM 112", title = "Digital Electronics", credits = 2, score = 68),
                Course(code = "COM 113", title = "Computer Programming in BASIC", credits = 3, score = 72),
                Course(code = "MTH 111", title = "Algebra", credits = 2, score = 58),
                Course(code = "GNS 101", title = "Use of English I", credits = 2, score = 82)
            ).map { c ->
                val (grade, gp) = GradeUtils.computeGradeAndGp(c.score, system)
                c.copy(grade = grade, gp = gp)
            }
            val sem1Gpa = GradeUtils.calculateGPA(sem1Courses, system)

            val sem2Courses = listOf(
                Course(code = "COM 121", title = "Scientific Programming with Fortran", credits = 3, score = 80),
                Course(code = "COM 122", title = "Object-Oriented Programming", credits = 3, score = 75),
                Course(code = "COM 123", title = "Data Structures & Algorithms", credits = 3, score = 62),
                Course(code = "MTH 121", title = "Calculus", credits = 2, score = 48),
                Course(code = "GNS 102", title = "Use of English II", credits = 2, score = 71)
            ).map { c ->
                val (grade, gp) = GradeUtils.computeGradeAndGp(c.score, system)
                c.copy(grade = grade, gp = gp)
            }
            val sem2Gpa = GradeUtils.calculateGPA(sem2Courses, system)

            val sem3Courses = listOf(
                Course(code = "COM 211", title = "Systems Analysis & Design", credits = 3, score = 85),
                Course(code = "COM 212", title = "Database Design & Management", credits = 4, score = 76),
                Course(code = "COM 213", title = "Operating Systems", credits = 3, score = 67),
                Course(code = "COM 214", title = "Java Programming", credits = 3, score = 54),
                Course(code = "EED 216", title = "Entrepreneurship Development", credits = 2, score = 73)
            ).map { c ->
                val (grade, gp) = GradeUtils.computeGradeAndGp(c.score, system)
                c.copy(grade = grade, gp = gp)
            }
            val sem3Gpa = GradeUtils.calculateGPA(sem3Courses, system)

            return listOf(
                Semester(name = "ND I - 1st Semester", courses = sem1Courses, gpa = sem1Gpa),
                Semester(name = "ND I - 2nd Semester", courses = sem2Courses, gpa = sem2Gpa),
                Semester(name = "ND II - 1st Semester", courses = sem3Courses, gpa = sem3Gpa)
            )
        }
    }

    private fun getCachedAdvice(): String {
        return sharedPrefs.getString("advisor_advice", "") ?: ""
    }

    private fun saveStudentProfile(student: Student) {
        sharedPrefs.edit().putString("student_profile", gson.toJson(student)).apply()
        _studentState.value = student
        recalculateAllGPAs()
    }

    private fun saveSemesters(semesters: List<Semester>) {
        sharedPrefs.edit().putString("semesters_records", gson.toJson(semesters)).apply()
        _semestersState.value = semesters
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
        sharedPrefs.edit().remove("student_profile").remove("semesters_records").remove("advisor_advice").apply()
        _studentState.value = getInitialStudent()
        _semestersState.value = getInitialSemesters()
        _advisorAdvice.value = ""
        _errorMessage.value = null
    }

    fun clearAllData() {
        val clearedStudent = _studentState.value.copy(name = "New Student", matricNo = "", department = "", level = "ND I")
        saveStudentProfile(clearedStudent)
        saveSemesters(emptyList())
        sharedPrefs.edit().remove("advisor_advice").apply()
        _advisorAdvice.value = ""
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

        // Construct academic summary prompt
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
                    sharedPrefs.edit().putString("advisor_advice", advice).apply()
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

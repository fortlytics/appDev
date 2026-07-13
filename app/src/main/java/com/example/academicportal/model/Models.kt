package com.example.academicportal.model

import java.util.UUID

enum class GradingSystem {
    NBTE,           // Nigerian Polytechnic (4.0 Scale)
    UNIVERSAL_5_0,  // Nigerian University (5.0 Scale)
    STANDARD_4_0    // Standard US Academic (4.0 Scale)
}

data class Student(
    val name: String,
    val matricNo: String,
    val department: String,
    val level: String,
    val gradingSystem: GradingSystem,
    val institution: String
)

data class StudentCredential(
    val username: String,
    val password: String,
    val name: String,
    val matricNo: String,
    val department: String,
    val level: String,
    val gradingSystem: GradingSystem,
    val institution: String
)

data class AdminCredential(
    val username: String,
    val password: String,
    val name: String,
    val department: String,
    val institution: String
)

data class CredentialsFile(
    val students: List<StudentCredential>,
    val admins: List<AdminCredential>
)

enum class UserRole {
    STUDENT, ADMIN
}

data class UserSession(
    val username: String,
    val role: UserRole,
    val displayName: String
)

data class Course(
    val id: String = UUID.randomUUID().toString(),
    val code: String,
    val title: String,
    val credits: Int,
    val score: Int,
    val grade: String = "",
    val gp: Double = 0.0
)

data class Semester(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val courses: List<Course> = emptyList(),
    val gpa: Double = 0.0
)

data class GradeThreshold(
    val grade: String,
    val gp: Double,
    val minScore: Int,
    val label: String
)

object GradeUtils {
    fun getThresholds(system: GradingSystem): List<GradeThreshold> {
        return when (system) {
            GradingSystem.NBTE -> listOf(
                GradeThreshold("A", 4.00, 75, "Distinction"),
                GradeThreshold("AB", 3.50, 70, "Distinction"),
                GradeThreshold("B", 3.25, 65, "Upper Credit"),
                GradeThreshold("BC", 3.00, 60, "Upper Credit"),
                GradeThreshold("C", 2.75, 55, "Lower Credit"),
                GradeThreshold("CD", 2.50, 50, "Lower Credit"),
                GradeThreshold("D", 2.25, 45, "Pass"),
                GradeThreshold("E", 2.00, 40, "Pass"),
                GradeThreshold("F", 0.00, 0, "Fail")
            )
            GradingSystem.UNIVERSAL_5_0 -> listOf(
                GradeThreshold("A", 5.00, 70, "First Class"),
                GradeThreshold("B", 4.00, 60, "Second Class Upper"),
                GradeThreshold("C", 3.00, 50, "Second Class Lower"),
                GradeThreshold("D", 2.00, 45, "Third Class"),
                GradeThreshold("E", 1.00, 40, "Pass"),
                GradeThreshold("F", 0.00, 0, "Fail")
            )
            GradingSystem.STANDARD_4_0 -> listOf(
                GradeThreshold("A", 4.00, 90, "Excellent"),
                GradeThreshold("B", 3.00, 80, "Good"),
                GradeThreshold("C", 2.00, 70, "Satisfactory"),
                GradeThreshold("D", 1.00, 60, "Poor"),
                GradeThreshold("F", 0.00, 0, "Fail")
            )
        }
    }

    fun computeGradeAndGp(score: Int, system: GradingSystem): Pair<String, Double> {
        val thresholds = getThresholds(system)
        for (t in thresholds) {
            if (score >= t.minScore) {
                return Pair(t.grade, t.gp)
            }
        }
        return Pair("F", 0.0)
    }

    fun calculateGPA(courses: List<Course>, system: GradingSystem): Double {
        if (courses.isEmpty()) return 0.0
        var totalPoints = 0.0
        var totalCredits = 0
        for (c in courses) {
            val (grade, gp) = computeGradeAndGp(c.score, system)
            totalPoints += gp * c.credits
            totalCredits += c.credits
        }
        return if (totalCredits > 0) totalPoints / totalCredits else 0.0
    }

    data class Classification(
        val className: String,
        val colorHex: String,
        val badgeBgHex: String
    )

    fun getClassification(cgpa: Double, system: GradingSystem): Classification {
        return when (system) {
            GradingSystem.NBTE -> {
                when {
                    cgpa >= 3.50 -> Classification("Distinction", "#059669", "#10b981")
                    cgpa >= 3.00 -> Classification("Upper Credit", "#2563eb", "#3b82f6")
                    cgpa >= 2.50 -> Classification("Lower Credit", "#d97706", "#f59e0b")
                    cgpa >= 2.00 -> Classification("Pass", "#64748b", "#94a3b8")
                    else -> Classification("Fail / Probation", "#dc2626", "#ef4444")
                }
            }
            GradingSystem.UNIVERSAL_5_0 -> {
                when {
                    cgpa >= 4.50 -> Classification("First Class", "#059669", "#10b981")
                    cgpa >= 3.50 -> Classification("Second Class Upper", "#2563eb", "#3b82f6")
                    cgpa >= 2.40 -> Classification("Second Class Lower", "#d97706", "#f59e0b")
                    cgpa >= 1.50 -> Classification("Third Class", "#64748b", "#94a3b8")
                    cgpa >= 1.00 -> Classification("Pass", "#64748b", "#cbd5e1")
                    else -> Classification("Fail / Probation", "#dc2626", "#ef4444")
                }
            }
            GradingSystem.STANDARD_4_0 -> {
                when {
                    cgpa >= 3.50 -> Classification("Excellent (Distinction)", "#059669", "#10b981")
                    cgpa >= 3.00 -> Classification("Good (High Class)", "#2563eb", "#3b82f6")
                    cgpa >= 2.00 -> Classification("Satisfactory (Average)", "#d97706", "#f59e0b")
                    cgpa >= 1.00 -> Classification("Poor Standing", "#64748b", "#94a3b8")
                    else -> Classification("Academic Dismissal", "#dc2626", "#ef4444")
                }
            }
        }
    }
}

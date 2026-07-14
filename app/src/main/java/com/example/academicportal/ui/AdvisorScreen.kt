package com.example.academicportal.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicportal.model.Student

@Composable
fun AdvisorScreen(
    student: Student,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Inquiry state
    var inquirySubject by remember { mutableStateOf("") }
    var inquiryBody by remember { mutableStateOf("") }
    var inquirySent by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "OFFICIAL EXAM OFFICE",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF4F46E5),
                letterSpacing = 1.sp
            )
            Text(
                text = "Academic Records & Registrar Services",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Official administrative support, academic transcript ledger validation, and grading policy inquiries.",
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Contact details card
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
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = "Officer icon",
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "DESIGNATED EXAM OFFICER",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color(0xFF1E293B),
                        letterSpacing = 0.5.sp
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Officer Name card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFEEF2F6), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color(0xFF475569)
                        )
                    }
                    Column {
                        Text(
                            text = "Dr. O. A. Bello",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Departmental Exam Officer",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Metadata list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Email, "Email", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        Text(
                            text = "bello.oa@fedpo.edu.ng",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, "Location", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        Text(
                            text = "Academic Block A, Room 204",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Schedule, "Hours", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        Text(
                            text = "09:00 AM - 04:00 PM (Monday - Friday)",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Registrar Services Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Registrar icon",
                        tint = Color(0xFF0EA5E9),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ACADEMIC AFFAIRS REGISTRY",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color(0xFF1E293B),
                        letterSpacing = 0.5.sp
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Text(
                    text = "For official stamp validation, transcript delivery requests to external institutions, or academic suspension appeals, contact the core registrar services desk.",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 15.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("REGISTRAR EMAIL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        Text("registrar@fedpo.edu.ng", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("REGISTRAR HOTLINE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        Text("+234 (0) 803 123 4567", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                }
            }
        }

        // Send Inquiry Form
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
                        imageVector = Icons.Default.Send,
                        contentDescription = "Inquiry icon",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "DIRECT STUDENT OFFICE INQUIRY",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color(0xFF1E293B),
                        letterSpacing = 0.5.sp
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                if (inquirySent) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, "Sent", tint = Color(0xFF10B981), modifier = Modifier.size(36.dp))
                            Text(
                                text = "Inquiry Logged Successfully!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "Your inquiry has been placed in Dr. O. A. Bello's office queue. Please monitor your student inbox for response.",
                                fontSize = 10.sp,
                                color = Color(0xFF047857),
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                            Button(
                                onClick = {
                                    inquirySent = false
                                    inquirySubject = ""
                                    inquiryBody = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.padding(top = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("New Inquiry", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Need a correction on your official grades, or want to register a grievance? Log an official inquiry immediately.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 15.sp
                    )

                    OutlinedTextField(
                        value = inquirySubject,
                        onValueChange = { inquirySubject = it },
                        label = { Text("Inquiry Subject (e.g. Grade appeal for EEC211)", fontSize = 11.sp) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    OutlinedTextField(
                        value = inquiryBody,
                        onValueChange = { inquiryBody = it },
                        label = { Text("Details / Explanation", fontSize = 11.sp) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Button(
                        onClick = {
                            if (inquirySubject.isNotBlank() && inquiryBody.isNotBlank()) {
                                inquirySent = true
                            }
                        },
                        enabled = inquirySubject.isNotBlank() && inquiryBody.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5),
                            disabledContainerColor = Color(0xFFC7D2FE)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log Official Inquiry", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApplicationEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.CompanyEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.SalaryRecordEntity
import com.example.ui.theme.CompanyGreen
import com.example.ui.theme.CompanyGreenDark
import com.example.ui.theme.CompanyGreenLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanySalaryScreen(
    company: CompanyEntity?,
    approvedWorkers: List<ApplicationEntity>,
    attendanceList: List<AttendanceEntity>,
    salaryRecords: List<SalaryRecordEntity>,
    payments: List<PaymentEntity>,
    isHindi: Boolean,
    onSaveSalaryDate: (Int) -> Unit,
    onMarkAttendance: (workerId: String, workerName: String, jobId: String, date: String, status: String, otHours: Double, otRate: Double, note: String) -> Unit,
    onRecordLeave: (workerId: String, workerName: String, jobId: String, date: String, leaveType: String, reason: String) -> Unit,
    onCalculateSalary: (workerId: String, workerName: String, jobId: String, jobTitle: String, cycleStartDate: String, cycleEndDate: String, cycleLabel: String, totalWorkingDays: Int, basicSalary: Double, deductions: Double, advance: Double) -> Unit,
    onMarkSalaryPaid: (salaryRecord: SalaryRecordEntity, paymentRef: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Attendance & OT, 1: Salary Calculation, 2: Payment History, 3: Salary Date Setup

    // Date Setting State
    var showDateDialog by remember { mutableStateOf(false) }
    var selectedSalaryDay by remember { mutableIntStateOf(company?.salaryPaymentDate ?: 10) }

    // Attendance marking state
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateStr = remember { sdfDate.format(Calendar.getInstance().time) }
    var attendanceDate by remember { mutableStateOf(todayDateStr) }
    var selectedWorkerId by remember { mutableStateOf(approvedWorkers.firstOrNull()?.id ?: "") }
    var attendanceStatus by remember { mutableStateOf("Present") } // "Present", "Half Day", "Absent"
    var otHoursText by remember { mutableStateOf("0") }
    var otRateText by remember { mutableStateOf("100") }
    var attendanceNote by remember { mutableStateOf("") }

    // Payment dialog state
    var payingRecord by remember { mutableStateOf<SalaryRecordEntity?>(null) }
    var paymentRefText by remember { mutableStateOf("") }

    // Advance & Deduction state for Calculation tab
    var advanceText by remember { mutableStateOf("0") }
    var deductionText by remember { mutableStateOf("0") }
    var basicSalaryText by remember { mutableStateOf("15000") }
    var workingDaysText by remember { mutableStateOf("26") }

    // Computed cycle label
    val salaryDay = company?.salaryPaymentDate ?: 10
    val cycleLabel = remember(salaryDay) {
        val nextDay = if (salaryDay == 28 || salaryDay == 30 || salaryDay == 31) 1 else salaryDay + 1
        "$nextDay तारीख से $salaryDay तारीख तक"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("company_salary_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBackClick)
                        .testTag("company_salary_back_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "← वापस" else "← Back",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isHindi) "💰 Salary & Attendance System" else "💰 Salary & Attendance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Company: ${company?.companyName ?: "Apex Logistics"}",
                        fontSize = 12.sp,
                        color = CompanyGreen
                    )
                }
            }
        }

        // Section 1: Company Salary Date Indicator & Quick Edit
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = CompanyGreenLight.copy(alpha = 0.5f)),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CompanyGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isHindi) "सैलरी पेमेंट तारीख: हर महीने $salaryDay तारीख" else "Salary Date: Day $salaryDay each month",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CompanyGreenDark
                            )
                            Text(
                                text = if (isHindi) "सैलरी साइकिल: $cycleLabel" else "Salary Cycle: $cycleLabel",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showDateDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_change_salary_date")
                    ) {
                        Text(text = if (isHindi) "बदलें" else "Change", fontSize = 12.sp)
                    }
                }
            }
        }

        // Tabs Row: 1. Attendance & OT | 2. Calculate Salary | 3. Payment History
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = if (isHindi) "1. हाजिरी & OT" else "1. Attendance",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_company_attendance")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = if (isHindi) "2. सैलरी कैलकुलेट" else "2. Salary",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_company_salary_calc")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = if (isHindi) "3. पेमेंट इतिहास" else "3. History",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_company_history")
                )
            }
        }

        // ================= TAB 0: ATTENDANCE & OVERTIME =================
        if (selectedTab == 0) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isHindi) "👷 सेलेक्टेड वर्कर की हाजिरी (Attendance) लगाएं" else "Mark Worker Attendance & OT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CompanyGreen
                        )
                        Text(
                            text = if (isHindi) "केवल Company ही attendance लगा सकती है (1 दिन = 1 रिकॉर्ड)" else "Only company can mark attendance (1 record per day)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Worker Selection
                        Text(
                            text = if (isHindi) "वर्कर चुनें:" else "Select Approved Worker:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (approvedWorkers.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isHindi) "⚠️ कोई स्वीकृत वर्कर नहीं है। पहले Company Panel में वर्कर को Accept/Approve करें।" else "No approved workers yet. Accept an applicant first.",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(approvedWorkers, key = { it.id }) { w ->
                                    val isSelected = selectedWorkerId == w.id || (selectedWorkerId.isBlank() && approvedWorkers.firstOrNull()?.id == w.id)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedWorkerId = w.id },
                                        label = { Text("${w.applicantName} (${w.jobTitle})") },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CompanyGreenLight,
                                            selectedLabelColor = CompanyGreenDark
                                        ),
                                        modifier = Modifier.testTag("chip_worker_${w.id}")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Date field
                        OutlinedTextField(
                            value = attendanceDate,
                            onValueChange = { attendanceDate = it },
                            label = { Text(if (isHindi) "तारीख (YYYY-MM-DD)" else "Date (YYYY-MM-DD)") },
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_attendance_date")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Buttons: Present, Half Day, Absent
                        Text(
                            text = if (isHindi) "हाजिरी स्थिति (Status):" else "Attendance Status:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Present" to "उपस्थित (Present)", "Half Day" to "आधा दिन (Half Day)", "Absent" to "अनुपस्थित (Absent)").forEach { (statusKey, label) ->
                                val isSelected = attendanceStatus == statusKey
                                val color = when (statusKey) {
                                    "Present" -> Color(0xFF2E7D32)
                                    "Half Day" -> Color(0xFFF57C00)
                                    else -> Color(0xFFD32F2F)
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, color) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { attendanceStatus = statusKey }
                                        .testTag("btn_status_$statusKey")
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = if (statusKey == "Present") "✅" else if (statusKey == "Half Day") "🌗" else "❌",
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Overtime (OT) Inputs: OT Hours & OT Rate
                        Text(
                            text = if (isHindi) "⏰ Overtime (OT) दर्ज करें:" else "Overtime (OT) Details:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = otHoursText,
                                onValueChange = { otHoursText = it },
                                label = { Text(if (isHindi) "OT घंटे (Hours)" else "OT Hours") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_ot_hours")
                            )

                            OutlinedTextField(
                                value = otRateText,
                                onValueChange = { otRateText = it },
                                label = { Text(if (isHindi) "OT दर (₹/Hour)" else "Rate (₹/hr)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_ot_rate")
                            )
                        }

                        // Live OT Amount preview
                        val otHrs = otHoursText.toDoubleOrNull() ?: 0.0
                        val otRt = otRateText.toDoubleOrNull() ?: 100.0
                        val otAmt = otHrs * otRt
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "💡 OT Amount: $otHrs घंटे × ₹${otRt.toInt()} = ₹${otAmt.toInt()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CompanyGreen
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Submit Attendance Button
                        Button(
                            onClick = {
                                val targetWorker = approvedWorkers.find { it.id == selectedWorkerId } ?: approvedWorkers.firstOrNull()
                                if (targetWorker != null) {
                                    val targetWorkerId = targetWorker.workerId.ifBlank { targetWorker.id }
                                    onMarkAttendance(
                                        targetWorkerId,
                                        targetWorker.applicantName,
                                        targetWorker.jobId,
                                        attendanceDate,
                                        attendanceStatus,
                                        otHrs,
                                        otRt,
                                        attendanceNote
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CompanyGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_submit_attendance")
                        ) {
                            Text(
                                text = if (isHindi) "💾 हाजिरी & OT सुरक्षित करें" else "Save Attendance & OT",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Leave Record Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isHindi) "🌿 छुट्टी (Leave) दर्ज करें" else "Record Worker Leave",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Weekly Off" to "साप्ताहिक छुट्टी (Weekly Off)", "Approved Leave" to "स्वीकृत छुट्टी (Approved Leave)").forEach { (lType, lLabel) ->
                                Button(
                                    onClick = {
                                        val targetWorker = approvedWorkers.find { it.id == selectedWorkerId } ?: approvedWorkers.firstOrNull()
                                        if (targetWorker != null) {
                                            val targetWorkerId = targetWorker.workerId.ifBlank { targetWorker.id }
                                            onRecordLeave(
                                                targetWorkerId,
                                                targetWorker.applicantName,
                                                targetWorker.jobId,
                                                attendanceDate,
                                                lType,
                                                "Approved by employer"
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = lLabel,
                                        fontSize = 11.sp,
                                        color = PrimaryBlueDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent attendance logged list
            item {
                Text(
                    text = if (isHindi) "📋 हालिया हाजिरी रिकॉर्ड (${attendanceList.size})" else "Recent Attendance Log (${attendanceList.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (attendanceList.isEmpty()) {
                item {
                    Text(
                        text = if (isHindi) "अभी कोई हाजिरी रिकॉर्ड नहीं है।" else "No attendance records logged yet.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(attendanceList.take(10), key = { it.id }) { att ->
                    AttendanceRowCard(att = att, isHindi = isHindi)
                }
            }
        }

        // ================= TAB 1: SALARY CALCULATION =================
        if (selectedTab == 1) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isHindi) "🧮 सैलरी कैलकुलेशन (Salary Calculation)" else "Salary Calculation & Finalization",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CompanyGreen
                        )
                        Text(
                            text = if (isHindi) "साइकिल: $cycleLabel (Payment Date: हर महीने $salaryDay)" else "Cycle: $cycleLabel",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Select Worker to calculate
                        Text(
                            text = if (isHindi) "सैलरी के लिए वर्कर चुनें:" else "Select Worker:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(approvedWorkers, key = { it.id }) { w ->
                                val isSelected = selectedWorkerId == w.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedWorkerId = w.id },
                                    label = { Text(w.applicantName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CompanyGreenLight,
                                        selectedLabelColor = CompanyGreenDark
                                    )
                                )
                            }
                        }

                        val activeApp = approvedWorkers.find { it.id == selectedWorkerId } ?: approvedWorkers.firstOrNull()
                        val activeWorkerId = activeApp?.workerId?.ifBlank { activeApp.id } ?: ""

                        // Worker's attendance summary in current cycle
                        val workerAtts = attendanceList.filter { it.workerId == activeWorkerId }
                        val presents = workerAtts.count { it.status == "Present" }
                        val halfs = workerAtts.count { it.status == "Half Day" }
                        val absents = workerAtts.count { it.status == "Absent" }
                        val totalOtHrs = workerAtts.sumOf { it.otHours }
                        val totalOtAmt = workerAtts.sumOf { it.otAmount }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isHindi) "📊 साइकिल सारांश (Cycle Summary):" else "📊 Cycle Summary:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Present: $presents दिन", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    Text("Half Day: $halfs दिन", fontSize = 12.sp, color = Color(0xFFF57C00), fontWeight = FontWeight.Bold)
                                    Text("Absent: $absents दिन", fontSize = 12.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "OT: $totalOtHrs घंटे = ₹${totalOtAmt.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CompanyGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Basic Salary & Total Working Days
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = basicSalaryText,
                                onValueChange = { basicSalaryText = it },
                                label = { Text(if (isHindi) "बेसिक सैलरी (Basic)" else "Basic Salary") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_basic_salary")
                            )

                            OutlinedTextField(
                                value = workingDaysText,
                                onValueChange = { workingDaysText = it },
                                label = { Text(if (isHindi) "कुल कार्य दिवस" else "Total Days") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_working_days")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Advance & Deductions
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = advanceText,
                                onValueChange = { advanceText = it },
                                label = { Text(if (isHindi) "एडवांस (Advance ₹)" else "Advance (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_advance")
                            )

                            OutlinedTextField(
                                value = deductionText,
                                onValueChange = { deductionText = it },
                                label = { Text(if (isHindi) "कटौती (Deduction ₹)" else "Deductions (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_deduction")
                            )
                        }

                        // Net Payable Calculation preview
                        val basic = basicSalaryText.toDoubleOrNull() ?: 15000.0
                        val totalDays = workingDaysText.toIntOrNull() ?: 26
                        val adv = advanceText.toDoubleOrNull() ?: 0.0
                        val ded = deductionText.toDoubleOrNull() ?: 0.0
                        val effectiveDays = presents + (halfs * 0.5)
                        val dayRate = if (totalDays > 0) basic / totalDays else 0.0
                        val earnedBase = dayRate * effectiveDays
                        val netPayable = maxOf(0.0, earnedBase + totalOtAmt - adv - ded)

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CompanyGreenLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isHindi) "कुल देय सैलरी (Final Net Payable):" else "Final Net Payable:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CompanyGreenDark
                                    )
                                    Text(
                                        text = "(Basic ₹${basic.toInt()} / $totalDays × $effectiveDays दिन + OT ₹${totalOtAmt.toInt()} - Adv ₹${adv.toInt()})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "₹${netPayable.toInt()}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CompanyGreenDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (activeApp != null) {
                                    onCalculateSalary(
                                        activeWorkerId,
                                        activeApp.applicantName,
                                        activeApp.jobId,
                                        activeApp.jobTitle,
                                        todayDateStr,
                                        todayDateStr,
                                        cycleLabel,
                                        totalDays,
                                        basic,
                                        ded,
                                        adv
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CompanyGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_finalize_salary")
                        ) {
                            Text(
                                text = if (isHindi) "💾 सैलरी कैलकुलेट & सेव करें" else "Calculate & Save Salary",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Salary Records Ready for Payment
            item {
                Text(
                    text = if (isHindi) "💳 सैलरी रिकॉर्ड्स (Salary Records & Payments):" else "Salary Records & Payment:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (salaryRecords.isEmpty()) {
                item {
                    Text(
                        text = if (isHindi) "अभी कोई सैलरी रिकॉर्ड नहीं बना है। ऊपर दिए फॉर्म से कैलकुलेट करें।" else "No salary records created yet.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(salaryRecords, key = { it.id }) { rec ->
                    SalaryRecordCard(
                        rec = rec,
                        isHindi = isHindi,
                        onPayClick = {
                            payingRecord = rec
                            paymentRefText = "UTR-" + System.currentTimeMillis().toString().takeLast(8)
                        }
                    )
                }
            }
        }

        // ================= TAB 2: PAYMENT HISTORY =================
        if (selectedTab == 2) {
            item {
                Text(
                    text = if (isHindi) "📜 स्थायी पेमेंट इतिहास (Permanent Payment History):" else "Permanent Payment History:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isHindi) "यह इतिहास कभी डिलीट नहीं होगा (Immutable Records)" else "Permanent & Immutable payment history",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (payments.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isHindi) "अभी तक कोई पेमेंट नहीं की गई है।" else "No payment records found.",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(payments, key = { it.paymentId }) { pay ->
                    PaymentRowCard(pay = pay, isHindi = isHindi)
                }
            }
        }
    }

    // Modal Dialog: Change Salary Payment Date
    if (showDateDialog) {
        AlertDialog(
            onDismissRequest = { showDateDialog = false },
            title = {
                Text(
                    text = if (isHindi) "📅 सैलरी पेमेंट तारीख चुनें" else "Set Salary Payment Date",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isHindi) "हर महीने किस तारीख को वर्कर को सैलरी देंगे? (1 से 28 तारीख):" else "Choose payment date of every month:",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(7, 10, 15, 20, 25).forEach { day ->
                            val isSel = selectedSalaryDay == day
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedSalaryDay = day },
                                label = { Text("$day तारीख") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CompanyGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cycle: ${if (selectedSalaryDay == 28) 1 else selectedSalaryDay + 1} तारीख से $selectedSalaryDay तारीख तक",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CompanyGreenDark
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveSalaryDate(selectedSalaryDay)
                        showDateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CompanyGreen),
                    modifier = Modifier.testTag("btn_confirm_salary_date")
                ) {
                    Text(text = if (isHindi) "सेव करें" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) {
                    Text(text = if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }

    // Modal Dialog: Mark Salary as Paid
    payingRecord?.let { rec ->
        AlertDialog(
            onDismissRequest = { payingRecord = null },
            title = {
                Text(
                    text = if (isHindi) "💸 सैलरी का भुगतान करें (Mark as Paid)" else "Mark Salary as Paid",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Worker: ${rec.workerName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Amount: ₹${rec.finalSalary.toInt()} (Net Payable)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CompanyGreen
                    )
                    Text(
                        text = "Cycle: ${rec.cycleLabel}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = paymentRefText,
                        onValueChange = { paymentRefText = it },
                        label = { Text(if (isHindi) "पेमेंट / UTR / रसीद नंबर" else "Payment Reference / UTR") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_payment_reference")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMarkSalaryPaid(rec, paymentRefText)
                        payingRecord = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CompanyGreen),
                    modifier = Modifier.testTag("btn_confirm_mark_paid")
                ) {
                    Text(text = if (isHindi) "✅ Mark Salary as Paid" else "Mark Paid")
                }
            },
            dismissButton = {
                TextButton(onClick = { payingRecord = null }) {
                    Text(text = if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun AttendanceRowCard(att: AttendanceEntity, isHindi: Boolean) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = att.workerName.ifBlank { "Worker" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "📅 ${att.date}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (att.otHours > 0) {
                    Text(
                        text = "⏰ OT: ${att.otHours} hrs (₹${att.otAmount.toInt()})",
                        fontSize = 11.sp,
                        color = CompanyGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (att.status) {
                    "Present" -> Color(0xFFE8F5E9)
                    "Half Day" -> Color(0xFFFFF3E0)
                    else -> Color(0xFFFFEBEE)
                }
            ) {
                Text(
                    text = att.status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (att.status) {
                        "Present" -> Color(0xFF2E7D32)
                        "Half Day" -> Color(0xFFE65100)
                        else -> Color(0xFFC62828)
                    }
                )
            }
        }
    }
}

@Composable
private fun SalaryRecordCard(
    rec: SalaryRecordEntity,
    isHindi: Boolean,
    onPayClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = rec.workerName.ifBlank { "Worker" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cycle: ${rec.cycleLabel}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (rec.status == "Paid") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ) {
                    Text(
                        text = if (rec.status == "Paid") "✅ PAID" else "⏳ Calculated",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rec.status == "Paid") Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Present: ${rec.presentDays.toInt()} दिन", fontSize = 12.sp)
                Text("OT: ${rec.otHours} hrs (₹${rec.otAmount.toInt()})", fontSize = 12.sp)
                Text("Basic: ₹${rec.basicSalary.toInt()}", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Payable: ₹${rec.finalSalary.toInt()}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = CompanyGreen
                )

                if (rec.status != "Paid") {
                    Button(
                        onClick = onPayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CompanyGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_pay_salary_${rec.id}")
                    ) {
                        Text(
                            text = if (isHindi) "Mark as Paid" else "Mark Paid",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentRowCard(pay: PaymentEntity, isHindi: Boolean) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "✅ Paid to: ${pay.workerName.ifBlank { "Worker" }}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "📅 ${pay.paymentDate} • Ref: ${pay.paymentReference}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "₹${pay.amount.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
            if (pay.salaryCycle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cycle: ${pay.salaryCycle}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

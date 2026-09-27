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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceEntity
import com.example.data.model.LeaveEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.SalaryRecordEntity
import com.example.data.model.WorkerEntity
import com.example.ui.theme.CompanyGreen
import com.example.ui.theme.CompanyGreenDark
import com.example.ui.theme.CompanyGreenLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight

@Composable
fun WorkerSalaryScreen(
    worker: WorkerEntity?,
    attendances: List<AttendanceEntity>,
    leaves: List<LeaveEntity>,
    salaryRecords: List<SalaryRecordEntity>,
    payments: List<PaymentEntity>,
    isHindi: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Cycle & Salary, 1: Attendance History, 2: Payment Slips
    var selectedYearFilter by remember { mutableStateOf("2026") }
    var selectedMonthFilter by remember { mutableStateOf("All") }

    // Summary calculations for current worker
    val totalPresents = attendances.count { it.status == "Present" }
    val totalHalfs = attendances.count { it.status == "Half Day" }
    val totalAbsents = attendances.count { it.status == "Absent" } + leaves.count { it.leaveType == "Absent" }
    val totalLeaves = leaves.count { it.leaveType != "Absent" }
    val totalOtHours = attendances.sumOf { it.otHours }
    val totalOtAmount = attendances.sumOf { it.otAmount }

    val latestSalary = salaryRecords.firstOrNull()
    val latestPayment = payments.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("worker_salary_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Back Button & Header
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
                        .testTag("worker_salary_back_button")
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
                        text = if (isHindi) "👷 मेरी सैलरी & हाजिरी" else "👷 My Salary & Attendance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${worker?.name ?: "राम कुमार"} • ${worker?.workerId ?: "WRK-NOIDA-01"}",
                        fontSize = 12.sp,
                        color = PrimaryBlue
                    )
                }
            }
        }

        // Prominent: "✅ Salary Paid Successfully" Card if Payment is done
        if (latestPayment != null) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("salary_paid_success_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E7D32)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isHindi) "✅ Salary Paid Successfully" else "✅ Salary Paid Successfully",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = if (isHindi) "कंपनी द्वारा सैलरी का भुगतान कर दिया गया है" else "Salary has been paid by the employer",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isHindi) "कुल प्राप्त सैलरी:" else "Total Paid Amount:",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹${latestPayment.amount.toInt()}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isHindi) "सैलरी साइकिल (Salary Cycle):" else "Salary Cycle:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = latestPayment.salaryCycle.ifBlank { "Current Cycle" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isHindi) "पेमेंट तारीख (Payment Date):" else "Payment Date:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = latestPayment.paymentDate,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (latestPayment.otAmount > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isHindi) "शामिल OT राशि (OT Amount):" else "Included OT:",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "₹${latestPayment.otAmount.toInt()}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CompanyGreen
                                        )
                                    }
                                }

                                if (latestPayment.paymentReference.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isHindi) "रेफरेंस / UTR नंबर:" else "Reference / UTR:",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = latestPayment.paymentReference,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = PrimaryBlue
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isHindi) "पेमेंट स्टेटस (Status):" else "Payment Status:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "✅ Paid",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Read-only Notice Card
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isHindi) "🔒 हाजिरी, OT व सैलरी केवल आपकी Company लगाती है। आप केवल अपना रिकॉर्ड सुरक्षित देख सकते हैं।" else "Attendance and salary are verified and managed only by your employer.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Live Overview Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(title = if (isHindi) "Present दिन" else "Present", value = "$totalPresents", color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                MetricPill(title = if (isHindi) "Half Day" else "Half Day", value = "$totalHalfs", color = Color(0xFFE65100), modifier = Modifier.weight(1f))
                MetricPill(title = if (isHindi) "Absent दिन" else "Absent", value = "$totalAbsents", color = Color(0xFFC62828), modifier = Modifier.weight(1f))
                MetricPill(title = if (isHindi) "OT घंटे" else "OT Hours", value = "${totalOtHours}h", color = CompanyGreen, modifier = Modifier.weight(1f))
            }
        }

        // Tabs: Live Cycle & Salary | Attendance History | All Payments
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (isHindi) "सैलरी सारांश" else "Salary", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_worker_salary")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (isHindi) "हाजिरी इतिहास" else "Attendance", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_worker_attendance")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(if (isHindi) "पेमेंट स्लिप्स" else "Payments", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_worker_payments")
                )
            }
        }

        // ================= TAB 0: SALARY SUMMARY =================
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
                            text = if (isHindi) "💰 चालू सैलरी साइकिल सारांश" else "Current Salary Cycle Summary",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SummaryRow(
                            label = if (isHindi) "बेसिक सैलरी (Basic Salary):" else "Basic Salary:",
                            value = "₹${latestSalary?.basicSalary?.toInt() ?: 15000}"
                        )
                        SummaryRow(
                            label = if (isHindi) "उपस्थित दिन (Present Days):" else "Present Days:",
                            value = "$totalPresents दिन"
                        )
                        SummaryRow(
                            label = if (isHindi) "आधा दिन (Half Days):" else "Half Days:",
                            value = "$totalHalfs दिन"
                        )
                        SummaryRow(
                            label = if (isHindi) "छुट्टी (Leaves / Off):" else "Approved Leaves:",
                            value = "$totalLeaves दिन"
                        )
                        SummaryRow(
                            label = if (isHindi) "ओवरटाइम (OT Hours & Amount):" else "Overtime:",
                            value = "$totalOtHours hrs (₹${totalOtAmount.toInt()})"
                        )
                        if ((latestSalary?.deductions ?: 0.0) > 0) {
                            SummaryRow(
                                label = if (isHindi) "कटौती (Deduction):" else "Deductions:",
                                value = "- ₹${latestSalary?.deductions?.toInt()}"
                            )
                        }
                        if ((latestSalary?.advance ?: 0.0) > 0) {
                            SummaryRow(
                                label = if (isHindi) "एडवांस (Advance):" else "Advance:",
                                value = "- ₹${latestSalary?.advance?.toInt()}"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PrimaryBlueLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isHindi) "देय सैलरी (Payable Salary):" else "Net Payable Salary:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                                Text(
                                    text = "₹${latestSalary?.finalSalary?.toInt() ?: 15000}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // ================= TAB 1: ATTENDANCE HISTORY =================
        if (selectedTab == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "📅 हाजिरी इतिहास (${attendances.size} दिन)" else "Attendance Records (${attendances.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (attendances.isEmpty()) {
                item {
                    Text(
                        text = if (isHindi) "कंपनी द्वारा अभी कोई हाजिरी दर्ज नहीं की गई है।" else "No attendance logged yet.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(attendances, key = { it.id }) { att ->
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
                                Text(text = "📅 ${att.date}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (att.otHours > 0) {
                                    Text(
                                        text = "⏰ Overtime: ${att.otHours} hrs (₹${att.otAmount.toInt()})",
                                        fontSize = 12.sp,
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
                                    text = when (att.status) {
                                        "Present" -> "उपस्थित (Present)"
                                        "Half Day" -> "आधा दिन (Half Day)"
                                        else -> "अनुपस्थित (Absent)"
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 11.sp,
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
            }
        }

        // ================= TAB 2: PAYMENT SLIPS & PERMANENT HISTORY =================
        if (selectedTab == 2) {
            item {
                Text(
                    text = if (isHindi) "📜 सभी भुगतान रिकॉर्ड (Permanent Payments History)" else "Payment Receipts & History",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isHindi) "महीने व साल अनुसार हमेशा सुरक्षित उपलब्ध" else "Permanent & immutable records",
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
                                text = if (isHindi) "अभी तक कोई पेमेंट स्लिप नहीं है।" else "No payment slips recorded yet.",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(payments, key = { it.paymentId }) { p ->
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
                                        text = "✅ Paid Amount",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "₹${p.amount.toInt()}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        text = "STATUS: PAID",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "📅 Payment Date: ${p.paymentDate}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (p.salaryCycle.isNotBlank()) {
                                Text(
                                    text = "🔄 Salary Cycle: ${p.salaryCycle}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (p.paymentReference.isNotBlank()) {
                                Text(
                                    text = "🔢 Ref / UTR: ${p.paymentReference}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

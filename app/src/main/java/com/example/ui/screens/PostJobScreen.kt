package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CompanyGreen
import com.example.ui.theme.PrimaryBlue

@Composable
fun PostJobScreen(
    isHindi: Boolean,
    onJobSubmit: (
        company: String,
        title: String,
        category: String,
        salary: String,
        workers: Int,
        timing: String,
        location: String,
        distance: Double,
        phone: String,
        description: String,
        isUrgent: Boolean
    ) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var companyName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Helper") }
    var salary by remember { mutableStateOf("") }
    var workers by remember { mutableStateOf("") }
    var timing by remember { mutableStateOf("9 AM - 6 PM") }
    var location by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("2.0") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

    val categories = listOf("Helper", "Packing", "Delivery", "Security", "Cook", "Driver", "Warehouse", "Electrician")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("post_job_screen"),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Back Button & Title
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
                        .testTag("post_job_back_button")
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

                Text(
                    text = if (isHindi) "📢 नई Vacancy डालें" else "📢 Post New Vacancy",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Category selector chips
                    Text(
                        text = if (isHindi) "नौकरी की श्रेणी (Category)" else "Job Category",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = cat
                                    if (jobTitle.isBlank()) jobTitle = cat
                                },
                                label = { Text(cat, fontSize = 12.sp) }
                            )
                        }
                    }

                    // 1. Company Name
                    FormLabel(if (isHindi) "Company का नाम" else "Company Name")
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        placeholder = { Text(if (isHindi) "जैसे ABC Company" else "e.g., ABC Company") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_company_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Job Title
                    FormLabel(if (isHindi) "काम का नाम (Job Title)" else "Job Title")
                    OutlinedTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        placeholder = { Text(if (isHindi) "जैसे Helper, Food Packing" else "e.g., Helper, Packing Worker") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_job_title_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Salary
                    FormLabel(if (isHindi) "Salary (वेतन)" else "Salary")
                    OutlinedTextField(
                        value = salary,
                        onValueChange = { salary = it },
                        placeholder = { Text(if (isHindi) "जैसे ₹15,000/month" else "e.g., ₹15,000/month") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_salary_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Workers Required
                    FormLabel(if (isHindi) "कितने Workers चाहिए?" else "Workers Required")
                    OutlinedTextField(
                        value = workers,
                        onValueChange = { workers = it },
                        placeholder = { Text(if (isHindi) "जैसे 10" else "e.g., 10") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_workers_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. Duty Timing
                    FormLabel(if (isHindi) "Duty Timing" else "Duty Timing")
                    OutlinedTextField(
                        value = timing,
                        onValueChange = { timing = it },
                        placeholder = { Text(if (isHindi) "जैसे 9 AM - 6 PM" else "e.g., 9 AM - 6 PM") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_timing_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. Company Location
                    FormLabel(if (isHindi) "Company Location" else "Company Location")
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = { Text(if (isHindi) "जैसे Noida Sector 62" else "e.g., Noida Sector 62") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_location_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 7. Distance (KM)
                    FormLabel(if (isHindi) "Distance KM (दूरी)" else "Distance in KM")
                    OutlinedTextField(
                        value = distance,
                        onValueChange = { distance = it },
                        placeholder = { Text("जैसे 2.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_distance_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 8. Contact Number
                    FormLabel(if (isHindi) "Contact Number (मोबाइल नंबर)" else "Contact Phone")
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = { Text(if (isHindi) "जैसे 9876543210" else "e.g., 9876543210") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_phone_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 9. Job Description
                    FormLabel(if (isHindi) "काम की पूरी जानकारी" else "Full Job Description")
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text(if (isHindi) "यहाँ काम की पूरी जानकारी लिखें (जैसे शिफ्ट, ओवरटाइम, खाना आदि)" else "Enter full description, shifts, overtime, perks...") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_description_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Urgent hiring toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "⚡ तुरंत जॉइनिंग चाहिए? (Urgent)" else "⚡ Urgent Hiring?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isHindi) "वैकेंसी पर तुरंत भर्ती का टैग दिखेगा" else "Highlights job with an urgent badge",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isUrgent,
                            onCheckedChange = { isUrgent = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button: 🚀 Job Post करें
                    Button(
                        onClick = {
                            if (companyName.isBlank()) {
                                Toast.makeText(context, if (isHindi) "कृपया कंपनी का नाम भरें" else "Please enter company name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (jobTitle.isBlank()) {
                                Toast.makeText(context, if (isHindi) "कृपया काम का नाम भरें" else "Please enter job title", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (salary.isBlank()) {
                                Toast.makeText(context, if (isHindi) "कृपया सैलरी भरें" else "Please enter salary", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (phone.isBlank()) {
                                Toast.makeText(context, if (isHindi) "कृपया मोबाइल नंबर भरें" else "Please enter contact phone", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (location.isBlank()) {
                                Toast.makeText(context, if (isHindi) "कृपया लोकेशन भरें" else "Please enter location", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val workersCount = workers.toIntOrNull() ?: 1
                            val distVal = distance.toDoubleOrNull() ?: 2.0

                            onJobSubmit(
                                companyName,
                                jobTitle,
                                selectedCategory,
                                salary,
                                workersCount,
                                timing,
                                location,
                                distVal,
                                phone,
                                description,
                                isUrgent
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_job_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = if (isHindi) "🚀 Job Post करें" else "🚀 Post Job Vacancy",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 4.dp, top = 2.dp)
    )
}

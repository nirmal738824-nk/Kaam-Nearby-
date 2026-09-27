package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApplicationEntity
import com.example.data.model.JobEntity
import com.example.ui.components.DistancePill
import com.example.ui.components.IntentActions
import com.example.ui.components.UrgentBadge
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.CompanyGreen
import com.example.ui.theme.CompanyGreenLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight

@Composable
fun JobDetailsScreen(
    job: JobEntity?,
    application: ApplicationEntity?,
    isHindi: Boolean,
    onApply: (JobEntity) -> Unit,
    onOpenChat: ((ApplicationEntity) -> Unit)? = null,
    onMarkArrived: (String, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showArrivedDialog by remember { mutableStateOf(false) }

    if (job == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (isHindi) "Job की जानकारी लोड हो रही है..." else "Loading job details...")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBackClick) {
                    Text(if (isHindi) "वापस जाएं" else "Go Back")
                }
            }
        }
        return
    }

    if (showArrivedDialog) {
        AlertDialog(
            onDismissRequest = { showArrivedDialog = false },
            title = {
                Text(
                    text = if (isHindi) "🚶 Company पहुँच गया?" else "🚶 Arrived at Company?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isHindi) {
                        "क्या आप ${job.company} की लोकेशन पर पहुँच गए हैं? कंपनी को आपकी arrival की सूचना भेज दी जाएगी।"
                    } else {
                        "Have you reached ${job.company}'s work location? The company will be notified of your arrival."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMarkArrived(job.id, job.title)
                        showArrivedDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {
                    Text(if (isHindi) "हाँ, मैं पहुँच गया" else "Yes, I Arrived")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showArrivedDialog = false }) {
                    Text(if (isHindi) "नहीं" else "Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("job_details_screen"),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Back Button
        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBackClick)
                    .testTag("details_back_button")
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
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Main Detail Box (matching .detail-box in HTML)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Job Title
                    Text(
                        text = job.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (job.isVerified) {
                            VerifiedBadge()
                        }
                        if (job.isUrgent) {
                            UrgentBadge()
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Company & key details
                    DetailRow(icon = "🏢", label = if (isHindi) "Company" else "Company", value = job.company, isBold = true)
                    DetailRow(icon = "💰", label = if (isHindi) "Salary" else "Salary", value = job.salary, isBold = true, highlightGreen = true)
                    DetailRow(icon = "👷", label = if (isHindi) "Workers Required" else "Workers Required", value = "${job.workers} पद")
                    DetailRow(icon = "⏰", label = if (isHindi) "Duty" else "Duty Timing", value = job.timing)
                    DetailRow(icon = "📍", label = if (isHindi) "Location" else "Location", value = job.location)
                    DetailRow(icon = "📏", label = if (isHindi) "Distance" else "Distance", value = "${job.distance} KM", isBold = true)

                    // Application Status Banner if applied
                    if (application != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = if (application.hasArrived) AccentPurple.copy(alpha = 0.15f) else CompanyGreenLight
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (application.hasArrived) Icons.Default.DirectionsWalk else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (application.hasArrived) AccentPurple else CompanyGreen
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (application.hasArrived) {
                                            if (isHindi) "🚶 आप Company पहुँच चुके हैं" else "🚶 You have arrived at company"
                                        } else {
                                            if (isHindi) "✅ आवेदन भेजा जा चुका है" else "✅ Application Submitted"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (application.hasArrived) AccentPurple else CompanyGreen
                                    )
                                    Text(
                                        text = if (application.hasArrived) {
                                            if (isHindi) "Company को आपकी मौजूदगी पता चल गई है" else "Company has been notified of your presence"
                                        } else {
                                            if (isHindi) "अब आप सीधे कॉल कर सकते हैं या लोकेशन पर जा सकते हैं" else "You can call or reach the location now"
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // Description section: "काम की जानकारी"
                    Text(
                        text = if (isHindi) "काम की जानकारी" else "Job Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = job.description.ifBlank {
                            if (isHindi) "कोई अतिरिक्त जानकारी नहीं है।" else "No additional description provided."
                        },
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4 Action Buttons as requested in prototype:
                    // 1. ✅ Apply Now
                    Button(
                        onClick = { onApply(job) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("apply_now_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (application != null) CompanyGreen else PrimaryBlue
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (application != null) {
                                if (isHindi) "✓ आवेदन भेजा गया (Applied)" else "✓ Applied"
                            } else {
                                if (isHindi) "✅ Apply Now" else "✅ Apply Now"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. 📞 Company को Call करें
                    Button(
                        onClick = { IntentActions.callPhone(context, job.phone) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("call_company_details_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CompanyGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "📞 Company को Call करें" else "📞 Call Company",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (application != null && onOpenChat != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onOpenChat(application) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("chat_company_details_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "💬 Company से Chat करें" else "💬 Chat with Company",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. 📍 Map में Location देखें
                    Button(
                        onClick = { IntentActions.openMap(context, job.location) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("open_map_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "📍 Map में Location देखें" else "📍 View Location on Map",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. 🚶 Company पहुँच गया
                    Button(
                        onClick = { showArrivedDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("mark_arrived_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (application?.hasArrived == true) {
                                if (isHindi) "🚶 Company पहुँच गया (Confirmed)" else "🚶 Arrival Confirmed"
                            } else {
                                if (isHindi) "🚶 Company पहुँच गया" else "🚶 I Arrived at Company"
                            },
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
private fun DetailRow(
    icon: String,
    label: String,
    value: String,
    isBold: Boolean = false,
    highlightGreen: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (highlightGreen) CompanyGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}

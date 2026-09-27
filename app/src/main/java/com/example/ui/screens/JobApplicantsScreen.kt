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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.IntentActions
import com.example.ui.components.UnreadBadge
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.CompanyGreen
import com.example.ui.theme.CompanyGreenLight
import com.example.ui.theme.PrimaryBlue

@Composable
fun JobApplicantsScreen(
    jobTitle: String,
    applicants: List<ApplicationEntity>,
    unreadCounts: Map<String, Int>,
    isHindi: Boolean,
    onAcceptApplicant: (String) -> Unit,
    onRejectApplicant: (String) -> Unit,
    onOpenChat: (ApplicationEntity) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("job_applicants_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBackClick)
                    .testTag("applicants_back_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHindi) "वापस" else "Back",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = if (isHindi) "उम्मीदवार (Applicants • ${applicants.size})" else "Applicants (${applicants.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "💼 $jobTitle",
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (applicants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isHindi) "इस वैकेंसी के लिए अभी तक कोई आवेदन नहीं आया है।" else "No applicants yet for this vacancy.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(applicants, key = { it.id }) { applicant ->
                    val chatId = "chat_${applicant.jobId}_${applicant.id}"
                    val unreadCount = unreadCounts[chatId] ?: 0

                    ApplicantCard(
                        applicant = applicant,
                        unreadCount = unreadCount,
                        isHindi = isHindi,
                        onCall = { IntentActions.callPhone(context, applicant.applicantPhone) },
                        onAccept = { onAcceptApplicant(applicant.id) },
                        onReject = { onRejectApplicant(applicant.id) },
                        onChat = { onOpenChat(applicant) }
                    )
                }
            }
        }
    }
}

@Composable
fun ApplicantCard(
    applicant: ApplicationEntity,
    unreadCount: Int,
    isHindi: Boolean,
    onCall: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("applicant_card_${applicant.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar + Info + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "👷", fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = applicant.applicantName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (unreadCount > 0) {
                            UnreadBadge(count = unreadCount)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "📱 ${applicant.applicantPhone}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "💼 Applied for: ${applicant.jobTitle}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )

                    if (applicant.hasArrived) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isHindi) "कंपनी लोकेशन पर पहुँच चुका है!" else "Arrived at company location!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentPurple
                            )
                        }
                    }
                }

                // Application Status Badge
                StatusPill(status = applicant.status, isHindi = isHindi)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Row: 📞 Call & 💬 Chat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. 📞 Call
                Button(
                    onClick = onCall,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("applicant_call_btn_${applicant.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CompanyGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHindi) "Call" else "Call",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 2. 💬 Chat
                Button(
                    onClick = onChat,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("applicant_chat_btn_${applicant.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chat",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHindi) "Chat" else "Chat",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Red
                        ) {
                            Text(
                                text = "$unreadCount",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: Accept & Reject Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Accept Action
                Button(
                    onClick = onAccept,
                    enabled = applicant.status != "Accepted",
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("applicant_accept_btn_${applicant.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (applicant.status == "Accepted") CompanyGreen.copy(alpha = 0.5f) else CompanyGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (applicant.status == "Accepted") {
                            if (isHindi) "स्वीकृत ✓" else "Accepted ✓"
                        } else {
                            if (isHindi) "Accept करें" else "Accept"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Reject Action
                OutlinedButton(
                    onClick = onReject,
                    enabled = applicant.status != "Rejected",
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("applicant_reject_btn_${applicant.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (applicant.status == "Rejected") Color.Gray else Color.Red
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Reject",
                        modifier = Modifier.size(16.dp),
                        tint = if (applicant.status == "Rejected") Color.Gray else Color.Red
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (applicant.status == "Rejected") {
                            if (isHindi) "अस्वीकृत ✗" else "Rejected ✗"
                        } else {
                            if (isHindi) "Reject करें" else "Reject"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (applicant.status == "Rejected") Color.Gray else Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: String, isHindi: Boolean) {
    val (label, bgColor, textColor) = when (status) {
        "Accepted" -> Triple(
            if (isHindi) "स्वीकृत" else "Accepted",
            CompanyGreenLight,
            CompanyGreen
        )
        "Rejected" -> Triple(
            if (isHindi) "अस्वीकृत" else "Rejected",
            Color(0xFFFFEBEE),
            Color(0xFFC62828)
        )
        else -> Triple(
            if (isHindi) "विचाराधीन" else "Pending",
            AccentOrange.copy(alpha = 0.15f),
            AccentOrange
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

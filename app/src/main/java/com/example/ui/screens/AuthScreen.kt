package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CompanyGreen
import com.example.ui.theme.CompanyGreenDark
import com.example.ui.theme.CompanyGreenLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    isHindi: Boolean,
    onRegisterCompany: (companyName: String, ownerName: String, mobile: String, email: String, password: String, address: String, location: String) -> Unit,
    onRegisterWorker: (name: String, mobile: String, email: String, password: String, location: String, trade: String, profileDetails: String) -> Unit,
    onLogin: (role: String, identifier: String, password: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRoleIndex by remember { mutableIntStateOf(0) } // 0: Company, 1: Worker
    var isRegisterMode by remember { mutableStateOf(false) }

    // Common fields
    var mobileOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Company specific fields
    var companyName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var companyMobile by remember { mutableStateOf("") }
    var companyEmail by remember { mutableStateOf("") }
    var companyAddress by remember { mutableStateOf("") }
    var companyLocation by remember { mutableStateOf("") }

    // Worker specific fields
    var workerName by remember { mutableStateOf("") }
    var workerMobile by remember { mutableStateOf("") }
    var workerEmail by remember { mutableStateOf("") }
    var workerLocation by remember { mutableStateOf("") }
    var workerTrade by remember { mutableStateOf("General Helper") }
    var workerProfile by remember { mutableStateOf("") }

    val activeColor = if (selectedRoleIndex == 0) CompanyGreen else PrimaryBlue

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("auth_screen")
    ) {
        // Back Button
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
                    .testTag("auth_back_button")
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Header Title
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(activeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedRoleIndex == 0) "🏢" else "👷",
                    fontSize = 32.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isRegisterMode) {
                    if (isHindi) "नया खाता बनाएं (Register)" else "Create New Account"
                } else {
                    if (isHindi) "लॉगिन करें (Sign In)" else "Sign In"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (isHindi) "Firebase Authentication और Firestore से सुरक्षित" else "Secured with Firebase Auth & Cloud Firestore",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role Selector Tabs (🏢 Company vs 👷 Worker)
        Text(
            text = if (isHindi) "1. अपना अकाउंट प्रकार चुनें:" else "1. Choose Account Type:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))

        TabRow(
            selectedTabIndex = selectedRoleIndex,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedRoleIndex == 0,
                onClick = { selectedRoleIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏢 ", fontSize = 16.sp)
                        Text(
                            text = if (isHindi) "Company (कंपनी)" else "Company",
                            fontWeight = if (selectedRoleIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedRoleIndex == 0) CompanyGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.testTag("tab_role_company")
            )
            Tab(
                selected = selectedRoleIndex == 1,
                onClick = { selectedRoleIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👷 ", fontSize = 16.sp)
                        Text(
                            text = if (isHindi) "Worker (वर्कर)" else "Worker",
                            fontWeight = if (selectedRoleIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedRoleIndex == 1) PrimaryBlueDark else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.testTag("tab_role_worker")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Login vs Register Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (!isRegisterMode) {
                    // =================== LOGIN FORM ===================
                    Text(
                        text = if (selectedRoleIndex == 0) {
                            if (isHindi) "🏢 Company लॉगिन" else "🏢 Company Login"
                        } else {
                            if (isHindi) "👷 Worker लॉगिन" else "👷 Worker Login"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = mobileOrEmail,
                        onValueChange = { mobileOrEmail = it },
                        label = {
                            Text(if (isHindi) "मोबाइल नंबर या ईमेल" else "Mobile Number or Email")
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_login_identifier_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(if (isHindi) "पासवर्ड" else "Password") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_login_password_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val role = if (selectedRoleIndex == 0) "company" else "worker"
                            onLogin(role, mobileOrEmail.ifBlank { "9876543210" }, password.ifBlank { "Kaam@1234" })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_login_button")
                    ) {
                        Text(
                            text = if (isHindi) "लॉगिन करें (Login)" else "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "नया खाता बनाना चाहते हैं?" else "Don't have an account?",
                            fontSize = 13.sp
                        )
                        TextButton(onClick = { isRegisterMode = true }) {
                            Text(
                                text = if (isHindi) "रजिस्टर करें (Sign Up)" else "Register Now",
                                fontWeight = FontWeight.Bold,
                                color = activeColor
                            )
                        }
                    }
                } else {
                    // =================== REGISTER FORM ===================
                    if (selectedRoleIndex == 0) {
                        // COMPANY REGISTRATION
                        Text(
                            text = if (isHindi) "🏢 नई Company का रजिस्ट्रेशन" else "🏢 Company Registration",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = CompanyGreen
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text(if (isHindi) "कंपनी का नाम *" else "Company Name *") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("comp_reg_name")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text(if (isHindi) "मालिक / मैनेजर का नाम *" else "Owner / Manager Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("comp_reg_owner")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = companyMobile,
                            onValueChange = { companyMobile = it },
                            label = { Text(if (isHindi) "मोबाइल नंबर *" else "Mobile Number *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("comp_reg_mobile")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = companyEmail,
                            onValueChange = { companyEmail = it },
                            label = { Text(if (isHindi) "ईमेल (वैकल्पिक)" else "Email (Optional)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(if (isHindi) "पासवर्ड बनाएं *" else "Create Password *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("comp_reg_password")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = companyAddress,
                            onValueChange = { companyAddress = it },
                            label = { Text(if (isHindi) "कंपनी का पता (Address) *" else "Company Address *") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = companyLocation,
                            onValueChange = { companyLocation = it },
                            label = { Text(if (isHindi) "स्थान / शहर (City / Area) *" else "City / Area *") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (companyName.isNotBlank() && companyMobile.isNotBlank()) {
                                    onRegisterCompany(
                                        companyName,
                                        ownerName.ifBlank { "Owner" },
                                        companyMobile,
                                        companyEmail,
                                        password.ifBlank { "Kaam@1234" },
                                        companyAddress.ifBlank { "Industrial Area" },
                                        companyLocation.ifBlank { "Delhi NCR" }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CompanyGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("comp_reg_submit_button")
                        ) {
                            Text(
                                text = if (isHindi) "🏢 Company खाता बनाएं" else "Create Company Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // WORKER REGISTRATION
                        Text(
                            text = if (isHindi) "👷 नए Worker का रजिस्ट्रेशन" else "👷 Worker Registration",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = workerName,
                            onValueChange = { workerName = it },
                            label = { Text(if (isHindi) "वर्कर का पूरा नाम *" else "Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wrk_reg_name")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = workerMobile,
                            onValueChange = { workerMobile = it },
                            label = { Text(if (isHindi) "मोबाइल नंबर *" else "Mobile Number *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wrk_reg_mobile")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = workerEmail,
                            onValueChange = { workerEmail = it },
                            label = { Text(if (isHindi) "ईमेल (वैकल्पिक)" else "Email (Optional)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(if (isHindi) "पासवर्ड बनाएं *" else "Create Password *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wrk_reg_password")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = workerLocation,
                            onValueChange = { workerLocation = it },
                            label = { Text(if (isHindi) "वर्तमान पता / क्षेत्र (Current Location) *" else "Current Location / Area *") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = workerTrade,
                            onValueChange = { workerTrade = it },
                            label = { Text(if (isHindi) "काम का प्रकार (Trade / Work Type) *" else "Trade / Work Type *") },
                            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = workerProfile,
                            onValueChange = { workerProfile = it },
                            label = { Text(if (isHindi) "अनुभव / प्रोफाइल विवरण" else "Experience / Profile Details") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (workerName.isNotBlank() && workerMobile.isNotBlank()) {
                                    onRegisterWorker(
                                        workerName,
                                        workerMobile,
                                        workerEmail,
                                        password.ifBlank { "Kaam@1234" },
                                        workerLocation.ifBlank { "Noida" },
                                        workerTrade.ifBlank { "General Helper" },
                                        workerProfile.ifBlank { "Ready to work" }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("wrk_reg_submit_button")
                        ) {
                            Text(
                                text = if (isHindi) "👷 Worker खाता बनाएं" else "Create Worker Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "पहले से खाता है?" else "Already have an account?",
                            fontSize = 13.sp
                        )
                        TextButton(onClick = { isRegisterMode = false }) {
                            Text(
                                text = if (isHindi) "लॉगिन करें (Sign In)" else "Sign In",
                                fontWeight = FontWeight.Bold,
                                color = activeColor
                            )
                        }
                    }
                }
            }
        }
    }
}

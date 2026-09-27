package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ApplicationEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CompanyEntity
import com.example.data.model.JobEntity
import com.example.data.model.LeaveEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.SalaryRecordEntity
import com.example.data.model.WorkerEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.JobRepository
import com.example.data.repository.SalaryRepository
import com.example.ui.navigation.Screen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class WorkerProfile(
    val name: String = "राम कुमार",
    val phone: String = "9876543210",
    val trade: String = "General Helper & Worker",
    val location: String = "Noida Sector 62"
)

data class SalaryCycleInfo(
    val salaryDate: Int,
    val startDate: String,
    val endDate: String,
    val label: String
)

class KaamNearbyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JobRepository
    private val chatRepository: ChatRepository
    private val salaryRepository: SalaryRepository
    val authRepository: AuthRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = JobRepository(application, database.jobDao())
        chatRepository = ChatRepository(application, database.chatDao())
        salaryRepository = SalaryRepository(application, database.salaryDao())
        authRepository = AuthRepository(application, database.salaryDao())

        // Start real-time Firestore sync
        repository.startRealtimeSync(viewModelScope)
        salaryRepository.startRealtimeSync(viewModelScope)

        viewModelScope.launch {
            authRepository.initSession()
        }
    }

    // Navigation Stack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val screenStack: StateFlow<List<Screen>> = _screenStack.asStateFlow()

    // Filters & Language
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _distanceFilter = MutableStateFlow<Double?>(null)
    val distanceFilter: StateFlow<Double?> = _distanceFilter.asStateFlow()

    private val _selectedCategory = MutableStateFlow("सब")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _workerProfile = MutableStateFlow(WorkerProfile())
    val workerProfile: StateFlow<WorkerProfile> = _workerProfile.asStateFlow()

    private val _isHindi = MutableStateFlow(true)
    val isHindi: StateFlow<Boolean> = _isHindi.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Auth State
    val currentRole: StateFlow<String> = authRepository.currentRole
    val currentCompany: StateFlow<CompanyEntity?> = authRepository.currentCompany
    val currentWorker: StateFlow<WorkerEntity?> = authRepository.currentWorker
    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn

    // Jobs & Applications
    val allJobs: StateFlow<List<JobEntity>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allApplications: StateFlow<List<ApplicationEntity>> = repository.allApplications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat unread counts
    val companyUnreadCounts: StateFlow<Map<String, Int>> = chatRepository.getAllUnreadCountsFlow("COMPANY")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val workerUnreadCounts: StateFlow<Map<String, Int>> = chatRepository.getAllUnreadCountsFlow("WORKER")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Filtered jobs
    val filteredJobs: StateFlow<List<JobEntity>> = combine(
        allJobs,
        _searchQuery,
        _distanceFilter,
        _selectedCategory
    ) { jobs, query, maxDist, category ->
        jobs.filter { job ->
            val matchesQuery = query.isBlank() ||
                job.title.contains(query, ignoreCase = true) ||
                job.company.contains(query, ignoreCase = true) ||
                job.location.contains(query, ignoreCase = true) ||
                job.category.contains(query, ignoreCase = true)

            val matchesDistance = maxDist == null || job.distance <= maxDist

            val matchesCategory = category == "सब" || category == "All" ||
                job.category.equals(category, ignoreCase = true) ||
                job.title.contains(category, ignoreCase = true)

            matchesQuery && matchesDistance && matchesCategory
        }.sortedBy { it.distance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Worker's Own Salary & Attendance Streams (Strictly worker's own data)
    val workerAttendance: StateFlow<List<AttendanceEntity>> = currentWorker.flatMapLatest { wrk ->
        val id = wrk?.workerId ?: "WRK-NOIDA-01"
        salaryRepository.getAttendanceForWorkerFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workerLeaves: StateFlow<List<LeaveEntity>> = currentWorker.flatMapLatest { wrk ->
        val id = wrk?.workerId ?: "WRK-NOIDA-01"
        salaryRepository.getLeavesForWorkerFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workerSalaryRecords: StateFlow<List<SalaryRecordEntity>> = currentWorker.flatMapLatest { wrk ->
        val id = wrk?.workerId ?: "WRK-NOIDA-01"
        salaryRepository.getSalaryRecordsForWorkerFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workerPayments: StateFlow<List<PaymentEntity>> = currentWorker.flatMapLatest { wrk ->
        val id = wrk?.workerId ?: "WRK-NOIDA-01"
        salaryRepository.getPaymentsForWorkerFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Company's Streams
    val companyAttendance: StateFlow<List<AttendanceEntity>> = currentCompany.flatMapLatest { comp ->
        val id = comp?.companyId ?: "COMP-DELHI-01"
        salaryRepository.getAttendanceForCompanyFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companyLeaves: StateFlow<List<LeaveEntity>> = currentCompany.flatMapLatest { comp ->
        val id = comp?.companyId ?: "COMP-DELHI-01"
        salaryRepository.getLeavesForCompanyFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companySalaryRecords: StateFlow<List<SalaryRecordEntity>> = currentCompany.flatMapLatest { comp ->
        val id = comp?.companyId ?: "COMP-DELHI-01"
        salaryRepository.getSalaryRecordsForCompanyFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companyPayments: StateFlow<List<PaymentEntity>> = currentCompany.flatMapLatest { comp ->
        val id = comp?.companyId ?: "COMP-DELHI-01"
        salaryRepository.getPaymentsForCompanyFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation Methods
    fun currentScreen(): Screen = _screenStack.value.lastOrNull() ?: Screen.Home

    fun navigateTo(screen: Screen) {
        val currentList = _screenStack.value.toMutableList()
        if (currentList.lastOrNull() != screen) {
            currentList.add(screen)
            _screenStack.value = currentList
        }
    }

    fun popBack(): Boolean {
        val currentList = _screenStack.value.toMutableList()
        return if (currentList.size > 1) {
            currentList.removeAt(currentList.lastIndex)
            _screenStack.value = currentList
            true
        } else {
            false
        }
    }

    fun navigateToRoot(screen: Screen) {
        _screenStack.value = listOf(screen)
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setDistanceFilter(maxDistance: Double?) { _distanceFilter.value = maxDistance }
    fun setSelectedCategory(category: String) { _selectedCategory.value = category }
    fun toggleLanguage() { _isHindi.value = !_isHindi.value }

    fun updateWorkerProfile(name: String, phone: String, trade: String, location: String) {
        _workerProfile.value = WorkerProfile(
            name = name,
            phone = phone,
            trade = trade,
            location = location
        )
        _userMessage.value = if (_isHindi.value) "प्रोफाइल अपडेट हो गई!" else "Profile updated successfully!"
    }

    // Role & Auth Actions
    fun switchRole(role: String) {
        authRepository.setRole(role)
        if (role == "company") {
            navigateToRoot(Screen.CompanyDashboard)
        } else {
            navigateToRoot(Screen.WorkerJobs)
        }
    }

    fun registerCompany(
        companyName: String,
        ownerName: String,
        mobile: String,
        email: String,
        password: String,
        address: String,
        location: String
    ) {
        viewModelScope.launch {
            val result = authRepository.registerCompany(
                companyName = companyName,
                ownerName = ownerName,
                mobile = mobile,
                emailInput = email,
                passwordInput = password,
                address = address,
                location = location
            )
            if (result.isSuccess) {
                _userMessage.value = if (_isHindi.value) "🏢 Company Account सफलतापूर्वक बन गया!" else "Company Account created successfully!"
                navigateToRoot(Screen.CompanyDashboard)
            } else {
                _userMessage.value = "Registration Failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun registerWorker(
        name: String,
        mobile: String,
        email: String,
        password: String,
        location: String,
        trade: String,
        profileDetails: String
    ) {
        viewModelScope.launch {
            val result = authRepository.registerWorker(
                name = name,
                mobile = mobile,
                emailInput = email,
                passwordInput = password,
                location = location,
                trade = trade,
                profileDetails = profileDetails
            )
            if (result.isSuccess) {
                _userMessage.value = if (_isHindi.value) "👷 Worker Account सफलतापूर्वक बन गया!" else "Worker Account created successfully!"
                navigateToRoot(Screen.WorkerJobs)
            } else {
                _userMessage.value = "Registration Failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun login(role: String, identifier: String, password: String) {
        viewModelScope.launch {
            val res = authRepository.login(role, identifier, password)
            if (res.isSuccess) {
                _userMessage.value = if (_isHindi.value) "लॉगिन सफल!" else "Login Successful!"
                if (role == "company") {
                    navigateToRoot(Screen.CompanyDashboard)
                } else {
                    navigateToRoot(Screen.WorkerJobs)
                }
            } else {
                _userMessage.value = "Login Failed: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun logout() {
        authRepository.logout()
        navigateToRoot(Screen.Auth)
    }

    // Job Actions
    fun applyForJob(job: JobEntity) {
        viewModelScope.launch {
            val profile = currentWorker.value?.let {
                WorkerProfile(it.name, it.mobile, it.trade, it.location)
            } ?: _workerProfile.value

            val appliedApp = repository.applyToJob(
                job = job,
                applicantName = profile.name,
                applicantPhone = profile.phone
            )
            if (appliedApp != null) {
                val chatId = "chat_${job.id}_${appliedApp.id}"
                chatRepository.sendMessage(
                    chatId = chatId,
                    jobId = job.id,
                    applicationId = appliedApp.id,
                    senderRole = "WORKER",
                    senderName = profile.name,
                    text = if (_isHindi.value) "नमस्ते सर, मैंने ${job.title} पद के लिए आवेदन किया है।" else "Hello sir, I have applied for the ${job.title} position."
                )

                _userMessage.value = if (_isHindi.value) {
                    "✅ आपका आवेदन Cloud Firestore में दर्ज हो गया!\nCompany: ${job.company}"
                } else {
                    "✅ Application saved to Firestore!\nCompany: ${job.company}"
                }
            } else {
                _userMessage.value = if (_isHindi.value) {
                    "आप इस नौकरी के लिए पहले ही आवेदन कर चुके हैं।"
                } else {
                    "You have already applied for this job."
                }
            }
        }
    }

    fun acceptApplication(applicationId: String) {
        viewModelScope.launch {
            repository.updateApplicationStatus(applicationId, "Accepted")
            _userMessage.value = if (_isHindi.value) {
                "✅ Worker को नौकरी पर Select/Approve कर लिया गया!"
            } else {
                "✅ Worker Selected & Approved!"
            }
        }
    }

    fun rejectApplication(applicationId: String) {
        viewModelScope.launch {
            repository.updateApplicationStatus(applicationId, "Rejected")
            _userMessage.value = if (_isHindi.value) {
                "❌ आवेदन अस्वीकार (Rejected) किया गया।"
            } else {
                "❌ Application Rejected."
            }
        }
    }

    fun markArrived(jobId: String, jobTitle: String) {
        viewModelScope.launch {
            repository.markArrived(jobId)
            _userMessage.value = if (_isHindi.value) {
                "📍 आपकी arrival company को बताई गई!\nJob: $jobTitle"
            } else {
                "📍 Arrival marked successfully!\nJob: $jobTitle"
            }
        }
    }

    fun postJob(
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
    ) {
        viewModelScope.launch {
            val companyId = currentCompany.value?.companyId ?: "COMP-DELHI-01"
            val newJob = JobEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                company = company.trim(),
                title = title.trim(),
                category = category.trim().ifBlank { "Helper" },
                salary = salary.trim(),
                salaryAmount = parseSalary(salary),
                workers = if (workers > 0) workers else 1,
                timing = timing.trim().ifBlank { "9 AM - 6 PM" },
                location = location.trim(),
                distance = if (distance > 0) distance else 1.0,
                phone = phone.trim(),
                description = description.trim(),
                isVerified = true,
                isUrgent = isUrgent,
                applicantsCount = 0,
                createdAt = System.currentTimeMillis()
            )
            repository.insertJob(newJob)
            _userMessage.value = if (_isHindi.value) {
                "🎉 Vacancy successfully Cloud Firestore में post हो गई!"
            } else {
                "🎉 Vacancy posted to Cloud Firestore!"
            }
            navigateTo(Screen.CompanyDashboard)
        }
    }

    fun deleteJob(jobId: String) {
        viewModelScope.launch {
            repository.deleteJob(jobId)
            _userMessage.value = if (_isHindi.value) "Job हटा दी गई" else "Job deleted"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun getApplicationsForJob(jobId: String) = repository.getApplicationsForJob(jobId)
    fun getApplicationForJobFlow(jobId: String) = repository.getApplicationForJobFlow(jobId)

    // Chat operations
    fun getChatMessages(chatId: String): Flow<List<ChatMessageEntity>> {
        chatRepository.listenToChat(chatId, viewModelScope)
        return chatRepository.getMessagesFlow(chatId)
    }

    fun sendChatMessage(
        chatId: String,
        jobId: String,
        applicationId: String,
        senderRole: String,
        senderName: String,
        text: String
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(
                chatId = chatId,
                jobId = jobId,
                applicationId = applicationId,
                senderRole = senderRole,
                senderName = senderName,
                text = text
            )
        }
    }

    fun markChatRead(chatId: String, currentRole: String) {
        viewModelScope.launch {
            chatRepository.markChatAsRead(chatId, currentRole)
        }
    }

    // ==================== SALARY, ATTENDANCE & OT SYSTEM ====================

    // 1. Company sets/updates its Salary Payment Date
    fun updateCompanySalaryDate(date: Int) {
        viewModelScope.launch {
            val companyId = currentCompany.value?.companyId ?: "COMP-DELHI-01"
            val cycleInfo = computeSalaryCycle(date)
            salaryRepository.updateCompanySalaryDate(
                companyId = companyId,
                date = date,
                cycleLabel = cycleInfo.label,
                startDate = cycleInfo.startDate,
                endDate = cycleInfo.endDate
            )
            _userMessage.value = if (_isHindi.value) {
                "📅 Salary Payment Date हर महीने की $date तारीख सेट हो गई!\nCycle: ${cycleInfo.label}"
            } else {
                "📅 Salary payment date set to day $date!\nCycle: ${cycleInfo.label}"
            }
        }
    }

    // 2. Company marks attendance and OT for selected worker
    fun markWorkerAttendance(
        workerId: String,
        workerName: String,
        jobId: String,
        date: String,
        status: String, // "Present", "Absent", "Half Day"
        otHours: Double,
        otRate: Double,
        note: String
    ) {
        viewModelScope.launch {
            val companyId = currentCompany.value?.companyId ?: "COMP-DELHI-01"
            val otAmount = otHours * otRate
            val id = "att_${companyId}_${workerId}_$date"

            val attendance = AttendanceEntity(
                id = id,
                workerId = workerId,
                workerName = workerName,
                companyId = companyId,
                jobId = jobId,
                date = date,
                status = status,
                otHours = otHours,
                otRate = otRate,
                otAmount = otAmount,
                note = note,
                markedBy = companyId,
                timestamp = System.currentTimeMillis()
            )

            salaryRepository.markAttendance(attendance)
            _userMessage.value = if (_isHindi.value) {
                "✅ Attendance दर्ज हो गई: $status (OT: $otHours hrs = ₹$otAmount)"
            } else {
                "✅ Attendance recorded: $status (OT: $otHours hrs = ₹$otAmount)"
            }
        }
    }

    // 3. Company records Leave
    fun recordWorkerLeave(
        workerId: String,
        workerName: String,
        jobId: String,
        date: String,
        leaveType: String, // "Weekly Off", "Approved Leave", "Absent"
        reason: String
    ) {
        viewModelScope.launch {
            val companyId = currentCompany.value?.companyId ?: "COMP-DELHI-01"
            val leave = LeaveEntity(
                id = "leave_${companyId}_${workerId}_$date",
                workerId = workerId,
                workerName = workerName,
                companyId = companyId,
                jobId = jobId,
                date = date,
                leaveType = leaveType,
                reason = reason,
                approvedBy = companyId,
                createdAt = System.currentTimeMillis()
            )

            salaryRepository.recordLeave(leave)
            _userMessage.value = if (_isHindi.value) {
                "🌿 छुट्टी दर्ज/स्वीकृत की गई: $leaveType"
            } else {
                "🌿 Leave recorded: $leaveType"
            }
        }
    }

    // 4. Calculate Salary at cycle end
    fun calculateSalaryForWorker(
        workerId: String,
        workerName: String,
        jobId: String,
        jobTitle: String,
        cycleStartDate: String,
        cycleEndDate: String,
        cycleLabel: String,
        totalWorkingDays: Int,
        basicSalary: Double,
        deductions: Double,
        advance: Double
    ) {
        viewModelScope.launch {
            val company = currentCompany.value
            val companyId = company?.companyId ?: "COMP-DELHI-01"
            val companyName = company?.companyName ?: "Company"

            val record = salaryRepository.calculateAndSaveSalary(
                companyId = companyId,
                companyName = companyName,
                workerId = workerId,
                workerName = workerName,
                jobId = jobId,
                jobTitle = jobTitle,
                cycleStartDate = cycleStartDate,
                cycleEndDate = cycleEndDate,
                cycleLabel = cycleLabel,
                totalWorkingDays = totalWorkingDays,
                basicSalary = basicSalary,
                deductions = deductions,
                advance = advance
            )

            _userMessage.value = if (_isHindi.value) {
                "💰 सैलरी कैलकुलेट हो गई: ₹${record.finalSalary.toInt()} (Net Payable)"
            } else {
                "💰 Salary calculated: ₹${record.finalSalary.toInt()} (Net Payable)"
            }
        }
    }

    // 5. Mark Salary as Paid
    fun markSalaryAsPaid(
        salaryRecord: SalaryRecordEntity,
        paymentReference: String
    ) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Calendar.getInstance().time)

            val payment = salaryRepository.markSalaryPaid(
                salaryRecord = salaryRecord,
                paymentReference = paymentReference.ifBlank { "CASH/UPI-" + UUID.randomUUID().toString().take(6).uppercase() },
                amount = salaryRecord.finalSalary,
                otAmount = salaryRecord.otAmount,
                paymentDate = todayStr
            )

            _userMessage.value = if (_isHindi.value) {
                "✅ Salary Paid Successfully! (₹${payment.amount.toInt()} - Ref: ${payment.paymentReference})"
            } else {
                "✅ Salary Paid Successfully! (₹${payment.amount.toInt()} - Ref: ${payment.paymentReference})"
            }
        }
    }

    // Helper: Compute Salary Cycle based on Company's Salary Date
    fun computeSalaryCycle(salaryDate: Int = currentCompany.value?.salaryPaymentDate ?: 10): SalaryCycleInfo {
        val cal = Calendar.getInstance()
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfMonth = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        val startCal = Calendar.getInstance()
        val endCal = Calendar.getInstance()

        if (currentDay > salaryDate) {
            // E.g. Today is Sep 27, salaryDate is 10:
            // Current cycle: 11 Sep to 10 Oct
            startCal.set(Calendar.DAY_OF_MONTH, salaryDate + 1)
            endCal.add(Calendar.MONTH, 1)
            endCal.set(Calendar.DAY_OF_MONTH, salaryDate)
        } else {
            // E.g. Today is Sep 5, salaryDate is 10:
            // Current cycle: 11 Aug to 10 Sep
            startCal.add(Calendar.MONTH, -1)
            startCal.set(Calendar.DAY_OF_MONTH, salaryDate + 1)
            endCal.set(Calendar.DAY_OF_MONTH, salaryDate)
        }

        val startStr = sdfDate.format(startCal.time)
        val endStr = sdfDate.format(endCal.time)
        val startDay = startCal.get(Calendar.DAY_OF_MONTH)
        val endDay = endCal.get(Calendar.DAY_OF_MONTH)
        val startMonth = SimpleDateFormat("dd MMM", Locale.getDefault()).format(startCal.time)
        val endMonth = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(endCal.time)

        val label = "$startMonth – $endMonth"

        return SalaryCycleInfo(
            salaryDate = salaryDate,
            startDate = startStr,
            endDate = endStr,
            label = label
        )
    }

    private fun parseSalary(salaryStr: String): Int {
        val digits = salaryStr.filter { it.isDigit() }
        return digits.toIntOrNull() ?: 15000
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopRealtimeSync()
        salaryRepository.stopRealtimeSync()
    }
}

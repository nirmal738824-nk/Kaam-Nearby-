package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppTopBar
import com.example.ui.components.BottomNavBar
import com.example.ui.components.WorkerProfileDialog
import com.example.ui.navigation.Screen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CompanyDashboardScreen
import com.example.ui.screens.CompanySalaryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JobApplicantsScreen
import com.example.ui.screens.JobDetailsScreen
import com.example.ui.screens.MyApplicationsScreen
import com.example.ui.screens.PostJobScreen
import com.example.ui.screens.WorkerJobsScreen
import com.example.ui.screens.WorkerSalaryScreen
import com.example.ui.theme.KaamNearbyTheme
import com.example.ui.viewmodel.KaamNearbyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: KaamNearbyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KaamNearbyTheme {
                KaamNearbyApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun KaamNearbyApp(viewModel: KaamNearbyViewModel) {
    val screenStack by viewModel.screenStack.collectAsStateWithLifecycle()
    val currentScreen = screenStack.lastOrNull() ?: Screen.Home

    val allJobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val filteredJobs by viewModel.filteredJobs.collectAsStateWithLifecycle()
    val allApplications by viewModel.allApplications.collectAsStateWithLifecycle()
    val companyUnreadCounts by viewModel.companyUnreadCounts.collectAsStateWithLifecycle()
    val workerUnreadCounts by viewModel.workerUnreadCounts.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val distanceFilter by viewModel.distanceFilter.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val workerProfile by viewModel.workerProfile.collectAsStateWithLifecycle()

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentCompany by viewModel.currentCompany.collectAsStateWithLifecycle()
    val currentWorker by viewModel.currentWorker.collectAsStateWithLifecycle()
    val workerAttendance by viewModel.workerAttendance.collectAsStateWithLifecycle()
    val workerLeaves by viewModel.workerLeaves.collectAsStateWithLifecycle()
    val workerSalaryRecords by viewModel.workerSalaryRecords.collectAsStateWithLifecycle()
    val workerPayments by viewModel.workerPayments.collectAsStateWithLifecycle()
    val companyAttendance by viewModel.companyAttendance.collectAsStateWithLifecycle()
    val companySalaryRecords by viewModel.companySalaryRecords.collectAsStateWithLifecycle()
    val companyPayments by viewModel.companyPayments.collectAsStateWithLifecycle()

    var showProfileDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Intercept hardware/system back button
    BackHandler(enabled = screenStack.size > 1) {
        viewModel.popBack()
    }

    // Display snackbars when user actions occur
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUserMessage()
        }
    }

    if (showProfileDialog) {
        WorkerProfileDialog(
            profile = workerProfile,
            isHindi = isHindi,
            onDismiss = { showProfileDialog = false },
            onSave = { name, phone, trade, location ->
                viewModel.updateWorkerProfile(name, phone, trade, location)
            }
        )
    }

    val isChatScreen = currentScreen is Screen.Chat

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isChatScreen) {
                AppTopBar(
                    title = "Kaam Nearby",
                    subtitle = if (isHindi) "अपने आसपास काम खोजें" else "Find Local Work Near You",
                    canNavigateBack = screenStack.size > 1,
                    isHindi = isHindi,
                    onBackClick = { viewModel.popBack() },
                    onLanguageToggle = { viewModel.toggleLanguage() },
                    onProfileClick = { showProfileDialog = true }
                )
            }
        },
        bottomBar = {
            if (!isChatScreen) {
                BottomNavBar(
                    currentScreen = currentScreen,
                    isHindi = isHindi,
                    onTabSelected = { targetScreen ->
                        viewModel.navigateToRoot(targetScreen)
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    is Screen.Home -> {
                        HomeScreen(
                            isHindi = isHindi,
                            recentJobs = allJobs,
                            onNavigate = { screen -> viewModel.navigateTo(screen) },
                            onCategorySelected = { cat -> viewModel.setSelectedCategory(cat) }
                        )
                    }

                    is Screen.WorkerJobs -> {
                        WorkerJobsScreen(
                            jobs = filteredJobs,
                            searchQuery = searchQuery,
                            distanceFilter = distanceFilter,
                            selectedCategory = selectedCategory,
                            isHindi = isHindi,
                            onSearchChanged = { viewModel.setSearchQuery(it) },
                            onDistanceFilterChanged = { viewModel.setDistanceFilter(it) },
                            onCategoryChanged = { viewModel.setSelectedCategory(it) },
                            onJobClick = { jobId -> viewModel.navigateTo(Screen.JobDetails(jobId)) },
                            onOpenSalaryDashboard = { viewModel.navigateTo(Screen.WorkerSalaryDashboard) },
                            onBackClick = { viewModel.popBack() }
                        )
                    }

                    is Screen.Auth -> {
                        AuthScreen(
                            isHindi = isHindi,
                            onRegisterCompany = { name, owner, mobile, email, pwd, addr, loc ->
                                viewModel.registerCompany(name, owner, mobile, email, pwd, addr, loc)
                            },
                            onRegisterWorker = { name, mobile, email, pwd, loc, trade, profile ->
                                viewModel.registerWorker(name, mobile, email, pwd, loc, trade, profile)
                            },
                            onLogin = { role, id, pwd ->
                                viewModel.login(role, id, pwd)
                            },
                            onBackClick = { viewModel.popBack() }
                        )
                    }

                    is Screen.CompanySalaryManager -> {
                        val approvedWorkers = allApplications.filter { it.status == "Accepted" }
                        CompanySalaryScreen(
                            company = currentCompany,
                            approvedWorkers = approvedWorkers,
                            attendanceList = companyAttendance,
                            salaryRecords = companySalaryRecords,
                            payments = companyPayments,
                            isHindi = isHindi,
                            onSaveSalaryDate = { date -> viewModel.updateCompanySalaryDate(date) },
                            onMarkAttendance = { wId, wName, jId, dt, st, otH, otR, nt ->
                                viewModel.markWorkerAttendance(wId, wName, jId, dt, st, otH, otR, nt)
                            },
                            onRecordLeave = { wId, wName, jId, dt, lt, rsn ->
                                viewModel.recordWorkerLeave(wId, wName, jId, dt, lt, rsn)
                            },
                            onCalculateSalary = { wId, wName, jId, jTitle, cStart, cEnd, cLabel, twd, bSal, ded, adv ->
                                viewModel.calculateSalaryForWorker(wId, wName, jId, jTitle, cStart, cEnd, cLabel, twd, bSal, ded, adv)
                            },
                            onMarkSalaryPaid = { rec, ref ->
                                viewModel.markSalaryAsPaid(rec, ref)
                            },
                            onBackClick = { viewModel.popBack() }
                        )
                    }

                    is Screen.WorkerSalaryDashboard, is Screen.SalaryHistory -> {
                        WorkerSalaryScreen(
                            worker = currentWorker,
                            attendances = workerAttendance,
                            leaves = workerLeaves,
                            salaryRecords = workerSalaryRecords,
                            payments = workerPayments,
                            isHindi = isHindi,
                            onBackClick = { viewModel.popBack() }
                        )
                    }

                    is Screen.JobDetails -> {
                        val job = allJobs.find { it.id == targetScreen.jobId }
                        val app = allApplications.find { it.jobId == targetScreen.jobId }
                        JobDetailsScreen(
                            job = job,
                            application = app,
                            isHindi = isHindi,
                            onApply = { jobToApply -> viewModel.applyForJob(jobToApply) },
                            onOpenChat = { appliedApp ->
                                viewModel.navigateTo(
                                    Screen.Chat(
                                        chatId = "chat_${appliedApp.jobId}_${appliedApp.id}",
                                        jobId = appliedApp.jobId,
                                        applicationId = appliedApp.id,
                                        otherPartyName = appliedApp.companyName,
                                        otherPartyPhone = appliedApp.phone,
                                        otherPartyRole = "COMPANY",
                                        currentRole = "WORKER",
                                        jobTitle = appliedApp.jobTitle
                                    )
                                )
                            },
                            onMarkArrived = { id, title -> viewModel.markArrived(id, title) },
                            onBackClick = { viewModel.popBack() }
                        )
                    }

                    is Screen.CompanyDashboard -> {
                        CompanyDashboardScreen(
                            jobs = allJobs,
                            applications = allApplications,
                            unreadCounts = companyUnreadCounts,
                            isHindi = isHindi,
                            onPostJobClick = { viewModel.navigateTo(Screen.PostJob) },
                            onOpenSalaryManager = { viewModel.navigateTo(Screen.CompanySalaryManager) },
                            onViewApplicants = { jobId, title ->
                                viewModel.navigateTo(Screen.JobApplicants(jobId, title))
                            },
                            onAcceptApplicant = { appId -> viewModel.acceptApplication(appId) },
                            onRejectApplicant = { appId -> viewModel.rejectApplication(appId) },
                            onOpenChat = { applicant ->
                                viewModel.navigateTo(
                                    Screen.Chat(
                                        chatId = "chat_${applicant.jobId}_${applicant.id}",
                                        jobId = applicant.jobId,
                                        applicationId = applicant.id,
                                        otherPartyName = applicant.applicantName,
                                        otherPartyPhone = applicant.applicantPhone,
                                        otherPartyRole = "WORKER",
                                        currentRole = "COMPANY",
                                        jobTitle = applicant.jobTitle
                                    )
                                )
                            },
                            onDeleteJob = { jobId -> viewModel.deleteJob(jobId) },
                            onBackClick = { viewModel.popBack() }
                        )
                    }

                    is Screen.PostJob -> {
                        PostJobScreen(
                            isHindi = isHindi,
                            onJobSubmit = { company, title, category, salary, workers, timing, location, distance, phone, description, isUrgent ->
                                viewModel.postJob(
                                    company = company,
                                    title = title,
                                    category = category,
                                    salary = salary,
                                    workers = workers,
                                    timing = timing,
                                    location = location,
                                    distance = distance,
                                    phone = phone,
                                    description = description,
                                    isUrgent = isUrgent
                                )
                            },
                            onBackClick = { viewModel.popBack() }
                        )
                    }

                    is Screen.MyApplications -> {
                        MyApplicationsScreen(
                            applications = allApplications,
                            unreadCounts = workerUnreadCounts,
                            isHindi = isHindi,
                            onOpenChat = { app ->
                                viewModel.navigateTo(
                                    Screen.Chat(
                                        chatId = "chat_${app.jobId}_${app.id}",
                                        jobId = app.jobId,
                                        applicationId = app.id,
                                        otherPartyName = app.companyName,
                                        otherPartyPhone = app.phone,
                                        otherPartyRole = "COMPANY",
                                        currentRole = "WORKER",
                                        jobTitle = app.jobTitle
                                    )
                                )
                            },
                            onMarkArrived = { id, title -> viewModel.markArrived(id, title) },
                            onBackClick = { viewModel.popBack() },
                            onExploreJobsClick = { viewModel.navigateTo(Screen.WorkerJobs) }
                        )
                    }

                    is Screen.JobApplicants -> {
                        val jobApplicants = allApplications.filter { it.jobId == targetScreen.jobId }
                        JobApplicantsScreen(
                            jobTitle = targetScreen.jobTitle,
                            applicants = jobApplicants,
                            unreadCounts = companyUnreadCounts,
                            isHindi = isHindi,
                            onAcceptApplicant = { appId -> viewModel.acceptApplication(appId) },
                            onRejectApplicant = { appId -> viewModel.rejectApplication(appId) },
                            onOpenChat = { applicant ->
                                viewModel.navigateTo(
                                    Screen.Chat(
                                        chatId = "chat_${applicant.jobId}_${applicant.id}",
                                        jobId = applicant.jobId,
                                        applicationId = applicant.id,
                                        otherPartyName = applicant.applicantName,
                                        otherPartyPhone = applicant.applicantPhone,
                                        otherPartyRole = "WORKER",
                                        currentRole = "COMPANY",
                                        jobTitle = targetScreen.jobTitle
                                    )
                                )
                            },
                            onBackClick = { viewModel.popBack() }
                        )
                    }

                    is Screen.Chat -> {
                        val chatMessages by viewModel.getChatMessages(targetScreen.chatId)
                            .collectAsStateWithLifecycle(initialValue = emptyList())

                        // Mark as read when entering or receiving in current chat
                        LaunchedEffect(targetScreen.chatId, chatMessages.size) {
                            viewModel.markChatRead(targetScreen.chatId, targetScreen.currentRole)
                        }

                        ChatScreen(
                            chatId = targetScreen.chatId,
                            jobId = targetScreen.jobId,
                            applicationId = targetScreen.applicationId,
                            otherPartyName = targetScreen.otherPartyName,
                            otherPartyPhone = targetScreen.otherPartyPhone,
                            otherPartyRole = targetScreen.otherPartyRole,
                            currentRole = targetScreen.currentRole,
                            jobTitle = targetScreen.jobTitle,
                            messages = chatMessages,
                            isHindi = isHindi,
                            onSendMessage = { text ->
                                val myName = if (targetScreen.currentRole == "COMPANY") {
                                    val j = allJobs.find { it.id == targetScreen.jobId }
                                    j?.company ?: "Company"
                                } else {
                                    workerProfile.name
                                }
                                viewModel.sendChatMessage(
                                    chatId = targetScreen.chatId,
                                    jobId = targetScreen.jobId,
                                    applicationId = targetScreen.applicationId,
                                    senderRole = targetScreen.currentRole,
                                    senderName = myName,
                                    text = text
                                )
                            },
                            onBackClick = { viewModel.popBack() }
                        )
                    }
                }
            }
        }
    }
}

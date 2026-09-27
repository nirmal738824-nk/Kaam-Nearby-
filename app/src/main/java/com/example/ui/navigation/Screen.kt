package com.example.ui.navigation

sealed class Screen {
    object Home : Screen()
    object WorkerJobs : Screen()
    data class JobDetails(val jobId: String) : Screen()
    object CompanyDashboard : Screen()
    object PostJob : Screen()
    object MyApplications : Screen()
    data class JobApplicants(val jobId: String, val jobTitle: String) : Screen()
    object Auth : Screen()
    object CompanySalaryManager : Screen()
    object WorkerSalaryDashboard : Screen()
    object SalaryHistory : Screen()
    data class Chat(
        val chatId: String,
        val jobId: String,
        val applicationId: String,
        val otherPartyName: String,
        val otherPartyPhone: String,
        val otherPartyRole: String, // "WORKER" or "COMPANY"
        val currentRole: String,    // "COMPANY" or "WORKER"
        val jobTitle: String
    ) : Screen()
}

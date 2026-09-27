package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.JobEntity
import com.example.data.repository.JobRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KaamNearbyFlowTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: JobRepository
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = JobRepository(context, db.jobDao())
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testCompleteFlow_CompanyPostsJob_WorkerSeesJob_WorkerApplies_AppearsInCompanyPanel_AcceptAndReject() = runBlocking {
        // Step 1: Company posts job
        val postedJob = JobEntity(
            id = "job-test-101",
            company = "ABC Logistics",
            title = "Helper & Loader",
            category = "Helper",
            salary = "₹16,000/month",
            salaryAmount = 16000,
            workers = 5,
            timing = "9 AM - 6 PM",
            location = "Noida Sector 62",
            distance = 2.0,
            phone = "9876543210",
            description = "Warehouse packing and loading work",
            isVerified = true,
            isUrgent = true,
            applicantsCount = 0
        )
        repository.insertJob(postedJob)

        // Step 2: Worker sees job in the feed
        val jobsForWorker = repository.allJobs.first()
        assertEquals(1, jobsForWorker.size)
        val visibleJob = jobsForWorker.first()
        assertEquals("job-test-101", visibleJob.id)
        assertEquals("Helper & Loader", visibleJob.title)
        assertEquals("ABC Logistics", visibleJob.company)

        // Step 3: Worker applies for the job
        val application = repository.applyToJob(
            job = visibleJob,
            applicantName = "सुनील शर्मा",
            applicantPhone = "9876500001"
        )
        assertNotNull(application)
        assertEquals("job-test-101", application!!.jobId)
        assertEquals("सुनील शर्मा", application.applicantName)
        assertEquals("9876500001", application.applicantPhone)
        assertEquals("Pending", application.status)

        // Step 4: Application appears in company panel
        val applicationsInCompanyPanel = repository.allApplications.first()
        assertEquals(1, applicationsInCompanyPanel.size)
        val panelApp = applicationsInCompanyPanel.first()
        assertEquals("सुनील शर्मा", panelApp.applicantName)
        assertEquals("9876500001", panelApp.applicantPhone)
        assertEquals("Helper & Loader", panelApp.jobTitle)
        assertEquals("Pending", panelApp.status)

        // Step 5: Company accepts the applicant
        repository.updateApplicationStatus(panelApp.id, "Accepted")
        val acceptedApp = db.jobDao().getApplicationById(panelApp.id)
        assertNotNull(acceptedApp)
        assertEquals("Accepted", acceptedApp!!.status)

        // Step 6: Company rejects the applicant
        repository.updateApplicationStatus(panelApp.id, "Rejected")
        val rejectedApp = db.jobDao().getApplicationById(panelApp.id)
        assertNotNull(rejectedApp)
        assertEquals("Rejected", rejectedApp!!.status)
    }

    @Test
    fun testCompleteSalaryAttendanceOvertimeAndPaymentFlow() = runBlocking {
        val salaryRepo = com.example.data.repository.SalaryRepository(context, db.salaryDao())

        val companyId = "COMP-TEST-01"
        val workerId = "WRK-TEST-01"

        // 1. Company selects / approves worker
        val company = com.example.data.model.CompanyEntity(
            companyId = companyId,
            companyName = "Apex Logistics",
            salaryPaymentDate = 10
        )
        salaryRepo.saveCompany(company)

        val worker = com.example.data.model.WorkerEntity(
            workerId = workerId,
            name = "राम कुमार",
            mobile = "9876543210"
        )
        salaryRepo.saveWorker(worker)

        // 2. Company sets salary date to 10th
        salaryRepo.updateCompanySalaryDate(
            companyId = companyId,
            date = 10,
            cycleLabel = "11 Aug – 10 Sep",
            startDate = "2026-08-11",
            endDate = "2026-09-10"
        )

        // 3. Company marks worker attendance & overtime (OT)
        // Day 1: Present with 2 hours OT at ₹100/hr = ₹200
        val attDay1 = com.example.data.model.AttendanceEntity(
            id = "att_${companyId}_${workerId}_2026-08-12",
            workerId = workerId,
            workerName = "राम कुमार",
            companyId = companyId,
            jobId = "job-101",
            date = "2026-08-12",
            status = "Present",
            otHours = 2.0,
            otRate = 100.0,
            otAmount = 200.0,
            markedBy = companyId
        )
        salaryRepo.markAttendance(attDay1)

        // Day 2: Half Day with 1 hour OT at ₹100/hr = ₹100
        val attDay2 = com.example.data.model.AttendanceEntity(
            id = "att_${companyId}_${workerId}_2026-08-13",
            workerId = workerId,
            workerName = "राम कुमार",
            companyId = companyId,
            jobId = "job-101",
            date = "2026-08-13",
            status = "Half Day",
            otHours = 1.0,
            otRate = 100.0,
            otAmount = 100.0,
            markedBy = companyId
        )
        salaryRepo.markAttendance(attDay2)

        // Day 3: Approved Leave
        val leaveDay3 = com.example.data.model.LeaveEntity(
            id = "leave_${companyId}_${workerId}_2026-08-14",
            workerId = workerId,
            workerName = "राम कुमार",
            companyId = companyId,
            date = "2026-08-14",
            leaveType = "Approved Leave",
            approvedBy = companyId
        )
        salaryRepo.recordLeave(leaveDay3)

        // Verify Attendance & OT recorded
        val workerAttendances = salaryRepo.getAttendanceForWorkerFlow(workerId).first()
        assertEquals(2, workerAttendances.size)
        val day1Record = workerAttendances.find { it.date == "2026-08-12" }
        assertNotNull(day1Record)
        assertEquals("Present", day1Record!!.status)
        assertEquals(2.0, day1Record.otHours, 0.01)
        assertEquals(200.0, day1Record.otAmount, 0.01)

        val day2Record = workerAttendances.find { it.date == "2026-08-13" }
        assertNotNull(day2Record)
        assertEquals("Half Day", day2Record!!.status)
        assertEquals(100.0, day2Record.otAmount, 0.01)

        // 4. Salary calculation at end of cycle
        val salaryRecord = salaryRepo.calculateAndSaveSalary(
            companyId = companyId,
            companyName = "Apex Logistics",
            workerId = workerId,
            workerName = "राम कुमार",
            jobId = "job-101",
            jobTitle = "Helper",
            cycleStartDate = "2026-08-11",
            cycleEndDate = "2026-09-10",
            cycleLabel = "11 Aug – 10 Sep",
            totalWorkingDays = 26,
            basicSalary = 15600.0, // ₹600/day
            deductions = 0.0,
            advance = 0.0
        )

        assertNotNull(salaryRecord)
        assertEquals("Calculated", salaryRecord.status)
        assertEquals(1.0, salaryRecord.presentDays, 0.01)
        assertEquals(1, salaryRecord.halfDays)
        assertEquals(1, salaryRecord.approvedLeaveDays)
        assertEquals(3.0, salaryRecord.otHours, 0.01)
        assertEquals(300.0, salaryRecord.otAmount, 0.01)

        // Payable days = 1 (present) + 0.5 (half) + 1 (approved leave) = 2.5 days
        // Base earned = (15600 / 26) * 2.5 = 600 * 2.5 = 1500
        // Final salary = 1500 + 300 (OT) = 1800
        assertEquals(1800.0, salaryRecord.finalSalary, 0.01)

        // 5. Company pays salary & marks as paid
        val payment = salaryRepo.markSalaryPaid(
            salaryRecord = salaryRecord,
            paymentReference = "UTR-9876543210",
            amount = salaryRecord.finalSalary,
            otAmount = salaryRecord.otAmount,
            paymentDate = "2026-09-10"
        )

        assertNotNull(payment)
        assertEquals("Paid", payment.status)
        assertEquals(1800.0, payment.amount, 0.01)
        assertEquals(300.0, payment.otAmount, 0.01)
        assertEquals("UTR-9876543210", payment.paymentReference)

        // 6. Worker verifies their payment history & "Paid" status
        val workerPayments = salaryRepo.getPaymentsForWorkerFlow(workerId).first()
        assertEquals(1, workerPayments.size)
        val workerPayment = workerPayments.first()
        assertEquals("Paid", workerPayment.status)
        assertEquals(1800.0, workerPayment.amount, 0.01)
        assertEquals("UTR-9876543210", workerPayment.paymentReference)
        assertEquals("11 Aug – 10 Sep", workerPayment.salaryCycle)
    }
}

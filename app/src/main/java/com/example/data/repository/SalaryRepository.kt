package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.dao.SalaryDao
import com.example.data.model.AttendanceEntity
import com.example.data.model.CompanyEntity
import com.example.data.model.LeaveEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.SalaryCycleEntity
import com.example.data.model.SalaryRecordEntity
import com.example.data.model.WorkerEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class SalaryRepository(
    private val context: Context,
    private val salaryDao: SalaryDao
) {
    private val tag = "SalaryRepository"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:771224509431:android:b41e8e9044ff")
                    .setProjectId("ais-asia-southeast1-9c9517addd")
                    .setApiKey("AIzaSyDWnBW3-rm_J7K3rKirAZmSceF9uuJEhV4")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "Firestore initialization error: ${e.message}", e)
            null
        }
    }

    private var attendanceListener: ListenerRegistration? = null
    private var leavesListener: ListenerRegistration? = null
    private var salaryRecordsListener: ListenerRegistration? = null
    private var paymentsListener: ListenerRegistration? = null
    private var companiesListener: ListenerRegistration? = null
    private var workersListener: ListenerRegistration? = null
    private var cyclesListener: ListenerRegistration? = null

    fun startRealtimeSync(scope: CoroutineScope) {
        val db = firestore ?: return

        if (attendanceListener == null) {
            attendanceListener = db.collection("attendance")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Listen failed for attendance", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            val items = snapshot.documents.mapNotNull { parseAttendance(it) }
                            salaryDao.insertAttendanceList(items)
                        }
                    }
                }
        }

        if (leavesListener == null) {
            leavesListener = db.collection("leaves")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Listen failed for leaves", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            val items = snapshot.documents.mapNotNull { parseLeave(it) }
                            salaryDao.insertLeaves(items)
                        }
                    }
                }
        }

        if (salaryRecordsListener == null) {
            salaryRecordsListener = db.collection("salaryRecords")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Listen failed for salaryRecords", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            val items = snapshot.documents.mapNotNull { parseSalaryRecord(it) }
                            salaryDao.insertSalaryRecords(items)
                        }
                    }
                }
        }

        if (paymentsListener == null) {
            paymentsListener = db.collection("payments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Listen failed for payments", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            val items = snapshot.documents.mapNotNull { parsePayment(it) }
                            salaryDao.insertPayments(items)
                        }
                    }
                }
        }

        if (companiesListener == null) {
            companiesListener = db.collection("companies")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            snapshot.documents.mapNotNull { parseCompany(it) }.forEach {
                                salaryDao.insertCompany(it)
                            }
                        }
                    }
                }
        }

        if (workersListener == null) {
            workersListener = db.collection("workers")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            snapshot.documents.mapNotNull { parseWorker(it) }.forEach {
                                salaryDao.insertWorker(it)
                            }
                        }
                    }
                }
        }

        if (cyclesListener == null) {
            cyclesListener = db.collection("salaryCycles")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            snapshot.documents.mapNotNull { parseCycle(it) }.forEach {
                                salaryDao.insertSalaryCycle(it)
                            }
                        }
                    }
                }
        }
    }

    fun stopRealtimeSync() {
        attendanceListener?.remove()
        leavesListener?.remove()
        salaryRecordsListener?.remove()
        paymentsListener?.remove()
        companiesListener?.remove()
        workersListener?.remove()
        cyclesListener?.remove()
    }

    // Company & Worker Profile Persistence
    suspend fun saveCompany(company: CompanyEntity) = withContext(Dispatchers.IO) {
        salaryDao.insertCompany(company)
        try {
            firestore?.collection("companies")?.document(company.companyId)
                ?.set(company, SetOptions.merge())
        } catch (e: Exception) {
            Log.e(tag, "Error saving company to Firestore", e)
        }
    }

    suspend fun saveWorker(worker: WorkerEntity) = withContext(Dispatchers.IO) {
        salaryDao.insertWorker(worker)
        try {
            firestore?.collection("workers")?.document(worker.workerId)
                ?.set(worker, SetOptions.merge())
        } catch (e: Exception) {
            Log.e(tag, "Error saving worker to Firestore", e)
        }
    }

    suspend fun updateCompanySalaryDate(companyId: String, date: Int, cycleLabel: String, startDate: String, endDate: String) = withContext(Dispatchers.IO) {
        try {
            firestore?.collection("companies")?.document(companyId)?.update("salaryPaymentDate", date)
            val cycleEntity = SalaryCycleEntity(
                id = "cycle_${companyId}_$date",
                companyId = companyId,
                salaryDate = date,
                cycleStartDate = startDate,
                cycleEndDate = endDate,
                cycleLabel = cycleLabel,
                createdAt = System.currentTimeMillis()
            )
            salaryDao.insertSalaryCycle(cycleEntity)
            firestore?.collection("salaryCycles")?.document(cycleEntity.id)
                ?.set(cycleEntity, SetOptions.merge())
        } catch (e: Exception) {
            Log.e(tag, "Error updating salary date in Firestore", e)
        }
    }

    // Attendance Management
    suspend fun markAttendance(attendance: AttendanceEntity) = withContext(Dispatchers.IO) {
        salaryDao.insertAttendance(attendance)
        try {
            firestore?.collection("attendance")?.document(attendance.id)
                ?.set(attendance, SetOptions.merge())
        } catch (e: Exception) {
            Log.e(tag, "Error marking attendance in Firestore", e)
        }
    }

    // Leave Management
    suspend fun recordLeave(leave: LeaveEntity) = withContext(Dispatchers.IO) {
        salaryDao.insertLeave(leave)
        try {
            firestore?.collection("leaves")?.document(leave.id)
                ?.set(leave, SetOptions.merge())
        } catch (e: Exception) {
            Log.e(tag, "Error recording leave in Firestore", e)
        }
    }

    // Salary Calculation & Persistence
    suspend fun calculateAndSaveSalary(
        companyId: String,
        companyName: String,
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
    ): SalaryRecordEntity = withContext(Dispatchers.IO) {
        // Query attendance and leaves in this cycle
        val attendances = salaryDao.getAttendanceInCycle(workerId, cycleStartDate, cycleEndDate)
        val leaves = salaryDao.getLeavesInCycle(workerId, cycleStartDate, cycleEndDate)

        val presentDays = attendances.count { it.status == "Present" }.toDouble()
        val halfDays = attendances.count { it.status == "Half Day" }
        val absentDays = attendances.count { it.status == "Absent" } + leaves.count { it.leaveType == "Absent" }
        val approvedLeaveDays = leaves.count { it.leaveType == "Approved Leave" || it.leaveType == "Weekly Off" }

        val totalOtHours = attendances.sumOf { it.otHours }
        val totalOtAmount = attendances.sumOf { it.otAmount }

        val payableDays = presentDays + (halfDays * 0.5) + approvedLeaveDays
        val dailyRate = if (totalWorkingDays > 0) basicSalary / totalWorkingDays else 0.0
        val baseEarned = dailyRate * payableDays
        val finalSalary = maxOf(0.0, baseEarned + totalOtAmount - deductions - advance)

        val recordId = "sal_${companyId}_${workerId}_${cycleStartDate}_$cycleEndDate"
        val record = SalaryRecordEntity(
            id = recordId,
            workerId = workerId,
            workerName = workerName,
            companyId = companyId,
            companyName = companyName,
            jobId = jobId,
            jobTitle = jobTitle,
            cycleStartDate = cycleStartDate,
            cycleEndDate = cycleEndDate,
            cycleLabel = cycleLabel,
            totalWorkingDays = totalWorkingDays,
            presentDays = presentDays,
            halfDays = halfDays,
            absentDays = absentDays,
            approvedLeaveDays = approvedLeaveDays,
            otHours = totalOtHours,
            otAmount = totalOtAmount,
            basicSalary = basicSalary,
            deductions = deductions,
            advance = advance,
            finalSalary = finalSalary,
            status = "Calculated",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        salaryDao.insertSalaryRecord(record)
        try {
            firestore?.collection("salaryRecords")?.document(record.id)
                ?.set(record, SetOptions.merge())
        } catch (e: Exception) {
            Log.e(tag, "Error saving salary record to Firestore", e)
        }
        return@withContext record
    }

    // Mark Salary as Paid & Record Payment
    suspend fun markSalaryPaid(
        salaryRecord: SalaryRecordEntity,
        paymentReference: String,
        amount: Double,
        otAmount: Double,
        paymentDate: String
    ): PaymentEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        // 1. Update salaryRecord status to "Paid"
        salaryDao.updateSalaryRecordStatus(salaryRecord.id, "Paid", now)
        try {
            firestore?.collection("salaryRecords")?.document(salaryRecord.id)
                ?.update(mapOf("status" to "Paid", "updatedAt" to now))
        } catch (e: Exception) {
            Log.e(tag, "Error updating salary record in Firestore", e)
        }

        // 2. Insert immutable payment record
        val paymentId = "pay_${salaryRecord.id}_$now"
        val payment = PaymentEntity(
            paymentId = paymentId,
            workerId = salaryRecord.workerId,
            workerName = salaryRecord.workerName,
            companyId = salaryRecord.companyId,
            companyName = salaryRecord.companyName,
            salaryRecordId = salaryRecord.id,
            amount = amount,
            otAmount = otAmount,
            paymentDate = paymentDate,
            paymentReference = paymentReference,
            salaryCycle = salaryRecord.cycleLabel,
            status = "Paid",
            createdAt = now
        )

        salaryDao.insertPayment(payment)
        try {
            firestore?.collection("payments")?.document(payment.paymentId)
                ?.set(payment, SetOptions.merge())
        } catch (e: Exception) {
            Log.e(tag, "Error saving payment to Firestore", e)
        }
        return@withContext payment
    }

    // Flows for Worker & Company
    fun getAttendanceForWorkerFlow(workerId: String): Flow<List<AttendanceEntity>> =
        salaryDao.getAttendanceForWorkerFlow(workerId)

    fun getAttendanceForCompanyFlow(companyId: String): Flow<List<AttendanceEntity>> =
        salaryDao.getAttendanceForCompanyFlow(companyId)

    fun getLeavesForWorkerFlow(workerId: String): Flow<List<LeaveEntity>> =
        salaryDao.getLeavesForWorkerFlow(workerId)

    fun getLeavesForCompanyFlow(companyId: String): Flow<List<LeaveEntity>> =
        salaryDao.getLeavesForCompanyFlow(companyId)

    fun getSalaryRecordsForWorkerFlow(workerId: String): Flow<List<SalaryRecordEntity>> =
        salaryDao.getSalaryRecordsForWorkerFlow(workerId)

    fun getSalaryRecordsForCompanyFlow(companyId: String): Flow<List<SalaryRecordEntity>> =
        salaryDao.getSalaryRecordsForCompanyFlow(companyId)

    fun getPaymentsForWorkerFlow(workerId: String): Flow<List<PaymentEntity>> =
        salaryDao.getPaymentsForWorkerFlow(workerId)

    fun getPaymentsForCompanyFlow(companyId: String): Flow<List<PaymentEntity>> =
        salaryDao.getPaymentsForCompanyFlow(companyId)

    // Document Parsers
    private fun parseAttendance(doc: DocumentSnapshot): AttendanceEntity? {
        return try {
            val id = doc.getString("id") ?: doc.id
            val workerId = doc.getString("workerId") ?: return null
            val workerName = doc.getString("workerName") ?: ""
            val companyId = doc.getString("companyId") ?: return null
            val jobId = doc.getString("jobId") ?: ""
            val date = doc.getString("date") ?: ""
            val status = doc.getString("status") ?: "Present"
            val otHours = doc.getDouble("otHours") ?: 0.0
            val otRate = doc.getDouble("otRate") ?: 0.0
            val otAmount = doc.getDouble("otAmount") ?: 0.0
            val note = doc.getString("note") ?: ""
            val markedBy = doc.getString("markedBy") ?: ""
            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

            AttendanceEntity(
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
                markedBy = markedBy,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse attendance doc ${doc.id}", e)
            null
        }
    }

    private fun parseLeave(doc: DocumentSnapshot): LeaveEntity? {
        return try {
            val id = doc.getString("id") ?: doc.id
            val workerId = doc.getString("workerId") ?: return null
            val workerName = doc.getString("workerName") ?: ""
            val companyId = doc.getString("companyId") ?: return null
            val jobId = doc.getString("jobId") ?: ""
            val date = doc.getString("date") ?: ""
            val leaveType = doc.getString("leaveType") ?: "Weekly Off"
            val reason = doc.getString("reason") ?: ""
            val approvedBy = doc.getString("approvedBy") ?: ""
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

            LeaveEntity(
                id = id,
                workerId = workerId,
                workerName = workerName,
                companyId = companyId,
                jobId = jobId,
                date = date,
                leaveType = leaveType,
                reason = reason,
                approvedBy = approvedBy,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse leave doc ${doc.id}", e)
            null
        }
    }

    private fun parseSalaryRecord(doc: DocumentSnapshot): SalaryRecordEntity? {
        return try {
            val id = doc.getString("id") ?: doc.id
            val workerId = doc.getString("workerId") ?: return null
            val workerName = doc.getString("workerName") ?: ""
            val companyId = doc.getString("companyId") ?: return null
            val companyName = doc.getString("companyName") ?: ""
            val jobId = doc.getString("jobId") ?: ""
            val jobTitle = doc.getString("jobTitle") ?: ""
            val cycleStartDate = doc.getString("cycleStartDate") ?: ""
            val cycleEndDate = doc.getString("cycleEndDate") ?: ""
            val cycleLabel = doc.getString("cycleLabel") ?: ""
            val totalWorkingDays = doc.getLong("totalWorkingDays")?.toInt() ?: 26
            val presentDays = doc.getDouble("presentDays") ?: 0.0
            val halfDays = doc.getLong("halfDays")?.toInt() ?: 0
            val absentDays = doc.getLong("absentDays")?.toInt() ?: 0
            val approvedLeaveDays = doc.getLong("approvedLeaveDays")?.toInt() ?: 0
            val otHours = doc.getDouble("otHours") ?: 0.0
            val otAmount = doc.getDouble("otAmount") ?: 0.0
            val basicSalary = doc.getDouble("basicSalary") ?: 15000.0
            val deductions = doc.getDouble("deductions") ?: 0.0
            val advance = doc.getDouble("advance") ?: 0.0
            val finalSalary = doc.getDouble("finalSalary") ?: 15000.0
            val status = doc.getString("status") ?: "Calculated"
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()

            SalaryRecordEntity(
                id = id,
                workerId = workerId,
                workerName = workerName,
                companyId = companyId,
                companyName = companyName,
                jobId = jobId,
                jobTitle = jobTitle,
                cycleStartDate = cycleStartDate,
                cycleEndDate = cycleEndDate,
                cycleLabel = cycleLabel,
                totalWorkingDays = totalWorkingDays,
                presentDays = presentDays,
                halfDays = halfDays,
                absentDays = absentDays,
                approvedLeaveDays = approvedLeaveDays,
                otHours = otHours,
                otAmount = otAmount,
                basicSalary = basicSalary,
                deductions = deductions,
                advance = advance,
                finalSalary = finalSalary,
                status = status,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse salary record doc ${doc.id}", e)
            null
        }
    }

    private fun parsePayment(doc: DocumentSnapshot): PaymentEntity? {
        return try {
            val paymentId = doc.getString("paymentId") ?: doc.id
            val workerId = doc.getString("workerId") ?: return null
            val workerName = doc.getString("workerName") ?: ""
            val companyId = doc.getString("companyId") ?: return null
            val companyName = doc.getString("companyName") ?: ""
            val salaryRecordId = doc.getString("salaryRecordId") ?: ""
            val amount = doc.getDouble("amount") ?: 0.0
            val otAmount = doc.getDouble("otAmount") ?: 0.0
            val paymentDate = doc.getString("paymentDate") ?: ""
            val paymentReference = doc.getString("paymentReference") ?: ""
            val salaryCycle = doc.getString("salaryCycle") ?: ""
            val status = doc.getString("status") ?: "Paid"
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

            PaymentEntity(
                paymentId = paymentId,
                workerId = workerId,
                workerName = workerName,
                companyId = companyId,
                companyName = companyName,
                salaryRecordId = salaryRecordId,
                amount = amount,
                otAmount = otAmount,
                paymentDate = paymentDate,
                paymentReference = paymentReference,
                salaryCycle = salaryCycle,
                status = status,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse payment doc ${doc.id}", e)
            null
        }
    }

    private fun parseCompany(doc: DocumentSnapshot): CompanyEntity? {
        return try {
            val companyId = doc.getString("companyId") ?: doc.id
            val companyName = doc.getString("companyName") ?: ""
            val ownerName = doc.getString("ownerName") ?: ""
            val mobile = doc.getString("mobile") ?: ""
            val email = doc.getString("email") ?: ""
            val address = doc.getString("address") ?: ""
            val location = doc.getString("location") ?: ""
            val salaryPaymentDate = doc.getLong("salaryPaymentDate")?.toInt() ?: 10
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val authUid = doc.getString("authUid") ?: ""

            CompanyEntity(
                companyId = companyId,
                companyName = companyName,
                ownerName = ownerName,
                mobile = mobile,
                email = email,
                address = address,
                location = location,
                salaryPaymentDate = salaryPaymentDate,
                createdAt = createdAt,
                authUid = authUid
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseWorker(doc: DocumentSnapshot): WorkerEntity? {
        return try {
            val workerId = doc.getString("workerId") ?: doc.id
            val name = doc.getString("name") ?: ""
            val mobile = doc.getString("mobile") ?: ""
            val email = doc.getString("email") ?: ""
            val location = doc.getString("location") ?: ""
            val trade = doc.getString("trade") ?: "General Helper"
            val profileDetails = doc.getString("profileDetails") ?: ""
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val authUid = doc.getString("authUid") ?: ""

            WorkerEntity(
                workerId = workerId,
                name = name,
                mobile = mobile,
                email = email,
                location = location,
                trade = trade,
                profileDetails = profileDetails,
                createdAt = createdAt,
                authUid = authUid
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseCycle(doc: DocumentSnapshot): SalaryCycleEntity? {
        return try {
            val id = doc.getString("id") ?: doc.id
            val companyId = doc.getString("companyId") ?: return null
            val salaryDate = doc.getLong("salaryDate")?.toInt() ?: 10
            val cycleStartDate = doc.getString("cycleStartDate") ?: ""
            val cycleEndDate = doc.getString("cycleEndDate") ?: ""
            val cycleLabel = doc.getString("cycleLabel") ?: ""
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

            SalaryCycleEntity(
                id = id,
                companyId = companyId,
                salaryDate = salaryDate,
                cycleStartDate = cycleStartDate,
                cycleEndDate = cycleEndDate,
                cycleLabel = cycleLabel,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            null
        }
    }
}

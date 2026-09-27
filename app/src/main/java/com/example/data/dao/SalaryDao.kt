package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceEntity
import com.example.data.model.CompanyEntity
import com.example.data.model.LeaveEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.SalaryCycleEntity
import com.example.data.model.SalaryRecordEntity
import com.example.data.model.WorkerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryDao {

    // Attendance
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(attendances: List<AttendanceEntity>)

    @Query("SELECT * FROM attendance WHERE workerId = :workerId ORDER BY date DESC")
    fun getAttendanceForWorkerFlow(workerId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE companyId = :companyId ORDER BY date DESC")
    fun getAttendanceForCompanyFlow(companyId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE workerId = :workerId AND date = :date LIMIT 1")
    suspend fun getAttendanceForWorkerAndDate(workerId: String, date: String): AttendanceEntity?

    @Query("SELECT * FROM attendance WHERE workerId = :workerId AND date BETWEEN :startDate AND :endDate")
    suspend fun getAttendanceInCycle(workerId: String, startDate: String, endDate: String): List<AttendanceEntity>

    // Leaves
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: LeaveEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaves(leaves: List<LeaveEntity>)

    @Query("SELECT * FROM leaves WHERE workerId = :workerId ORDER BY date DESC")
    fun getLeavesForWorkerFlow(workerId: String): Flow<List<LeaveEntity>>

    @Query("SELECT * FROM leaves WHERE companyId = :companyId ORDER BY date DESC")
    fun getLeavesForCompanyFlow(companyId: String): Flow<List<LeaveEntity>>

    @Query("SELECT * FROM leaves WHERE workerId = :workerId AND date BETWEEN :startDate AND :endDate")
    suspend fun getLeavesInCycle(workerId: String, startDate: String, endDate: String): List<LeaveEntity>

    // Salary Cycles
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryCycle(cycle: SalaryCycleEntity)

    @Query("SELECT * FROM salary_cycles WHERE companyId = :companyId ORDER BY createdAt DESC")
    fun getSalaryCyclesFlow(companyId: String): Flow<List<SalaryCycleEntity>>

    // Salary Records
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryRecord(record: SalaryRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryRecords(records: List<SalaryRecordEntity>)

    @Query("SELECT * FROM salary_records WHERE workerId = :workerId ORDER BY createdAt DESC")
    fun getSalaryRecordsForWorkerFlow(workerId: String): Flow<List<SalaryRecordEntity>>

    @Query("SELECT * FROM salary_records WHERE companyId = :companyId ORDER BY createdAt DESC")
    fun getSalaryRecordsForCompanyFlow(companyId: String): Flow<List<SalaryRecordEntity>>

    @Query("SELECT * FROM salary_records WHERE id = :id LIMIT 1")
    suspend fun getSalaryRecordById(id: String): SalaryRecordEntity?

    @Query("UPDATE salary_records SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSalaryRecordStatus(id: String, status: String, updatedAt: Long)

    // Payments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Query("SELECT * FROM payments WHERE workerId = :workerId ORDER BY createdAt DESC")
    fun getPaymentsForWorkerFlow(workerId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE companyId = :companyId ORDER BY createdAt DESC")
    fun getPaymentsForCompanyFlow(companyId: String): Flow<List<PaymentEntity>>

    // Companies & Workers cache
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity)

    @Query("SELECT * FROM companies WHERE companyId = :companyId LIMIT 1")
    suspend fun getCompany(companyId: String): CompanyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerEntity)

    @Query("SELECT * FROM workers WHERE workerId = :workerId LIMIT 1")
    suspend fun getWorker(workerId: String): WorkerEntity?
}

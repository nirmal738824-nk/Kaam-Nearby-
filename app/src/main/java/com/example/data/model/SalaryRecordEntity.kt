package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "salary_records")
data class SalaryRecordEntity(
    @PrimaryKey
    val id: String = "sal_" + UUID.randomUUID().toString(),
    val workerId: String,
    val workerName: String = "",
    val companyId: String,
    val companyName: String = "",
    val jobId: String = "",
    val jobTitle: String = "",
    val cycleStartDate: String = "",
    val cycleEndDate: String = "",
    val cycleLabel: String = "",
    val totalWorkingDays: Int = 26,
    val presentDays: Double = 0.0,
    val halfDays: Int = 0,
    val absentDays: Int = 0,
    val approvedLeaveDays: Int = 0,
    val otHours: Double = 0.0,
    val otAmount: Double = 0.0,
    val basicSalary: Double = 15000.0,
    val deductions: Double = 0.0,
    val advance: Double = 0.0,
    val finalSalary: Double = 15000.0,
    val status: String = "Calculated", // "Calculated", "Paid"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

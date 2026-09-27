package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey
    val paymentId: String = "pay_" + UUID.randomUUID().toString(),
    val workerId: String,
    val workerName: String = "",
    val companyId: String,
    val companyName: String = "",
    val salaryRecordId: String = "",
    val amount: Double = 0.0,
    val otAmount: Double = 0.0,
    val paymentDate: String = "",
    val paymentReference: String = "",
    val salaryCycle: String = "",
    val status: String = "Paid",
    val createdAt: Long = System.currentTimeMillis()
)

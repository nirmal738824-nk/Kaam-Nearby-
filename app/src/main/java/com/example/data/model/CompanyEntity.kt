package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "companies")
data class CompanyEntity(
    @PrimaryKey
    val companyId: String = "comp_" + UUID.randomUUID().toString().take(8),
    val companyName: String = "",
    val ownerName: String = "",
    val mobile: String = "",
    val email: String = "",
    val address: String = "",
    val location: String = "",
    val salaryPaymentDate: Int = 10, // Default 10th of each month
    val createdAt: Long = System.currentTimeMillis(),
    val authUid: String = ""
)

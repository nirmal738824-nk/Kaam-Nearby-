package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val companyId: String = "",
    val company: String,
    val title: String,
    val category: String = "Helper",
    val salary: String,
    val salaryAmount: Int = 15000,
    val workers: Int = 1,
    val timing: String,
    val location: String,
    val distance: Double,
    val phone: String,
    val description: String,
    val isVerified: Boolean = true,
    val isUrgent: Boolean = false,
    val applicantsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

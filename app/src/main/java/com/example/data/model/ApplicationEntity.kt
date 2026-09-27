package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val jobId: String,
    val companyId: String = "",
    val workerId: String = "",
    val companyName: String,
    val jobTitle: String,
    val salary: String,
    val location: String,
    val phone: String, // company phone
    val applicantName: String,
    val applicantPhone: String,
    val appliedAt: Long = System.currentTimeMillis(),
    val status: String = "Pending", // "Pending", "Accepted", "Rejected"
    val hasArrived: Boolean = false,
    val arrivedAt: Long? = null
)

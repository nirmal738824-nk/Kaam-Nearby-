package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "leaves")
data class LeaveEntity(
    @PrimaryKey
    val id: String = "leave_" + UUID.randomUUID().toString(),
    val workerId: String,
    val workerName: String = "",
    val companyId: String,
    val jobId: String = "",
    val date: String, // YYYY-MM-DD
    val leaveType: String, // "Weekly Off", "Approved Leave", "Absent"
    val reason: String = "",
    val approvedBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

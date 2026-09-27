package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey
    val id: String, // format: "att_${workerId}_${date}"
    val workerId: String,
    val workerName: String = "",
    val companyId: String,
    val jobId: String = "",
    val date: String, // YYYY-MM-DD
    val status: String = "Present", // "Present", "Absent", "Half Day"
    val otHours: Double = 0.0,
    val otRate: Double = 0.0,
    val otAmount: Double = 0.0,
    val note: String = "",
    val markedBy: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

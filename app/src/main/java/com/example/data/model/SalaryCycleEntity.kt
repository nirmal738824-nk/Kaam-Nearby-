package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "salary_cycles")
data class SalaryCycleEntity(
    @PrimaryKey
    val id: String = "cycle_" + UUID.randomUUID().toString(),
    val companyId: String,
    val salaryDate: Int = 10,
    val cycleStartDate: String = "",
    val cycleEndDate: String = "",
    val cycleLabel: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

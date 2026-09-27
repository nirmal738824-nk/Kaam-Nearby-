package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "workers")
data class WorkerEntity(
    @PrimaryKey
    val workerId: String = "wrk_" + UUID.randomUUID().toString().take(8),
    val name: String = "",
    val mobile: String = "",
    val email: String = "",
    val location: String = "",
    val trade: String = "General Helper",
    val profileDetails: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val authUid: String = ""
)

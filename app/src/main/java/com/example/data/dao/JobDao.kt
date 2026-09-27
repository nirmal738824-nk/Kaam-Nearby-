package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApplicationEntity
import com.example.data.model.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {

    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun getAllJobsFlow(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    fun getJobByIdFlow(id: String): Flow<JobEntity?>

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: String): JobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Update
    suspend fun updateJob(job: JobEntity)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun deleteJobById(id: String)

    @Query("UPDATE jobs SET applicantsCount = applicantsCount + 1 WHERE id = :jobId")
    suspend fun incrementApplicants(jobId: String)

    @Query("SELECT COUNT(*) FROM jobs")
    suspend fun getJobCount(): Int

    @Query("DELETE FROM jobs")
    suspend fun clearAllJobs()

    // Applications
    @Query("SELECT * FROM applications ORDER BY appliedAt DESC")
    fun getAllApplicationsFlow(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE jobId = :jobId LIMIT 1")
    fun getApplicationByJobIdFlow(jobId: String): Flow<ApplicationEntity?>

    @Query("SELECT * FROM applications WHERE jobId = :jobId LIMIT 1")
    suspend fun getApplicationByJobId(jobId: String): ApplicationEntity?

    @Query("SELECT * FROM applications WHERE id = :id LIMIT 1")
    suspend fun getApplicationById(id: String): ApplicationEntity?

    @Query("SELECT * FROM applications WHERE jobId = :jobId ORDER BY appliedAt DESC")
    fun getApplicationsForJobFlow(jobId: String): Flow<List<ApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: ApplicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplications(applications: List<ApplicationEntity>)

    @Query("UPDATE applications SET status = :status WHERE id = :applicationId")
    suspend fun updateApplicationStatus(applicationId: String, status: String)

    @Query("UPDATE applications SET hasArrived = 1, arrivedAt = :arrivedTime WHERE jobId = :jobId")
    suspend fun markArrived(jobId: String, arrivedTime: Long)

    @Query("DELETE FROM applications")
    suspend fun clearAllApplications()
}

package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.dao.JobDao
import com.example.data.model.ApplicationEntity
import com.example.data.model.JobEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class JobRepository(
    private val context: Context,
    private val jobDao: JobDao
) {
    private val tag = "JobRepository"

    val allJobs: Flow<List<JobEntity>> = jobDao.getAllJobsFlow()
    val allApplications: Flow<List<ApplicationEntity>> = jobDao.getAllApplicationsFlow()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:771224509431:android:b41e8e9044ff")
                    .setProjectId("ais-asia-southeast1-9c9517addd")
                    .setApiKey("AIzaSyDWnBW3-rm_J7K3rKirAZmSceF9uuJEhV4")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "Firestore initialization warning: ${e.message}", e)
            null
        }
    }

    private var jobsListener: ListenerRegistration? = null
    private var applicationsListener: ListenerRegistration? = null

    fun startRealtimeSync(scope: CoroutineScope) {
        val db = firestore ?: return

        // 1. Sync 'jobs' collection from Firestore in real-time
        if (jobsListener == null) {
            jobsListener = db.collection("jobs")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Listen failed for jobs", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            val jobs = snapshot.documents.mapNotNull { parseJobDoc(it) }
                            jobDao.insertJobs(jobs)
                        }
                    }
                }
        }

        // 2. Sync 'applications' collection from Firestore in real-time
        if (applicationsListener == null) {
            applicationsListener = db.collection("applications")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Listen failed for applications", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            val apps = snapshot.documents.mapNotNull { parseApplicationDoc(it) }
                            jobDao.insertApplications(apps)
                        }
                    }
                }
        }
    }

    fun stopRealtimeSync() {
        jobsListener?.remove()
        jobsListener = null
        applicationsListener?.remove()
        applicationsListener = null
    }

    fun getJobByIdFlow(id: String): Flow<JobEntity?> = jobDao.getJobByIdFlow(id)

    suspend fun getJobById(id: String): JobEntity? = withContext(Dispatchers.IO) {
        jobDao.getJobById(id)
    }

    suspend fun insertJob(job: JobEntity) = withContext(Dispatchers.IO) {
        // 1. Save locally in Room
        jobDao.insertJob(job)

        // 2. Save directly to Cloud Firestore in "jobs" collection
        try {
            firestore?.let { db ->
                val jobData = hashMapOf(
                    "id" to job.id,
                    "company" to job.company,
                    "title" to job.title,
                    "category" to job.category,
                    "salary" to job.salary,
                    "salaryAmount" to job.salaryAmount,
                    "workers" to job.workers,
                    "timing" to job.timing,
                    "location" to job.location,
                    "distance" to job.distance,
                    "phone" to job.phone,
                    "description" to job.description,
                    "isVerified" to job.isVerified,
                    "isUrgent" to job.isUrgent,
                    "applicantsCount" to job.applicantsCount,
                    "createdAt" to job.createdAt
                )
                db.collection("jobs")
                    .document(job.id)
                    .set(jobData, SetOptions.merge())
                    .addOnFailureListener { e ->
                        Log.e(tag, "Error saving job to Firestore: ${e.message}", e)
                    }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to write job to Firestore", e)
        }
    }

    suspend fun deleteJob(id: String) = withContext(Dispatchers.IO) {
        jobDao.deleteJobById(id)
        try {
            firestore?.collection("jobs")?.document(id)?.delete()
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete job from Firestore", e)
        }
    }

    suspend fun applyToJob(
        job: JobEntity,
        applicantName: String,
        applicantPhone: String
    ): ApplicationEntity? = withContext(Dispatchers.IO) {
        // Prevent duplicate applications
        val existing = jobDao.getApplicationByJobId(job.id)
        if (existing != null) {
            return@withContext null
        }

        val appId = UUID.randomUUID().toString()
        val app = ApplicationEntity(
            id = appId,
            jobId = job.id,
            companyName = job.company,
            jobTitle = job.title,
            salary = job.salary,
            location = job.location,
            phone = job.phone,
            applicantName = applicantName.ifBlank { "Worker" },
            applicantPhone = applicantPhone.ifBlank { "9876543210" },
            appliedAt = System.currentTimeMillis(),
            status = "Pending",
            hasArrived = false,
            arrivedAt = null
        )

        // 1. Save locally in Room
        jobDao.insertApplication(app)
        jobDao.incrementApplicants(job.id)

        // 2. Save directly to Cloud Firestore in "applications" collection
        try {
            firestore?.let { db ->
                val appData = hashMapOf(
                    "id" to app.id,
                    "jobId" to app.jobId,
                    "companyName" to app.companyName,
                    "jobTitle" to app.jobTitle,
                    "salary" to app.salary,
                    "location" to app.location,
                    "phone" to app.phone,
                    "applicantName" to app.applicantName,
                    "applicantPhone" to app.applicantPhone,
                    "appliedAt" to app.appliedAt,
                    "status" to "Pending",
                    "hasArrived" to false,
                    "arrivedAt" to null
                )
                db.collection("applications")
                    .document(app.id)
                    .set(appData, SetOptions.merge())
                    .addOnFailureListener { e ->
                        Log.e(tag, "Error saving application to Firestore: ${e.message}", e)
                    }

                // Increment applicants count in jobs collection
                db.collection("jobs")
                    .document(job.id)
                    .update("applicantsCount", FieldValue.increment(1))
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to write application to Firestore", e)
        }

        return@withContext app
    }

    suspend fun updateApplicationStatus(applicationId: String, status: String) = withContext(Dispatchers.IO) {
        // 1. Update in local Room DB
        jobDao.updateApplicationStatus(applicationId, status)

        // 2. Update in Cloud Firestore "applications" collection
        try {
            firestore?.collection("applications")
                ?.document(applicationId)
                ?.update("status", status)
                ?.addOnFailureListener { e ->
                    Log.e(tag, "Error updating status in Firestore: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to update status in Firestore", e)
        }
    }

    suspend fun markArrived(jobId: String) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        jobDao.markArrived(jobId, now)

        try {
            firestore?.collection("applications")
                ?.whereEqualTo("jobId", jobId)
                ?.get()
                ?.addOnSuccessListener { snapshot ->
                    for (doc in snapshot.documents) {
                        doc.reference.update(
                            mapOf(
                                "hasArrived" to true,
                                "arrivedAt" to now
                            )
                        )
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to mark arrived in Firestore", e)
        }
    }

    fun getApplicationForJobFlow(jobId: String): Flow<ApplicationEntity?> {
        return jobDao.getApplicationByJobIdFlow(jobId)
    }

    fun getApplicationsForJob(jobId: String): Flow<List<ApplicationEntity>> {
        return jobDao.getApplicationsForJobFlow(jobId)
    }

    private fun parseJobDoc(doc: DocumentSnapshot): JobEntity? {
        return try {
            val id = doc.getString("id") ?: doc.id
            val company = doc.getString("company") ?: return null
            val title = doc.getString("title") ?: return null
            val category = doc.getString("category") ?: "Helper"
            val salary = doc.getString("salary") ?: "₹15,000/month"
            val salaryAmount = doc.getLong("salaryAmount")?.toInt() ?: 15000
            val workers = doc.getLong("workers")?.toInt() ?: 1
            val timing = doc.getString("timing") ?: "9 AM - 6 PM"
            val location = doc.getString("location") ?: ""
            val distance = doc.getDouble("distance") ?: 2.0
            val phone = doc.getString("phone") ?: ""
            val description = doc.getString("description") ?: ""
            val isVerified = doc.getBoolean("isVerified") ?: true
            val isUrgent = doc.getBoolean("isUrgent") ?: false
            val applicantsCount = doc.getLong("applicantsCount")?.toInt() ?: 0
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

            JobEntity(
                id = id,
                company = company,
                title = title,
                category = category,
                salary = salary,
                salaryAmount = salaryAmount,
                workers = workers,
                timing = timing,
                location = location,
                distance = distance,
                phone = phone,
                description = description,
                isVerified = isVerified,
                isUrgent = isUrgent,
                applicantsCount = applicantsCount,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse job document ${doc.id}", e)
            null
        }
    }

    private fun parseApplicationDoc(doc: DocumentSnapshot): ApplicationEntity? {
        return try {
            val id = doc.getString("id") ?: doc.id
            val jobId = doc.getString("jobId") ?: return null
            val companyName = doc.getString("companyName") ?: ""
            val jobTitle = doc.getString("jobTitle") ?: ""
            val salary = doc.getString("salary") ?: ""
            val location = doc.getString("location") ?: ""
            val phone = doc.getString("phone") ?: ""
            val applicantName = doc.getString("applicantName") ?: "Worker"
            val applicantPhone = doc.getString("applicantPhone") ?: ""
            val appliedAt = doc.getLong("appliedAt") ?: System.currentTimeMillis()
            val status = doc.getString("status") ?: "Pending"
            val hasArrived = doc.getBoolean("hasArrived") ?: false
            val arrivedAt = doc.getLong("arrivedAt")

            ApplicationEntity(
                id = id,
                jobId = jobId,
                companyName = companyName,
                jobTitle = jobTitle,
                salary = salary,
                location = location,
                phone = phone,
                applicantName = applicantName,
                applicantPhone = applicantPhone,
                appliedAt = appliedAt,
                status = status,
                hasArrived = hasArrived,
                arrivedAt = arrivedAt
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse application document ${doc.id}", e)
            null
        }
    }
}

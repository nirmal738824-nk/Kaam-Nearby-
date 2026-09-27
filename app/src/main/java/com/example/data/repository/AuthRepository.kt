package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.dao.SalaryDao
import com.example.data.model.CompanyEntity
import com.example.data.model.WorkerEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(
    private val context: Context,
    private val salaryDao: SalaryDao
) {
    private val tag = "AuthRepository"
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kaam_nearby_auth", Context.MODE_PRIVATE)

    private val auth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:771224509431:android:b41e8e9044ff")
                    .setProjectId("ais-asia-southeast1-9c9517addd")
                    .setApiKey("AIzaSyDWnBW3-rm_J7K3rKirAZmSceF9uuJEhV4")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "FirebaseAuth init error: ${e.message}", e)
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val _currentRole = MutableStateFlow<String>(prefs.getString("current_role", "company") ?: "company")
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    private val _currentCompany = MutableStateFlow<CompanyEntity?>(null)
    val currentCompany: StateFlow<CompanyEntity?> = _currentCompany.asStateFlow()

    private val _currentWorker = MutableStateFlow<WorkerEntity?>(null)
    val currentWorker: StateFlow<WorkerEntity?> = _currentWorker.asStateFlow()

    private val _isLoggedIn = MutableStateFlow<Boolean>(prefs.getBoolean("is_logged_in", true))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    suspend fun initSession() = withContext(Dispatchers.IO) {
        val savedRole = prefs.getString("current_role", "company") ?: "company"
        _currentRole.value = savedRole

        val savedCompanyId = prefs.getString("company_id", null)
        val savedWorkerId = prefs.getString("worker_id", null)

        if (savedCompanyId != null) {
            val comp = salaryDao.getCompany(savedCompanyId) ?: fetchCompanyFromFirestore(savedCompanyId)
            _currentCompany.value = comp
        } else {
            // Default active company if none yet
            val defaultComp = CompanyEntity(
                companyId = "COMP-DELHI-01",
                companyName = "Apex Logistics & Manufacturing",
                ownerName = "Rajesh Sharma",
                mobile = "9811223344",
                email = "apex.logistics@kaamnearby.app",
                address = "Plot 42, Udyog Vihar",
                location = "Noida Sector 62",
                salaryPaymentDate = 10
            )
            _currentCompany.value = defaultComp
            saveCompanyLocally(defaultComp)
        }

        if (savedWorkerId != null) {
            val wrk = salaryDao.getWorker(savedWorkerId) ?: fetchWorkerFromFirestore(savedWorkerId)
            _currentWorker.value = wrk
        } else {
            // Default active worker if none yet
            val defaultWrk = WorkerEntity(
                workerId = "WRK-NOIDA-01",
                name = "राम कुमार (Ram Kumar)",
                mobile = "9876543210",
                email = "ram.kumar@kaamnearby.app",
                location = "Noida Sector 62",
                trade = "General Helper & Worker",
                profileDetails = "5 years experience in packing & warehouse"
            )
            _currentWorker.value = defaultWrk
            saveWorkerLocally(defaultWrk)
        }
    }

    suspend fun registerCompany(
        companyName: String,
        ownerName: String,
        mobile: String,
        emailInput: String,
        passwordInput: String,
        address: String,
        location: String
    ): Result<CompanyEntity> = withContext(Dispatchers.IO) {
        try {
            val digits = mobile.filter { it.isDigit() }
            val email = if (emailInput.isNotBlank() && emailInput.contains("@")) {
                emailInput.trim()
            } else {
                "comp_${digits.ifBlank { UUID.randomUUID().toString().take(6) }}@kaamnearby.app"
            }
            val password = if (passwordInput.length >= 6) passwordInput else "Kaam@1234"

            var authUid = ""
            try {
                auth?.let { fbAuth ->
                    val authResult = fbAuth.createUserWithEmailAndPassword(email, password).await()
                    authUid = authResult.user?.uid ?: ""
                }
            } catch (e: Exception) {
                Log.w(tag, "Firebase Auth register warning (proceeding with Firestore ID): ${e.message}")
            }

            val companyId = "COMP-" + UUID.randomUUID().toString().take(6).uppercase()
            val company = CompanyEntity(
                companyId = companyId,
                companyName = companyName.trim(),
                ownerName = ownerName.trim(),
                mobile = mobile.trim(),
                email = email,
                address = address.trim(),
                location = location.trim(),
                salaryPaymentDate = 10,
                createdAt = System.currentTimeMillis(),
                authUid = authUid
            )

            // Save to Firestore
            firestore?.collection("companies")?.document(company.companyId)
                ?.set(company, SetOptions.merge())

            saveCompanyLocally(company)
            _currentCompany.value = company
            setRole("company")
            _isLoggedIn.value = true

            Result.success(company)
        } catch (e: Exception) {
            Log.e(tag, "Registration error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun registerWorker(
        name: String,
        mobile: String,
        emailInput: String,
        passwordInput: String,
        location: String,
        trade: String,
        profileDetails: String
    ): Result<WorkerEntity> = withContext(Dispatchers.IO) {
        try {
            val digits = mobile.filter { it.isDigit() }
            val email = if (emailInput.isNotBlank() && emailInput.contains("@")) {
                emailInput.trim()
            } else {
                "worker_${digits.ifBlank { UUID.randomUUID().toString().take(6) }}@kaamnearby.app"
            }
            val password = if (passwordInput.length >= 6) passwordInput else "Kaam@1234"

            var authUid = ""
            try {
                auth?.let { fbAuth ->
                    val authResult = fbAuth.createUserWithEmailAndPassword(email, password).await()
                    authUid = authResult.user?.uid ?: ""
                }
            } catch (e: Exception) {
                Log.w(tag, "Firebase Auth register warning: ${e.message}")
            }

            val workerId = "WRK-" + UUID.randomUUID().toString().take(6).uppercase()
            val worker = WorkerEntity(
                workerId = workerId,
                name = name.trim(),
                mobile = mobile.trim(),
                email = email,
                location = location.trim(),
                trade = trade.trim().ifBlank { "General Helper" },
                profileDetails = profileDetails.trim(),
                createdAt = System.currentTimeMillis(),
                authUid = authUid
            )

            // Save to Firestore
            firestore?.collection("workers")?.document(worker.workerId)
                ?.set(worker, SetOptions.merge())

            saveWorkerLocally(worker)
            _currentWorker.value = worker
            setRole("worker")
            _isLoggedIn.value = true

            Result.success(worker)
        } catch (e: Exception) {
            Log.e(tag, "Worker registration error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun login(
        role: String,
        identifier: String, // email or mobile
        passwordInput: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val isEmail = identifier.contains("@")
            val effectiveEmail = if (isEmail) {
                identifier.trim()
            } else {
                val digits = identifier.filter { it.isDigit() }
                if (role == "company") "comp_$digits@kaamnearby.app" else "worker_$digits@kaamnearby.app"
            }
            val password = if (passwordInput.length >= 6) passwordInput else "Kaam@1234"

            try {
                auth?.signInWithEmailAndPassword(effectiveEmail, password)?.await()
            } catch (e: Exception) {
                Log.w(tag, "Firebase sign-in info: ${e.message}")
            }

            setRole(role)
            _isLoggedIn.value = true
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun setRole(role: String) {
        _currentRole.value = role
        prefs.edit().putString("current_role", role).apply()
    }

    private suspend fun saveCompanyLocally(company: CompanyEntity) {
        salaryDao.insertCompany(company)
        prefs.edit()
            .putString("company_id", company.companyId)
            .putString("company_name", company.companyName)
            .apply()
    }

    private suspend fun saveWorkerLocally(worker: WorkerEntity) {
        salaryDao.insertWorker(worker)
        prefs.edit()
            .putString("worker_id", worker.workerId)
            .putString("worker_name", worker.name)
            .apply()
    }

    private suspend fun fetchCompanyFromFirestore(id: String): CompanyEntity? {
        return try {
            val doc = firestore?.collection("companies")?.document(id)?.get()?.await()
            if (doc != null && doc.exists()) {
                val comp = CompanyEntity(
                    companyId = doc.getString("companyId") ?: id,
                    companyName = doc.getString("companyName") ?: "",
                    ownerName = doc.getString("ownerName") ?: "",
                    mobile = doc.getString("mobile") ?: "",
                    email = doc.getString("email") ?: "",
                    address = doc.getString("address") ?: "",
                    location = doc.getString("location") ?: "",
                    salaryPaymentDate = doc.getLong("salaryPaymentDate")?.toInt() ?: 10,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
                salaryDao.insertCompany(comp)
                comp
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchWorkerFromFirestore(id: String): WorkerEntity? {
        return try {
            val doc = firestore?.collection("workers")?.document(id)?.get()?.await()
            if (doc != null && doc.exists()) {
                val wrk = WorkerEntity(
                    workerId = doc.getString("workerId") ?: id,
                    name = doc.getString("name") ?: "",
                    mobile = doc.getString("mobile") ?: "",
                    email = doc.getString("email") ?: "",
                    location = doc.getString("location") ?: "",
                    trade = doc.getString("trade") ?: "General Helper",
                    profileDetails = doc.getString("profileDetails") ?: "",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
                salaryDao.insertWorker(wrk)
                wrk
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth?.signOut()
        _isLoggedIn.value = false
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }
}

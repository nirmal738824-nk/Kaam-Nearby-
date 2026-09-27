package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ChatDao
import com.example.data.dao.JobDao
import com.example.data.dao.SalaryDao
import com.example.data.model.ApplicationEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CompanyEntity
import com.example.data.model.JobEntity
import com.example.data.model.LeaveEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.SalaryCycleEntity
import com.example.data.model.SalaryRecordEntity
import com.example.data.model.WorkerEntity

@Database(
    entities = [
        JobEntity::class,
        ApplicationEntity::class,
        ChatMessageEntity::class,
        CompanyEntity::class,
        WorkerEntity::class,
        AttendanceEntity::class,
        LeaveEntity::class,
        SalaryCycleEntity::class,
        SalaryRecordEntity::class,
        PaymentEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jobDao(): JobDao
    abstract fun chatDao(): ChatDao
    abstract fun salaryDao(): SalaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kaam_nearby_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

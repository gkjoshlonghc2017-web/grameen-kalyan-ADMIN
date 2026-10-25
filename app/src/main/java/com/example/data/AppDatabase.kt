package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CentreEntity::class,
        DailyReportEntity::class,
        WeeklyReportEntity::class,
        MonthlyTargetEntity::class,
        AuditLogEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun centreDao(): CentreDao
    abstract fun dailyReportDao(): DailyReportDao
    abstract fun weeklyReportDao(): WeeklyReportDao
    abstract fun targetDao(): TargetDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gk_keraniganj_db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

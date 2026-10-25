package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CentreDao {
    @Query("SELECT * FROM centres WHERE active = 1 ORDER BY id ASC")
    fun getAllCentresFlow(): Flow<List<CentreEntity>>

    @Query("SELECT * FROM centres ORDER BY id ASC")
    suspend fun getAllCentres(): List<CentreEntity>

    @Query("SELECT * FROM centres WHERE id = :id LIMIT 1")
    suspend fun getCentreById(id: String): CentreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCentres(centres: List<CentreEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(centre: CentreEntity)
}

@Dao
interface DailyReportDao {
    @Query("SELECT * FROM daily_reports ORDER BY reportDate DESC, createdAt DESC")
    fun getAllDailyReportsFlow(): Flow<List<DailyReportEntity>>

    @Query("SELECT * FROM daily_reports WHERE centreId = :centreId ORDER BY reportDate DESC")
    fun getReportsForCentreFlow(centreId: String): Flow<List<DailyReportEntity>>

    @Query("SELECT * FROM daily_reports WHERE reportDate LIKE :monthPrefix || '%' ORDER BY reportDate ASC")
    fun getReportsForMonthFlow(monthPrefix: String): Flow<List<DailyReportEntity>>

    @Query("SELECT * FROM daily_reports WHERE reportDate LIKE :monthPrefix || '%'")
    suspend fun getReportsForMonth(monthPrefix: String): List<DailyReportEntity>

    @Query("SELECT * FROM daily_reports WHERE centreId = :centreId AND reportDate LIKE :monthPrefix || '%'")
    suspend fun getReportsForCentreAndMonth(centreId: String, monthPrefix: String): List<DailyReportEntity>

    @Query("SELECT * FROM daily_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: String): DailyReportEntity?

    @Query("SELECT * FROM daily_reports WHERE centreId = :centreId AND reportDate = :date LIMIT 1")
    suspend fun findDuplicateReport(centreId: String, date: String): DailyReportEntity?

    @Query("SELECT * FROM daily_reports WHERE status = 'PENDING_VERIFICATION' ORDER BY reportDate DESC")
    fun getPendingReportsFlow(): Flow<List<DailyReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(report: DailyReportEntity)

    @Query("DELETE FROM daily_reports WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface WeeklyReportDao {
    @Query("SELECT * FROM weekly_reports ORDER BY periodEndDate DESC, createdAt DESC")
    fun getAllWeeklyReportsFlow(): Flow<List<WeeklyReportEntity>>

    @Query("SELECT * FROM weekly_reports WHERE centreId = :centreId ORDER BY periodEndDate DESC")
    fun getWeeklyReportsForCentreFlow(centreId: String): Flow<List<WeeklyReportEntity>>

    @Query("SELECT * FROM weekly_reports WHERE status = 'PENDING_VERIFICATION' ORDER BY createdAt DESC")
    fun getPendingWeeklyReportsFlow(): Flow<List<WeeklyReportEntity>>

    @Query("SELECT * FROM weekly_reports WHERE id = :id LIMIT 1")
    suspend fun getWeeklyReportById(id: String): WeeklyReportEntity?

    @Query("SELECT * FROM weekly_reports WHERE centreId = :centreId AND periodStartDate = :start AND periodEndDate = :end LIMIT 1")
    suspend fun findDuplicateWeeklyReport(centreId: String, start: String, end: String): WeeklyReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(report: WeeklyReportEntity)
}

@Dao
interface TargetDao {
    @Query("SELECT * FROM monthly_targets WHERE yearMonth = :yearMonth")
    fun getTargetsForMonthFlow(yearMonth: String): Flow<List<MonthlyTargetEntity>>

    @Query("SELECT * FROM monthly_targets WHERE centreId = :centreId AND yearMonth = :yearMonth LIMIT 1")
    suspend fun getTarget(centreId: String, yearMonth: String): MonthlyTargetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(target: MonthlyTargetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(targets: List<MonthlyTargetEntity>)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 500")
    fun getAllAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: AuditLogEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 'singleton_settings' LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 'singleton_settings' LIMIT 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettingsEntity)
}

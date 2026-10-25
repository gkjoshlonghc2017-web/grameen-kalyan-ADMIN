package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.HealthCentre
import com.example.model.DailyReport
import com.example.model.WeeklyReport
import com.example.model.ReportStatus
import com.example.model.AuditLogEntry

@Entity(tableName = "centres")
data class CentreEntity(
    @PrimaryKey val id: String,
    val nameBangla: String,
    val nameEnglish: String,
    val code: String,
    val assignedUserId: String = "",
    val active: Boolean = true,
    val defaultDailyTarget: Double = 10000.0,
    val defaultMonthlyWorkingDays: Int = 26
) {
    fun toDomain(): HealthCentre = HealthCentre(
        id = id,
        nameBangla = nameBangla,
        nameEnglish = nameEnglish,
        code = code,
        assignedUserId = assignedUserId,
        active = active,
        defaultDailyTarget = defaultDailyTarget,
        defaultMonthlyWorkingDays = defaultMonthlyWorkingDays
    )

    companion object {
        fun fromDomain(c: HealthCentre): CentreEntity = CentreEntity(
            id = c.id,
            nameBangla = c.nameBangla,
            nameEnglish = c.nameEnglish,
            code = c.code,
            assignedUserId = c.assignedUserId,
            active = c.active,
            defaultDailyTarget = c.defaultDailyTarget,
            defaultMonthlyWorkingDays = c.defaultMonthlyWorkingDays
        )
    }
}

@Entity(tableName = "daily_reports")
data class DailyReportEntity(
    @PrimaryKey val id: String,
    val centreId: String,
    val reportDate: String, // YYYY-MM-DD
    val reportingPeriod: String,
    val submitterId: String,
    val submitterName: String,
    val centrePatients: Int,
    val satellitePatients: Int,
    val campPatients: Int,
    val totalDailyIncome: Double,
    val medicineLabCost: Double,
    val satelliteSessions: Int,
    val cardsCount: Int,
    val totalDailyExpense: Double,
    val notes: String = "",
    val status: String = "VALIDATED",
    val warningNote: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val verifiedBy: String? = null
) {
    fun toDomain(): DailyReport = DailyReport(
        id = id,
        centreId = centreId,
        reportDate = reportDate,
        reportingPeriod = reportingPeriod,
        submitterId = submitterId,
        submitterName = submitterName,
        centrePatients = centrePatients,
        satellitePatients = satellitePatients,
        campPatients = campPatients,
        totalDailyIncome = totalDailyIncome,
        medicineLabCost = medicineLabCost,
        satelliteSessions = satelliteSessions,
        cardsCount = cardsCount,
        totalDailyExpense = totalDailyExpense,
        notes = notes,
        status = runCatching { ReportStatus.valueOf(status) }.getOrDefault(ReportStatus.VALIDATED),
        warningNote = warningNote,
        createdAt = createdAt,
        updatedAt = updatedAt,
        verifiedBy = verifiedBy
    )

    companion object {
        fun fromDomain(r: DailyReport): DailyReportEntity = DailyReportEntity(
            id = r.id,
            centreId = r.centreId,
            reportDate = r.reportDate,
            reportingPeriod = r.reportingPeriod,
            submitterId = r.submitterId,
            submitterName = r.submitterName,
            centrePatients = r.centrePatients,
            satellitePatients = r.satellitePatients,
            campPatients = r.campPatients,
            totalDailyIncome = r.totalDailyIncome,
            medicineLabCost = r.medicineLabCost,
            satelliteSessions = r.satelliteSessions,
            cardsCount = r.cardsCount,
            totalDailyExpense = r.totalDailyExpense,
            notes = r.notes,
            status = r.status.name,
            warningNote = r.warningNote,
            createdAt = r.createdAt,
            updatedAt = r.updatedAt,
            verifiedBy = r.verifiedBy
        )
    }
}

@Entity(tableName = "weekly_reports")
data class WeeklyReportEntity(
    @PrimaryKey val id: String,
    val centreId: String,
    val periodStartDate: String,
    val periodEndDate: String,
    val submitterId: String,
    val submitterName: String,
    val centrePatients: Int,
    val satellitePatients: Int,
    val campPatients: Int,
    val campsCount: Int,
    val centrePrescriptionIncome: Double,
    val centreMedicineIncome: Double,
    val centreLabIncome: Double,
    val centreUsgIncome: Double,
    val centreOtherIncome: Double,
    val campPrescriptionIncome: Double,
    val campMedicineSalesIncome: Double,
    val campLabIncome: Double,
    val campUsgIncome: Double,
    val campOtherIncome: Double,
    val reportedWeeklyIncome: Double,
    val totalWeeklyExpense: Double,
    val calculatedCentreIncome: Double,
    val calculatedCampIncome: Double,
    val calculatedWeeklyIncome: Double,
    val discrepancyAmount: Double,
    val status: String,
    val notes: String = "",
    val resolutionReason: String? = null,
    val verifiedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): WeeklyReport = WeeklyReport(
        id = id,
        centreId = centreId,
        periodStartDate = periodStartDate,
        periodEndDate = periodEndDate,
        submitterId = submitterId,
        submitterName = submitterName,
        centrePatients = centrePatients,
        satellitePatients = satellitePatients,
        campPatients = campPatients,
        campsCount = campsCount,
        centrePrescriptionIncome = centrePrescriptionIncome,
        centreMedicineIncome = centreMedicineIncome,
        centreLabIncome = centreLabIncome,
        centreUsgIncome = centreUsgIncome,
        centreOtherIncome = centreOtherIncome,
        campPrescriptionIncome = campPrescriptionIncome,
        campMedicineSalesIncome = campMedicineSalesIncome,
        campLabIncome = campLabIncome,
        campUsgIncome = campUsgIncome,
        campOtherIncome = campOtherIncome,
        reportedWeeklyIncome = reportedWeeklyIncome,
        totalWeeklyExpense = totalWeeklyExpense,
        calculatedCentreIncome = calculatedCentreIncome,
        calculatedCampIncome = calculatedCampIncome,
        calculatedWeeklyIncome = calculatedWeeklyIncome,
        discrepancyAmount = discrepancyAmount,
        status = runCatching { ReportStatus.valueOf(status) }.getOrDefault(ReportStatus.PENDING_VERIFICATION),
        notes = notes,
        resolutionReason = resolutionReason,
        verifiedBy = verifiedBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(w: WeeklyReport): WeeklyReportEntity = WeeklyReportEntity(
            id = w.id,
            centreId = w.centreId,
            periodStartDate = w.periodStartDate,
            periodEndDate = w.periodEndDate,
            submitterId = w.submitterId,
            submitterName = w.submitterName,
            centrePatients = w.centrePatients,
            satellitePatients = w.satellitePatients,
            campPatients = w.campPatients,
            campsCount = w.campsCount,
            centrePrescriptionIncome = w.centrePrescriptionIncome,
            centreMedicineIncome = w.centreMedicineIncome,
            centreLabIncome = w.centreLabIncome,
            centreUsgIncome = w.centreUsgIncome,
            centreOtherIncome = w.centreOtherIncome,
            campPrescriptionIncome = w.campPrescriptionIncome,
            campMedicineSalesIncome = w.campMedicineSalesIncome,
            campLabIncome = w.campLabIncome,
            campUsgIncome = w.campUsgIncome,
            campOtherIncome = w.campOtherIncome,
            reportedWeeklyIncome = w.reportedWeeklyIncome,
            totalWeeklyExpense = w.totalWeeklyExpense,
            calculatedCentreIncome = w.calculatedCentreIncome,
            calculatedCampIncome = w.calculatedCampIncome,
            calculatedWeeklyIncome = w.calculatedWeeklyIncome,
            discrepancyAmount = w.discrepancyAmount,
            status = w.status.name,
            notes = w.notes,
            resolutionReason = w.resolutionReason,
            verifiedBy = w.verifiedBy,
            createdAt = w.createdAt,
            updatedAt = w.updatedAt
        )
    }
}

@Entity(tableName = "monthly_targets")
data class MonthlyTargetEntity(
    @PrimaryKey val id: String, // e.g. "gk-c01_2026-10"
    val centreId: String,
    val yearMonth: String, // YYYY-MM
    val monthlyWorkingDays: Int,
    val dailyIncomeTarget: Double,
    val manualMonthlyTarget: Double? = null,
    val remainingWorkingDays: Int = 10,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val entityType: String,
    val entityId: String,
    val action: String,
    val userId: String,
    val userName: String,
    val oldValueJson: String,
    val newValueJson: String,
    val reason: String
) {
    fun toDomain(): AuditLogEntry = AuditLogEntry(
        id = id,
        timestamp = timestamp,
        entityType = entityType,
        entityId = entityId,
        action = action,
        userId = userId,
        userName = userName,
        oldValueJson = oldValueJson,
        newValueJson = newValueJson,
        reason = reason
    )

    companion object {
        fun fromDomain(a: AuditLogEntry): AuditLogEntity = AuditLogEntity(
            id = a.id,
            timestamp = a.timestamp,
            entityType = a.entityType,
            entityId = a.entityId,
            action = a.action,
            userId = a.userId,
            userName = a.userName,
            oldValueJson = a.oldValueJson,
            newValueJson = a.newValueJson,
            reason = a.reason
        )
    }
}

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: String = "singleton_settings",
    val defaultWorkingDays: Int = 26,
    val recipientPhoneNumbers: String = "+8801711000000, +8801811000000",
    val usgLabel: String = "ইউএসজি আয় (USG Income ৳)",
    val allowAutoVerificationIfMatches: Boolean = true,
    val activeMonth: String = "2026-10"
)

package com.example.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * User roles for Grameen Kalyan Keraniganj
 */
enum class UserRole(val displayNameBangla: String, val displayNameEnglish: String) {
    ADMIN("এডমিন (Admin)", "Admin"),
    RM("আঞ্চলিক ব্যবস্থাপক (RM)", "Regional Manager"),
    ACCOUNTS("হিসাবরক্ষক (Accounts)", "Accounts Officer"),
    CENTRE_USER("কেন্দ্রীয় ব্যবহারকারী (Centre User)", "Centre User");

    fun canEditApprovedData(): Boolean = this == ADMIN
    fun canManageUsersAndCentres(): Boolean = this == ADMIN
    fun canPerformCorrections(): Boolean = this == ADMIN || this == ACCOUNTS
    fun canVerifyReports(): Boolean = this == ADMIN || this == ACCOUNTS
    fun canViewAllCentres(): Boolean = this == ADMIN || this == RM || this == ACCOUNTS
}

/**
 * App User Model
 */
data class User(
    val id: String,
    val name: String,
    val role: UserRole,
    val assignedCentreId: String? = null,
    val email: String = ""
)

/**
 * Report submission & synchronization status
 */
enum class ReportStatus(val titleBangla: String, val titleEnglish: String) {
    DRAFT("খসড়া", "Draft"),
    QUEUED_OFFLINE("অফলাইনে অপেক্ষমাণ", "Queued Offline"),
    SYNCING("সিঙ্ক হচ্ছে", "Syncing"),
    SUBMITTED("দাখিলকৃত", "Submitted"),
    PENDING_VERIFICATION("যাচাইকরণ প্রয়োজন", "Pending Verification"),
    VALIDATED("অনুমোদিত / যাচাইকৃত", "Validated"),
    REJECTED("প্রত্যাখ্যাত", "Rejected")
}

/**
 * Health Centre Entity
 */
data class HealthCentre(
    val id: String, // Unique ID e.g. "gk-c01"
    val nameBangla: String,
    val nameEnglish: String,
    val code: String,
    val assignedUserId: String = "",
    val active: Boolean = true,
    val defaultDailyTarget: Double = 10000.0,
    val defaultMonthlyWorkingDays: Int = 26
)

/**
 * 11 Initial Health Centres in Keraniganj
 */
val INITIAL_KERANIGANJ_CENTRES = listOf(
    HealthCentre("gk-c01", "মালখানগর", "Malkhanagar", "MKH", defaultDailyTarget = 15000.0),
    HealthCentre("gk-c02", "ভাগ্যকুল", "Bhaggakul", "BGK", defaultDailyTarget = 12000.0),
    HealthCentre("gk-c03", "সিংপাড়া", "Singpara", "SGP", defaultDailyTarget = 11000.0),
    HealthCentre("gk-c04", "তেঘোরিয়া", "Teghoria", "TGH", defaultDailyTarget = 10000.0),
    HealthCentre("gk-c05", "ম্যালিরনকো বাজার", "Malironko Bazar", "MKB", defaultDailyTarget = 9500.0),
    HealthCentre("gk-c06", "যশলং", "Joshlong", "JSL", defaultDailyTarget = 9000.0),
    HealthCentre("gk-c07", "শেখানগর", "Shekhonagar", "SKN", defaultDailyTarget = 10500.0),
    HealthCentre("gk-c08", "শিকারী পাড়া", "Shikari Para", "SKP", defaultDailyTarget = 11500.0),
    HealthCentre("gk-c09", "কার্তিকপুর", "Kartikpur", "KTP", defaultDailyTarget = 10000.0),
    HealthCentre("gk-c10", "ষোল্লাবাজার", "Shollabazar", "SLB", defaultDailyTarget = 12500.0),
    HealthCentre("gk-c11", "টঙ্গীবাড়ী", "Tongibari", "TGB", defaultDailyTarget = 13000.0)
)

/**
 * Daily Report Form - 8 Core Fields
 */
data class DailyReport(
    val id: String,
    val centreId: String,
    val reportDate: String, // YYYY-MM-DD
    val reportingPeriod: String = "দৈনিক (Daily)",
    val submitterId: String,
    val submitterName: String,
    // Field 1: Centre patients
    val centrePatients: Int,
    // Field 2: Satellite patients
    val satellitePatients: Int,
    // Field 3: Camp patients
    val campPatients: Int,
    // Field 4: Total daily income (BDT)
    val totalDailyIncome: Double,
    // Field 5: Medicine & laboratory cost (BDT) - Reported separately, warning if > field 8
    val medicineLabCost: Double,
    // Field 6: Number of satellite sessions
    val satelliteSessions: Int,
    // Field 7: Number of cards
    val cardsCount: Int,
    // Field 8: Total daily expense including depreciation (BDT)
    val totalDailyExpense: Double,
    val notes: String = "",
    val status: ReportStatus = ReportStatus.VALIDATED,
    val warningNote: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val verifiedBy: String? = null
) {
    val totalPatients: Int get() = centrePatients + satellitePatients + campPatients
}

/**
 * Weekly Report Form
 */
data class WeeklyReport(
    val id: String,
    val centreId: String,
    val periodStartDate: String, // YYYY-MM-DD
    val periodEndDate: String,   // YYYY-MM-DD
    val submitterId: String,
    val submitterName: String,
    // 1(a), 1(b), 1(c)
    val centrePatients: Int,
    val satellitePatients: Int,
    val campPatients: Int,
    // 2. Camps count
    val campsCount: Int,
    // 3. Centre Income Breakdown
    val centrePrescriptionIncome: Double, // 3.1
    val centreMedicineIncome: Double,     // 3.2
    val centreLabIncome: Double,          // 3.3
    val centreUsgIncome: Double,          // 3.4
    val centreOtherIncome: Double,        // 3.5
    // 4. Camp Income Breakdown
    val campPrescriptionIncome: Double,   // 4.1
    val campMedicineSalesIncome: Double,  // 4.2
    val campLabIncome: Double,            // 4.3
    val campUsgIncome: Double,            // 4.4
    val campOtherIncome: Double,          // 4.5
    // 5. Total reported weekly income
    val reportedWeeklyIncome: Double,
    // 6. Total weekly expense including depreciation
    val totalWeeklyExpense: Double,
    // Calculated aggregates
    val calculatedCentreIncome: Double,
    val calculatedCampIncome: Double,
    val calculatedWeeklyIncome: Double,
    val discrepancyAmount: Double,
    val status: ReportStatus,
    val notes: String = "",
    val resolutionReason: String? = null,
    val verifiedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Target Sheet Row (Columns A to J)
 */
data class TargetSheetRow(
    val centreId: String,
    val centreNameBangla: String,      // Column A
    val centreNameEnglish: String,
    val monthlyWorkingDays: Int,       // Column B
    val dailyIncomeTarget: Double,     // Column C
    val monthlyIncomeTarget: Double,   // Column D (B * C or manual override)
    val currentDayIncome: Double,      // Column E
    val monthToDateIncome: Double,     // Column F
    val incomeShortfall: Double,       // Column G: max(D - F, 0)
    val achievementPercentage: Double, // Column H: (F / D) * 100
    val requiredDailyIncome: Double,   // Column I: max(D - F, 0) / J
    val remainingWorkingDays: Int      // Column J
)

/**
 * Cost Recovery Result with safe edge case handling
 */
data class CostRecoveryResult(
    val percentage: Double?,
    val formattedDisplayBangla: String,
    val formattedDisplayEnglish: String,
    val isReliable: Boolean
)

/**
 * App Settings
 */
data class AppSettings(
    val defaultWorkingDays: Int = 26,
    val recipientPhoneNumbers: String = "+8801711000000, +8801811000000",
    val usgLabel: String = "ইউএসজি আয় (USG Income ৳)",
    val allowAutoVerificationIfMatches: Boolean = true,
    val activeMonth: String = "2026-10"
)

/**
 * Audit Log Record
 */
data class AuditLogEntry(
    val id: String,
    val timestamp: Long,
    val entityType: String, // "DAILY_REPORT", "WEEKLY_REPORT", "CENTRE", "TARGET", "USER"
    val entityId: String,
    val action: String,     // "CREATE", "UPDATE", "CORRECTION", "VERIFY", "REJECT", "SETTINGS_CHANGE"
    val userId: String,
    val userName: String,
    val oldValueJson: String,
    val newValueJson: String,
    val reason: String
)

/**
 * Formatting and Bengali localization helper
 */
object BanglaFormatter {
    private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBanglaDigits(input: String): String {
        val sb = java.lang.StringBuilder()
        for (ch in input) {
            val idx = ch - '0'
            if (idx in 0..9) {
                sb.append(banglaDigits[idx])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toBanglaDigits(number: Number): String {
        val text = if (number is Double || number is Float) {
            String.format(Locale.US, "%.2f", number.toDouble())
        } else {
            number.toString()
        }
        return toBanglaDigits(text)
    }

    fun formatCurrencyBdt(amount: Double, showSymbol: Boolean = true): String {
        val rounded = BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).toDouble()
        val formatted = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }.format(rounded)
        val banglaNumber = toBanglaDigits(formatted)
        return if (showSymbol) "৳ $banglaNumber" else banglaNumber
    }

    fun formatPercentage(percentage: Double?): String {
        if (percentage == null) return "০%"
        val rounded = BigDecimal(percentage).setScale(1, RoundingMode.HALF_UP).toDouble()
        val text = String.format(Locale.US, "%.1f%%", rounded)
        return toBanglaDigits(text)
    }
}

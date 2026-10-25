package com.example.data

import android.content.Context
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Main Data Repository managing local persistence, offline queue,
 * accounting validations, audit logs, and business rules.
 */
class AccountingRepository private constructor(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val centreDao = db.centreDao()
    private val dailyDao = db.dailyReportDao()
    private val weeklyDao = db.weeklyReportDao()
    private val targetDao = db.targetDao()
    private val auditDao = db.auditLogDao()
    private val settingsDao = db.settingsDao()

    // Session State: Current User
    private val _currentUser = MutableStateFlow(
        User(
            id = "u-admin-01",
            name = "প্রকৌশলী রফিকুল ইসলাম (Admin)",
            role = UserRole.ADMIN,
            assignedCentreId = null,
            email = "admin.keraniganj@grameenkalyan.org"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Active Selected Month (e.g. "2026-10")
    private val _selectedMonth = MutableStateFlow(
        SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    )
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    // Cloud Sync Status Flow
    private val _syncState = MutableStateFlow("সিঙ্ক সম্পন্ন (সর্বশেষ সিঙ্ক: আজ)")
    val syncState: StateFlow<String> = _syncState.asStateFlow()

    init {
        // Seed default 11 centres and sample data in background
        CoroutineScope(Dispatchers.IO).launch {
            seedCentresAndInitialData()
        }
    }

    fun switchUserRole(role: UserRole, centreId: String? = null) {
        val user = when (role) {
            UserRole.ADMIN -> User(
                id = "u-admin-01",
                name = "আহমেদ হাসান (প্রধান এডমিন)",
                role = UserRole.ADMIN,
                assignedCentreId = null,
                email = "admin@grameenkalyan.org"
            )
            UserRole.RM -> User(
                id = "u-rm-01",
                name = "মোঃ জহিরুল হক (আঞ্চলিক ব্যবস্থাপক - কেরানীগঞ্জ)",
                role = UserRole.RM,
                assignedCentreId = null,
                email = "rm.keraniganj@grameenkalyan.org"
            )
            UserRole.ACCOUNTS -> User(
                id = "u-acc-01",
                name = "নাসরিন সুলতানা (হিসাব নিরীক্ষক)",
                role = UserRole.ACCOUNTS,
                assignedCentreId = null,
                email = "accounts.keraniganj@grameenkalyan.org"
            )
            UserRole.CENTRE_USER -> {
                val targetCentreId = centreId ?: "gk-c11" // Default Tongibari
                User(
                    id = "u-centre-${targetCentreId}",
                    name = "কেন্দ্রীয় ইনচার্জ (${targetCentreId})",
                    role = UserRole.CENTRE_USER,
                    assignedCentreId = targetCentreId,
                    email = "centre.${targetCentreId}@grameenkalyan.org"
                )
            }
        }
        _currentUser.value = user
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    // --- Centres ---
    fun getCentresFlow(): Flow<List<HealthCentre>> {
        return centreDao.getAllCentresFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getCentreById(id: String): HealthCentre? {
        return withContext(Dispatchers.IO) {
            centreDao.getCentreById(id)?.toDomain()
        }
    }

    suspend fun updateCentre(centre: HealthCentre, reason: String = "এডমিন কর্তৃক তথ্য পরিবর্তন"): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val user = _currentUser.value
            if (!user.role.canManageUsersAndCentres()) {
                return@withContext Result.failure(IllegalAccessException("শুধুমাত্র এডমিন কেন্দ্র সম্পাদনা করতে পারেন।"))
            }
            val existing = centreDao.getCentreById(centre.id)
            centreDao.insertOrUpdate(CentreEntity.fromDomain(centre))
            auditDao.insert(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    entityType = "CENTRE",
                    entityId = centre.id,
                    action = "UPDATE",
                    userId = user.id,
                    userName = user.name,
                    oldValueJson = existing?.nameBangla ?: "",
                    newValueJson = centre.nameBangla,
                    reason = reason
                )
            )
            Result.success(Unit)
        }
    }

    // --- Daily Reports ---
    fun getAllDailyReportsFlow(): Flow<List<DailyReport>> {
        return dailyDao.getAllDailyReportsFlow().map { list -> list.map { it.toDomain() } }
    }

    fun getDailyReportsForMonthFlow(month: String): Flow<List<DailyReport>> {
        return dailyDao.getReportsForMonthFlow(month).map { list -> list.map { it.toDomain() } }
    }

    fun getPendingDailyReportsFlow(): Flow<List<DailyReport>> {
        return dailyDao.getPendingReportsFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun submitDailyReport(report: DailyReport, isCorrection: Boolean = false, correctionReason: String = ""): Result<DailyReport> {
        return withContext(Dispatchers.IO) {
            val user = _currentUser.value

            // RBAC Check: Centre users can only submit for their assigned centre
            if (user.role == UserRole.CENTRE_USER && user.assignedCentreId != null && user.assignedCentreId != report.centreId) {
                return@withContext Result.failure(IllegalAccessException("আপনি শুধুমাত্র আপনার নির্ধারিত কেন্দ্রের রিপোর্ট দাখিল করতে পারবেন।"))
            }

            // Check duplicate submission
            val duplicate = dailyDao.findDuplicateReport(report.centreId, report.reportDate)
            if (duplicate != null && duplicate.id != report.id && !isCorrection) {
                return@withContext Result.failure(
                    IllegalArgumentException("এই কেন্দ্রে '${report.reportDate}' তারিখের রিপোর্ট ইতোমধ্যে দাখিল করা হয়েছে (ডুপ্লিকেট এন্ট্রি নিষিদ্ধ)!")
                )
            }

            // Numeric non-negative checks
            if (report.centrePatients < 0 || report.satellitePatients < 0 || report.campPatients < 0 ||
                report.totalDailyIncome < 0.0 || report.medicineLabCost < 0.0 ||
                report.satelliteSessions < 0 || report.cardsCount < 0 || report.totalDailyExpense < 0.0) {
                return@withContext Result.failure(IllegalArgumentException("কোনো ফিল্ডে ঋণাত্মক মান গ্রহণযোগ্য নয়।"))
            }

            // Warning note if medicineLabCost > totalDailyExpense
            val warning = if (report.medicineLabCost > report.totalDailyExpense) {
                "সতর্কতা: ঔষধ ও ল্যাব খরচ (${BanglaFormatter.formatCurrencyBdt(report.medicineLabCost)}) মোট ব্যয়ের (${BanglaFormatter.formatCurrencyBdt(report.totalDailyExpense)}) চেয়ে বেশি। হিসাবরক্ষক যাচাই করুন।"
            } else null

            val reportToSave = report.copy(
                warningNote = warning,
                updatedAt = System.currentTimeMillis()
            )

            val existingEntity = dailyDao.getReportById(reportToSave.id)
            dailyDao.insertOrUpdate(DailyReportEntity.fromDomain(reportToSave))

            // Audit Log
            auditDao.insert(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    entityType = "DAILY_REPORT",
                    entityId = reportToSave.id,
                    action = if (isCorrection) "CORRECTION" else if (existingEntity == null) "CREATE" else "UPDATE",
                    userId = user.id,
                    userName = user.name,
                    oldValueJson = existingEntity?.let { "Income: ${it.totalDailyIncome}, Expense: ${it.totalDailyExpense}" } ?: "NEW",
                    newValueJson = "Income: ${reportToSave.totalDailyIncome}, Expense: ${reportToSave.totalDailyExpense}",
                    reason = if (isCorrection) correctionReason else "নিয়মিত দৈনিক রিপোর্ট দাখিল"
                )
            )

            Result.success(reportToSave)
        }
    }

    // --- Weekly Reports ---
    fun getAllWeeklyReportsFlow(): Flow<List<WeeklyReport>> {
        return weeklyDao.getAllWeeklyReportsFlow().map { list -> list.map { it.toDomain() } }
    }

    fun getPendingWeeklyReportsFlow(): Flow<List<WeeklyReport>> {
        return weeklyDao.getPendingWeeklyReportsFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun submitWeeklyReport(
        centreId: String,
        periodStartDate: String,
        periodEndDate: String,
        centrePatients: Int,
        satellitePatients: Int,
        campPatients: Int,
        campsCount: Int,
        cPrescription: Double,
        cMedicine: Double,
        cLab: Double,
        cUsg: Double,
        cOther: Double,
        campPrescription: Double,
        campMedicine: Double,
        campLab: Double,
        campUsg: Double,
        campOther: Double,
        reportedWeeklyIncome: Double,
        totalWeeklyExpense: Double,
        notes: String = ""
    ): Result<WeeklyReport> {
        return withContext(Dispatchers.IO) {
            val user = _currentUser.value

            if (user.role == UserRole.CENTRE_USER && user.assignedCentreId != null && user.assignedCentreId != centreId) {
                return@withContext Result.failure(IllegalAccessException("অনুমতি নেই: শুধুমাত্র নির্ধারিত কেন্দ্রে রিপোর্ট করতে পারবেন।"))
            }

            // Check duplicate weekly report
            val duplicate = weeklyDao.findDuplicateWeeklyReport(centreId, periodStartDate, periodEndDate)
            if (duplicate != null) {
                return@withContext Result.failure(
                    IllegalArgumentException("এই কেন্দ্রে '${periodStartDate}' হতে '${periodEndDate}' মেয়াদের সাপ্তাহিক রিপোর্ট ইতোমধ্যে বিদ্যমান!")
                )
            }

            // Validate using pure accounting reconciliation engine
            val reconciliation = AccountingCalculations.reconcileWeeklyReport(
                cPrescription = cPrescription,
                cMedicine = cMedicine,
                cLab = cLab,
                cUsg = cUsg,
                cOther = cOther,
                campPrescription = campPrescription,
                campMedicine = campMedicine,
                campLab = campLab,
                campUsg = campUsg,
                campOther = campOther,
                reportedWeeklyIncome = reportedWeeklyIncome
            )

            val reportId = UUID.randomUUID().toString()
            val weeklyReport = WeeklyReport(
                id = reportId,
                centreId = centreId,
                periodStartDate = periodStartDate,
                periodEndDate = periodEndDate,
                submitterId = user.id,
                submitterName = user.name,
                centrePatients = centrePatients,
                satellitePatients = satellitePatients,
                campPatients = campPatients,
                campsCount = campsCount,
                centrePrescriptionIncome = cPrescription,
                centreMedicineIncome = cMedicine,
                centreLabIncome = cLab,
                centreUsgIncome = cUsg,
                centreOtherIncome = cOther,
                campPrescriptionIncome = campPrescription,
                campMedicineSalesIncome = campMedicine,
                campLabIncome = campLab,
                campUsgIncome = campUsg,
                campOtherIncome = campOther,
                reportedWeeklyIncome = reportedWeeklyIncome,
                totalWeeklyExpense = totalWeeklyExpense,
                calculatedCentreIncome = reconciliation.calculatedCentreIncome,
                calculatedCampIncome = reconciliation.calculatedCampIncome,
                calculatedWeeklyIncome = reconciliation.calculatedWeeklyIncome,
                discrepancyAmount = reconciliation.discrepancyAmount,
                status = reconciliation.status,
                notes = notes,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            weeklyDao.insertOrUpdate(WeeklyReportEntity.fromDomain(weeklyReport))

            auditDao.insert(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    entityType = "WEEKLY_REPORT",
                    entityId = reportId,
                    action = "CREATE",
                    userId = user.id,
                    userName = user.name,
                    oldValueJson = "NEW",
                    newValueJson = "Reported: $reportedWeeklyIncome, Calc: ${reconciliation.calculatedWeeklyIncome}, Diff: ${reconciliation.discrepancyAmount}, Status: ${reconciliation.status}",
                    reason = "সাপ্তাহিক রিপোর্ট দাখিল (${reconciliation.status.titleBangla})"
                )
            )

            Result.success(weeklyReport)
        }
    }

    suspend fun resolveWeeklyDiscrepancy(
        reportId: String,
        correctedReportedIncome: Double,
        reason: String
    ): Result<WeeklyReport> {
        return withContext(Dispatchers.IO) {
            val user = _currentUser.value
            if (!user.role.canPerformCorrections()) {
                return@withContext Result.failure(IllegalAccessException("শুধুমাত্র এডমিন বা হিসাবরক্ষক অমিল সংশোধন করতে পারেন।"))
            }

            val existing = weeklyDao.getWeeklyReportById(reportId)?.toDomain()
                ?: return@withContext Result.failure(NoSuchElementException("রিপোর্ট পাওয়া যায়নি।"))

            val reconciliation = AccountingCalculations.reconcileWeeklyReport(
                cPrescription = existing.centrePrescriptionIncome,
                cMedicine = existing.centreMedicineIncome,
                cLab = existing.centreLabIncome,
                cUsg = existing.centreUsgIncome,
                cOther = existing.centreOtherIncome,
                campPrescription = existing.campPrescriptionIncome,
                campMedicine = existing.campMedicineSalesIncome,
                campLab = existing.campLabIncome,
                campUsg = existing.campUsgIncome,
                campOther = existing.campOtherIncome,
                reportedWeeklyIncome = correctedReportedIncome
            )

            val updated = existing.copy(
                reportedWeeklyIncome = correctedReportedIncome,
                discrepancyAmount = reconciliation.discrepancyAmount,
                status = reconciliation.status,
                resolutionReason = reason,
                verifiedBy = user.name,
                updatedAt = System.currentTimeMillis()
            )

            weeklyDao.insertOrUpdate(WeeklyReportEntity.fromDomain(updated))

            auditDao.insert(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    entityType = "WEEKLY_REPORT",
                    entityId = reportId,
                    action = "CORRECTION",
                    userId = user.id,
                    userName = user.name,
                    oldValueJson = "Reported: ${existing.reportedWeeklyIncome}, Status: ${existing.status}",
                    newValueJson = "Reported: $correctedReportedIncome, Status: ${reconciliation.status}",
                    reason = reason
                )
            )

            Result.success(updated)
        }
    }

    // --- Target & Monthly Spreadsheet Calculations ---
    suspend fun getTargetSheetRowsForMonth(month: String): List<TargetSheetRow> {
        return withContext(Dispatchers.IO) {
            val centres = centreDao.getAllCentres().map { it.toDomain() }
            val dailyReports = dailyDao.getReportsForMonth(month).map { it.toDomain() }
                .filter { it.status == ReportStatus.VALIDATED }

            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            centres.map { centre ->
                val centreReports = dailyReports.filter { it.centreId == centre.id }
                val monthToDateIncome = centreReports.sumOf { it.totalDailyIncome }
                val currentDayIncome = centreReports.filter { it.reportDate == todayDate }.sumOf { it.totalDailyIncome }

                val targetEntity = targetDao.getTarget(centre.id, month)
                val workingDays = targetEntity?.monthlyWorkingDays ?: centre.defaultMonthlyWorkingDays
                val dailyTarget = targetEntity?.dailyIncomeTarget ?: centre.defaultDailyTarget
                val manualMonthly = targetEntity?.manualMonthlyTarget
                val remainingWorkingDays = targetEntity?.remainingWorkingDays ?: 10

                AccountingCalculations.calculateTargetRow(
                    centre = centre,
                    monthlyWorkingDays = workingDays,
                    dailyIncomeTarget = dailyTarget,
                    manualMonthlyTarget = manualMonthly,
                    currentDayIncome = currentDayIncome,
                    monthToDateIncome = monthToDateIncome,
                    remainingWorkingDays = remainingWorkingDays
                )
            }
        }
    }

    suspend fun saveTarget(
        centreId: String,
        month: String,
        workingDays: Int,
        dailyTarget: Double,
        manualMonthly: Double?,
        remainingDays: Int
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val user = _currentUser.value
            if (!user.role.canManageUsersAndCentres()) {
                return@withContext Result.failure(IllegalAccessException("শুধুমাত্র এডমিন লক্ষ্যমাত্রা পরিবর্তন করতে পারেন।"))
            }

            val targetId = "${centreId}_$month"
            val entity = MonthlyTargetEntity(
                id = targetId,
                centreId = centreId,
                yearMonth = month,
                monthlyWorkingDays = workingDays,
                dailyIncomeTarget = dailyTarget,
                manualMonthlyTarget = manualMonthly,
                remainingWorkingDays = remainingDays,
                updatedAt = System.currentTimeMillis()
            )
            targetDao.insertOrUpdate(entity)

            auditDao.insert(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    entityType = "TARGET",
                    entityId = targetId,
                    action = "UPDATE",
                    userId = user.id,
                    userName = user.name,
                    oldValueJson = "Target updated",
                    newValueJson = "Days: $workingDays, Daily: $dailyTarget, Manual: $manualMonthly, Rem: $remainingDays",
                    reason = "মাসিক লক্ষ্যমাত্রা হালনাগাদ ($month)"
                )
            )

            Result.success(Unit)
        }
    }

    // --- Audit Logs & Settings ---
    fun getAuditLogsFlow(): Flow<List<AuditLogEntry>> {
        return auditDao.getAllAuditLogsFlow().map { list -> list.map { it.toDomain() } }
    }

    fun getSettingsFlow(): Flow<AppSettings> {
        return settingsDao.getSettingsFlow().map { entity ->
            if (entity != null) {
                AppSettings(
                    defaultWorkingDays = entity.defaultWorkingDays,
                    recipientPhoneNumbers = entity.recipientPhoneNumbers,
                    usgLabel = entity.usgLabel,
                    allowAutoVerificationIfMatches = entity.allowAutoVerificationIfMatches,
                    activeMonth = entity.activeMonth
                )
            } else {
                AppSettings()
            }
        }
    }

    suspend fun updateSettings(settings: AppSettings): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val user = _currentUser.value
            if (!user.role.canManageUsersAndCentres()) {
                return@withContext Result.failure(IllegalAccessException("শুধুমাত্র এডমিন সেটিংস পরিবর্তন করতে পারেন।"))
            }
            settingsDao.insertOrUpdate(
                AppSettingsEntity(
                    defaultWorkingDays = settings.defaultWorkingDays,
                    recipientPhoneNumbers = settings.recipientPhoneNumbers,
                    usgLabel = settings.usgLabel,
                    allowAutoVerificationIfMatches = settings.allowAutoVerificationIfMatches,
                    activeMonth = settings.activeMonth
                )
            )
            Result.success(Unit)
        }
    }

    // Trigger Cloud Sync simulation
    suspend fun triggerSync(): String {
        return withContext(Dispatchers.IO) {
            _syncState.value = "ক্লাউড ফায়ারস্টোর সিঙ্ক হচ্ছে..."
            kotlinx.coroutines.delay(1000)
            _syncState.value = "সিঙ্ক সম্পন্ন: সকল রিপোর্ট ক্লাউড সার্ভারের সাথে সফলভাবে সমন্বিত হয়েছে।"
            "সকল রেকর্ড সফলভাবে ক্লাউডে সিঙ্ক করা হয়েছে।"
        }
    }

    /**
     * Seeds the 11 Keraniganj health centres and exact prompt test cases
     */
    private suspend fun seedCentresAndInitialData() {
        val existingCentres = centreDao.getAllCentres()
        if (existingCentres.isEmpty()) {
            val entities = INITIAL_KERANIGANJ_CENTRES.map { CentreEntity.fromDomain(it) }
            centreDao.insertCentres(entities)

            // Seed Tongibari Weekly Report (from prompt test cases)
            // Tongibari weekly centre income: 440 + 2947 + 1520 + 0 + 620 = 5527.
            // Tongibari weekly camp income: 600 + 3483 + 1180 + 0 + 320 = 5583.
            // Tongibari weekly total income: 11110. Expense: 1700.
            val tongibariRecon = AccountingCalculations.reconcileWeeklyReport(
                cPrescription = 440.0,
                cMedicine = 2947.0,
                cLab = 1520.0,
                cUsg = 0.0,
                cOther = 620.0,
                campPrescription = 600.0,
                campMedicine = 3483.0,
                campLab = 1180.0,
                campUsg = 0.0,
                campOther = 320.0,
                reportedWeeklyIncome = 11110.0
            )

            weeklyDao.insertOrUpdate(
                WeeklyReportEntity.fromDomain(
                    WeeklyReport(
                        id = "weekly-tongibari-01",
                        centreId = "gk-c11", // Tongibari
                        periodStartDate = "2026-10-01",
                        periodEndDate = "2026-10-07",
                        submitterId = "u-init",
                        submitterName = "ইনচার্জ টঙ্গীবাড়ী",
                        centrePatients = 120,
                        satellitePatients = 45,
                        campPatients = 80,
                        campsCount = 2,
                        centrePrescriptionIncome = 440.0,
                        centreMedicineIncome = 2947.0,
                        centreLabIncome = 1520.0,
                        centreUsgIncome = 0.0,
                        centreOtherIncome = 620.0,
                        campPrescriptionIncome = 600.0,
                        campMedicineSalesIncome = 3483.0,
                        campLabIncome = 1180.0,
                        campUsgIncome = 0.0,
                        campOtherIncome = 320.0,
                        reportedWeeklyIncome = 11110.0,
                        totalWeeklyExpense = 1700.0,
                        calculatedCentreIncome = tongibariRecon.calculatedCentreIncome,
                        calculatedCampIncome = tongibariRecon.calculatedCampIncome,
                        calculatedWeeklyIncome = tongibariRecon.calculatedWeeklyIncome,
                        discrepancyAmount = tongibariRecon.discrepancyAmount,
                        status = tongibariRecon.status,
                        notes = "টঙ্গীবাড়ী স্বাস্থ্য কেন্দ্র নমুনা সপ্তাহ ১ (যাচাইকৃত হিসাব)",
                        verifiedBy = "সিস্টেম অডিটর"
                    )
                )
            )

            // Seed Malkhanagar Weekly Report (from prompt test cases)
            // Malkhanagar weekly centre income: 3500 + 6163 + 1200 + 0 + 560 = 11423.
            // Malkhanagar weekly camp income: 4300 + 14283 + 9296 + 3300 + 1270 = 32449.
            // Malkhanagar weekly total income: 43872. Expense: 17000.
            val malkhanagarRecon = AccountingCalculations.reconcileWeeklyReport(
                cPrescription = 3500.0,
                cMedicine = 6163.0,
                cLab = 1200.0,
                cUsg = 0.0,
                cOther = 560.0,
                campPrescription = 4300.0,
                campMedicine = 14283.0,
                campLab = 9296.0,
                campUsg = 3300.0,
                campOther = 1270.0,
                reportedWeeklyIncome = 43872.0
            )

            weeklyDao.insertOrUpdate(
                WeeklyReportEntity.fromDomain(
                    WeeklyReport(
                        id = "weekly-malkhanagar-01",
                        centreId = "gk-c01", // Malkhanagar
                        periodStartDate = "2026-10-01",
                        periodEndDate = "2026-10-07",
                        submitterId = "u-init",
                        submitterName = "ইনচার্জ মালখানগর",
                        centrePatients = 310,
                        satellitePatients = 95,
                        campPatients = 240,
                        campsCount = 4,
                        centrePrescriptionIncome = 3500.0,
                        centreMedicineIncome = 6163.0,
                        centreLabIncome = 1200.0,
                        centreUsgIncome = 0.0,
                        centreOtherIncome = 560.0,
                        campPrescriptionIncome = 4300.0,
                        campMedicineSalesIncome = 14283.0,
                        campLabIncome = 9296.0,
                        campUsgIncome = 3300.0,
                        campOtherIncome = 1270.0,
                        reportedWeeklyIncome = 43872.0,
                        totalWeeklyExpense = 17000.0,
                        calculatedCentreIncome = malkhanagarRecon.calculatedCentreIncome,
                        calculatedCampIncome = malkhanagarRecon.calculatedCampIncome,
                        calculatedWeeklyIncome = malkhanagarRecon.calculatedWeeklyIncome,
                        discrepancyAmount = malkhanagarRecon.discrepancyAmount,
                        status = malkhanagarRecon.status,
                        notes = "মালখানগর স্বাস্থ্য কেন্দ্র নমুনা সপ্তাহ ১ (যাচাইকৃত হিসাব)",
                        verifiedBy = "সিস্টেম অডিটর"
                    )
                )
            )

            // Seed sample daily reports for October 2026
            val sampleDaily = listOf(
                DailyReport(
                    id = "daily-mkh-2026-10-20",
                    centreId = "gk-c01",
                    reportDate = "2026-10-20",
                    submitterId = "u-mkh",
                    submitterName = "ডাঃ সুফিয়ান",
                    centrePatients = 42,
                    satellitePatients = 15,
                    campPatients = 30,
                    totalDailyIncome = 18450.0,
                    medicineLabCost = 7200.0,
                    satelliteSessions = 1,
                    cardsCount = 12,
                    totalDailyExpense = 11200.0,
                    notes = "সফল স্যাটেলাইট ও ক্যাম্প সম্পন্ন",
                    status = ReportStatus.VALIDATED
                ),
                DailyReport(
                    id = "daily-tgb-2026-10-20",
                    centreId = "gk-c11",
                    reportDate = "2026-10-20",
                    submitterId = "u-tgb",
                    submitterName = "ডাঃ কামরুল",
                    centrePatients = 28,
                    satellitePatients = 10,
                    campPatients = 0,
                    totalDailyIncome = 14200.0,
                    medicineLabCost = 5400.0,
                    satelliteSessions = 1,
                    cardsCount = 8,
                    totalDailyExpense = 9800.0,
                    notes = "নিয়মিত ক্লিনিক কার্যক্রম",
                    status = ReportStatus.VALIDATED
                ),
                DailyReport(
                    id = "daily-bgk-2026-10-20",
                    centreId = "gk-c02",
                    reportDate = "2026-10-20",
                    submitterId = "u-bgk",
                    submitterName = "ফারুক হোসেন",
                    centrePatients = 35,
                    satellitePatients = 12,
                    campPatients = 25,
                    totalDailyIncome = 16800.0,
                    medicineLabCost = 6100.0,
                    satelliteSessions = 1,
                    cardsCount = 10,
                    totalDailyExpense = 10500.0,
                    notes = "নিয়মিত রিপোর্ট",
                    status = ReportStatus.VALIDATED
                )
            )

            sampleDaily.forEach {
                dailyDao.insertOrUpdate(DailyReportEntity.fromDomain(it))
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AccountingRepository? = null

        fun getInstance(context: Context): AccountingRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AccountingRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

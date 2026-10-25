package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccountingRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AccountingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AccountingRepository.getInstance(application)

    val currentUser: StateFlow<User> = repository.currentUser
    val selectedMonth: StateFlow<String> = repository.selectedMonth
    val syncState: StateFlow<String> = repository.syncState

    val allCentres: StateFlow<List<HealthCentre>> = repository.getCentresFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCentreId = MutableStateFlow("gk-c01")
    val selectedCentreId: StateFlow<String> = _selectedCentreId.asStateFlow()

    val allDailyReports: StateFlow<List<DailyReport>> = repository.getAllDailyReportsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWeeklyReports: StateFlow<List<WeeklyReport>> = repository.getAllWeeklyReportsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingWeeklyReports: StateFlow<List<WeeklyReport>> = repository.getPendingWeeklyReportsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntry>> = repository.getAuditLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appSettings: StateFlow<AppSettings> = repository.getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _targetSheetRows = MutableStateFlow<List<TargetSheetRow>>(emptyList())
    val targetSheetRows: StateFlow<List<TargetSheetRow>> = _targetSheetRows.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        // Refresh targets whenever month changes or reports change
        viewModelScope.launch {
            combine(selectedMonth, allDailyReports) { month, _ ->
                month
            }.collectLatest { month ->
                refreshTargetSheet(month)
            }
        }
    }

    fun selectCentre(id: String) {
        _selectedCentreId.value = id
    }

    fun selectMonth(month: String) {
        repository.setSelectedMonth(month)
        viewModelScope.launch {
            refreshTargetSheet(month)
        }
    }

    fun switchRole(role: UserRole, centreId: String? = null) {
        repository.switchUserRole(role, centreId)
        if (centreId != null) {
            _selectedCentreId.value = centreId
        }
        viewModelScope.launch {
            _userMessage.emit("ভূমিকা পরিবর্তন করা হয়েছে: ${role.displayNameBangla}")
        }
    }

    private suspend fun refreshTargetSheet(month: String) {
        val rows = repository.getTargetSheetRowsForMonth(month)
        _targetSheetRows.value = rows
    }

    fun submitDailyReport(
        centreId: String,
        reportDate: String,
        reportingPeriod: String,
        centrePatients: Int,
        satellitePatients: Int,
        campPatients: Int,
        totalDailyIncome: Double,
        medicineLabCost: Double,
        satelliteSessions: Int,
        cardsCount: Int,
        totalDailyExpense: Double,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val report = DailyReport(
                id = java.util.UUID.randomUUID().toString(),
                centreId = centreId,
                reportDate = reportDate,
                reportingPeriod = reportingPeriod,
                submitterId = user.id,
                submitterName = user.name,
                centrePatients = centrePatients,
                satellitePatients = satellitePatients,
                campPatients = campPatients,
                totalDailyIncome = totalDailyIncome,
                medicineLabCost = medicineLabCost,
                satelliteSessions = satelliteSessions,
                cardsCount = cardsCount,
                totalDailyExpense = totalDailyExpense,
                notes = notes,
                status = ReportStatus.VALIDATED
            )

            val result = repository.submitDailyReport(report)
            result.onSuccess {
                _userMessage.emit("দৈনিক রিপোর্ট সফলভাবে দাখিল করা হয়েছে!")
                refreshTargetSheet(selectedMonth.value)
                onSuccess()
            }.onFailure { err ->
                _userMessage.emit("দাখিলে ত্রুটি: ${err.message}")
            }
        }
    }

    fun submitWeeklyReport(
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
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.submitWeeklyReport(
                centreId = centreId,
                periodStartDate = periodStartDate,
                periodEndDate = periodEndDate,
                centrePatients = centrePatients,
                satellitePatients = satellitePatients,
                campPatients = campPatients,
                campsCount = campsCount,
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
                reportedWeeklyIncome = reportedWeeklyIncome,
                totalWeeklyExpense = totalWeeklyExpense,
                notes = notes
            )

            result.onSuccess { saved ->
                if (saved.status == ReportStatus.VALIDATED) {
                    _userMessage.emit("সাপ্তাহিক রিপোর্ট যাচাইকৃত ও সফলভাবে দাখিল হয়েছে!")
                } else {
                    _userMessage.emit("সতর্কতা: হিসাবে অমিল থাকায় রিপোর্টটি 'যাচাইকরণ অপেক্ষমাণ' তালিকায় যুক্ত হয়েছে!")
                }
                onSuccess()
            }.onFailure { err ->
                _userMessage.emit("দাখিলে ব্যর্থতা: ${err.message}")
            }
        }
    }

    fun resolveWeeklyDiscrepancy(reportId: String, correctedIncome: Double, reason: String) {
        viewModelScope.launch {
            val result = repository.resolveWeeklyDiscrepancy(reportId, correctedIncome, reason)
            result.onSuccess {
                _userMessage.emit("সাপ্তাহিক রিপোর্টের হিসাব সংশোধন ও অনুমোদন সম্পন্ন হয়েছে!")
            }.onFailure { err ->
                _userMessage.emit("সংশোধনে ত্রুটি: ${err.message}")
            }
        }
    }

    fun updateTarget(
        centreId: String,
        workingDays: Int,
        dailyTarget: Double,
        manualMonthly: Double?,
        remainingDays: Int
    ) {
        viewModelScope.launch {
            val result = repository.saveTarget(
                centreId = centreId,
                month = selectedMonth.value,
                workingDays = workingDays,
                dailyTarget = dailyTarget,
                manualMonthly = manualMonthly,
                remainingDays = remainingDays
            )
            result.onSuccess {
                _userMessage.emit("লক্ষ্যমাত্রা সফলভাবে সংরক্ষণ করা হয়েছে!")
                refreshTargetSheet(selectedMonth.value)
            }.onFailure { err ->
                _userMessage.emit("লক্ষ্যমাত্রা সংরক্ষণ ব্যর্থ: ${err.message}")
            }
        }
    }

    fun updateCentre(centre: HealthCentre, reason: String) {
        viewModelScope.launch {
            val result = repository.updateCentre(centre, reason)
            result.onSuccess {
                _userMessage.emit("কেন্দ্রের তথ্য সফলভাবে হালনাগাদ করা হয়েছে!")
            }.onFailure { err ->
                _userMessage.emit("হালনাগাদে ত্রুটি: ${err.message}")
            }
        }
    }

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            val result = repository.updateSettings(settings)
            result.onSuccess {
                _userMessage.emit("সিস্টেম সেটিংস সফলভাবে আপডেট হয়েছে!")
            }.onFailure { err ->
                _userMessage.emit("সেটিংস হালনাগাদ ব্যর্থ: ${err.message}")
            }
        }
    }

    fun syncWithCloud() {
        viewModelScope.launch {
            val msg = repository.triggerSync()
            _userMessage.emit(msg)
        }
    }
}

package com.example.model

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max

/**
 * Pure accounting calculation engine with decimal-safe arithmetic.
 */
object AccountingCalculations {

    /**
     * Calculate Cost Recovery (%)
     * Rule: Cost Recovery (%) = (Total Income / Total Expense Including Depreciation) * 100
     * Because total daily expense (Field 8) and weekly expense (Field 6) already include depreciation,
     * do not add depreciation a second time.
     */
    fun calculateCostRecovery(totalIncome: Double, totalExpense: Double): CostRecoveryResult {
        val incomeBd = BigDecimal.valueOf(totalIncome).setScale(2, RoundingMode.HALF_UP)
        val expenseBd = BigDecimal.valueOf(totalExpense).setScale(2, RoundingMode.HALF_UP)

        if (expenseBd.compareTo(BigDecimal.ZERO) == 0) {
            return if (incomeBd.compareTo(BigDecimal.ZERO) > 0) {
                CostRecoveryResult(
                    percentage = null,
                    formattedDisplayBangla = "ব্যয় নথিভুক্ত নয় / নির্ভরযোগ্যভাবে গণনা সম্ভব নয়",
                    formattedDisplayEnglish = "Expense not recorded / cannot calculate reliably",
                    isReliable = false
                )
            } else {
                CostRecoveryResult(
                    percentage = null,
                    formattedDisplayBangla = "কোনো আর্থিক লেনদেন নেই",
                    formattedDisplayEnglish = "No financial activity",
                    isReliable = false
                )
            }
        }

        val percentageBd = incomeBd
            .divide(expenseBd, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP)

        val percentage = percentageBd.toDouble()
        val formattedBangla = "${BanglaFormatter.toBanglaDigits(String.format(java.util.Locale.US, "%.1f", percentage))}%"
        val formattedEnglish = String.format(java.util.Locale.US, "%.1f%%", percentage)

        return CostRecoveryResult(
            percentage = percentage,
            formattedDisplayBangla = formattedBangla,
            formattedDisplayEnglish = formattedEnglish,
            isReliable = true
        )
    }

    /**
     * Weekly Report Income Reconciliation
     * 3.1..3.5 = Centre Income Breakdown
     * 4.1..4.5 = Camp Income Breakdown
     */
    data class WeeklyReconciliationResult(
        val calculatedCentreIncome: Double,
        val calculatedCampIncome: Double,
        val calculatedWeeklyIncome: Double,
        val reportedWeeklyIncome: Double,
        val discrepancyAmount: Double,
        val isMatched: Boolean,
        val status: ReportStatus,
        val discrepancyMessageBangla: String
    )

    fun reconcileWeeklyReport(
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
        reportedWeeklyIncome: Double
    ): WeeklyReconciliationResult {
        val centreIncomeBd = BigDecimal.valueOf(cPrescription)
            .add(BigDecimal.valueOf(cMedicine))
            .add(BigDecimal.valueOf(cLab))
            .add(BigDecimal.valueOf(cUsg))
            .add(BigDecimal.valueOf(cOther))
            .setScale(2, RoundingMode.HALF_UP)

        val campIncomeBd = BigDecimal.valueOf(campPrescription)
            .add(BigDecimal.valueOf(campMedicine))
            .add(BigDecimal.valueOf(campLab))
            .add(BigDecimal.valueOf(campUsg))
            .add(BigDecimal.valueOf(campOther))
            .setScale(2, RoundingMode.HALF_UP)

        val totalCalculatedBd = centreIncomeBd.add(campIncomeBd).setScale(2, RoundingMode.HALF_UP)
        val reportedBd = BigDecimal.valueOf(reportedWeeklyIncome).setScale(2, RoundingMode.HALF_UP)

        val discrepancyBd = reportedBd.subtract(totalCalculatedBd).setScale(2, RoundingMode.HALF_UP)
        val isMatched = discrepancyBd.compareTo(BigDecimal.ZERO) == 0

        val status = if (isMatched) ReportStatus.VALIDATED else ReportStatus.PENDING_VERIFICATION

        val discrepancyMsg = if (isMatched) {
            "হিসাব সঠিক: কেন্দ্র ও ক্যাম্প উপখাতসমূহের মোট যোগফল সাপ্তাহিক আয়ের সমান।"
        } else {
            val formattedDiff = BanglaFormatter.formatCurrencyBdt(discrepancyBd.abs().toDouble())
            val direction = if (discrepancyBd > BigDecimal.ZERO) "রিপোর্টে বেশি" else "রিপোর্টে কম"
            "অমিল সনাক্ত: গণনাকৃত যোগফল (${BanglaFormatter.formatCurrencyBdt(totalCalculatedBd.toDouble())}) ও দাখিলকৃত আয়ের মধ্যে $formattedDiff ($direction) ব্যবধান রয়েছে।"
        }

        return WeeklyReconciliationResult(
            calculatedCentreIncome = centreIncomeBd.toDouble(),
            calculatedCampIncome = campIncomeBd.toDouble(),
            calculatedWeeklyIncome = totalCalculatedBd.toDouble(),
            reportedWeeklyIncome = reportedBd.toDouble(),
            discrepancyAmount = discrepancyBd.toDouble(),
            isMatched = isMatched,
            status = status,
            discrepancyMessageBangla = discrepancyMsg
        )
    }

    /**
     * Compute Target Sheet Row (Columns A through J)
     */
    fun calculateTargetRow(
        centre: HealthCentre,
        monthlyWorkingDays: Int,
        dailyIncomeTarget: Double,
        manualMonthlyTarget: Double?,
        currentDayIncome: Double,
        monthToDateIncome: Double,
        remainingWorkingDays: Int
    ): TargetSheetRow {
        // D = B * C, unless manual monthly target override
        val targetMonthly = if (manualMonthlyTarget != null && manualMonthlyTarget > 0.0) {
            manualMonthlyTarget
        } else {
            BigDecimal.valueOf(monthlyWorkingDays.toLong())
                .multiply(BigDecimal.valueOf(dailyIncomeTarget))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        }

        // G = max(D - F, 0)
        val shortfall = max(targetMonthly - monthToDateIncome, 0.0)

        // H = (F / D) * 100
        val achievementPercentage = if (targetMonthly > 0.0) {
            BigDecimal.valueOf(monthToDateIncome)
                .divide(BigDecimal.valueOf(targetMonthly), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        } else {
            0.0
        }

        // I = max(D - F, 0) / J
        val requiredDaily = if (remainingWorkingDays > 0) {
            BigDecimal.valueOf(shortfall)
                .divide(BigDecimal.valueOf(remainingWorkingDays.toLong()), 2, RoundingMode.HALF_UP)
                .toDouble()
        } else {
            0.0
        }

        return TargetSheetRow(
            centreId = centre.id,
            centreNameBangla = centre.nameBangla,
            centreNameEnglish = centre.nameEnglish,
            monthlyWorkingDays = monthlyWorkingDays,
            dailyIncomeTarget = dailyIncomeTarget,
            monthlyIncomeTarget = targetMonthly,
            currentDayIncome = currentDayIncome,
            monthToDateIncome = monthToDateIncome,
            incomeShortfall = shortfall,
            achievementPercentage = achievementPercentage,
            requiredDailyIncome = requiredDaily,
            remainingWorkingDays = remainingWorkingDays
        )
    }

    /**
     * Formats Daily Report SMS payload
     */
    fun formatDailyReportSms(report: DailyReport, centre: HealthCentre): String {
        return buildString {
            append("গ্রামীণ কল্যাণ কেরানীগঞ্জ\n")
            append("কেন্দ্র: ${centre.nameBangla} (${centre.code})\n")
            append("তারিখ: ${report.reportDate}\n")
            append("১. কেন্দ্র রোগী: ${BanglaFormatter.toBanglaDigits(report.centrePatients)}\n")
            append("২. স্যাটেলাইট রোগী: ${BanglaFormatter.toBanglaDigits(report.satellitePatients)}\n")
            append("৩. ক্যাম্প রোগী: ${BanglaFormatter.toBanglaDigits(report.campPatients)}\n")
            append("৪. দৈনিক মোট আয়: ${BanglaFormatter.formatCurrencyBdt(report.totalDailyIncome)}\n")
            append("৫. ঔষধ ও ল্যাব খরচ: ${BanglaFormatter.formatCurrencyBdt(report.medicineLabCost)}\n")
            append("৬. স্যাটেলাইট সেশন: ${BanglaFormatter.toBanglaDigits(report.satelliteSessions)}\n")
            append("৭. কার্ড সংখ্যা: ${BanglaFormatter.toBanglaDigits(report.cardsCount)}\n")
            append("৮. মোট দৈনিক ব্যয়: ${BanglaFormatter.formatCurrencyBdt(report.totalDailyExpense)}\n")
            append("দাখিলকারী: ${report.submitterName}\n")
            append("আইডি: ${report.id.take(8)}")
        }
    }

    /**
     * Formats Weekly Report SMS payload
     */
    fun formatWeeklyReportSms(report: WeeklyReport, centre: HealthCentre): String {
        return buildString {
            append("গ্রামীণ কল্যাণ কেরানীগঞ্জ (সাপ্তাহিক)\n")
            append("কেন্দ্র: ${centre.nameBangla} (${centre.code})\n")
            append("মেয়াদ: ${report.periodStartDate} হতে ${report.periodEndDate}\n")
            append("রোগী সংখ্যা (কেন্দ্র/স্যাট/ক্যাম্প): ${BanglaFormatter.toBanglaDigits(report.centrePatients)}/${BanglaFormatter.toBanglaDigits(report.satellitePatients)}/${BanglaFormatter.toBanglaDigits(report.campPatients)}\n")
            append("ক্যাম্প সংখ্যা: ${BanglaFormatter.toBanglaDigits(report.campsCount)}\n")
            append("কেন্দ্র আয়: ${BanglaFormatter.formatCurrencyBdt(report.calculatedCentreIncome)}\n")
            append("ক্যাম্প আয়: ${BanglaFormatter.formatCurrencyBdt(report.calculatedCampIncome)}\n")
            append("মোট সাপ্তাহিক আয়: ${BanglaFormatter.formatCurrencyBdt(report.reportedWeeklyIncome)}\n")
            append("মোট সাপ্তাহিক ব্যয়: ${BanglaFormatter.formatCurrencyBdt(report.totalWeeklyExpense)}\n")
            append("অবস্থা: ${report.status.titleBangla}\n")
            append("আইডি: ${report.id.take(8)}")
        }
    }
}

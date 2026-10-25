package com.example

import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class AccountingSystemTest {

    @Test
    fun testTongibariWeeklyReconciliationPassesValidation() {
        // Tongibari weekly centre income: 440 + 2947 + 1520 + 0 + 620 = 5527.
        // Tongibari weekly camp income: 600 + 3483 + 1180 + 0 + 320 = 5583.
        // Tongibari weekly total income: 11110. (Expense: 1700)
        val result = AccountingCalculations.reconcileWeeklyReport(
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

        assertEquals("Centre income must equal 5527.0", 5527.0, result.calculatedCentreIncome, 0.001)
        assertEquals("Camp income must equal 5583.0", 5583.0, result.calculatedCampIncome, 0.001)
        assertEquals("Total weekly income must equal 11110.0", 11110.0, result.calculatedWeeklyIncome, 0.001)
        assertEquals("Discrepancy must be 0", 0.0, result.discrepancyAmount, 0.001)
        assertTrue("Report must be matched", result.isMatched)
        assertEquals("Status must be VALIDATED", ReportStatus.VALIDATED, result.status)
    }

    @Test
    fun testMalkhanagarWeeklyReconciliationPassesValidation() {
        // Malkhanagar weekly centre income: 3500 + 6163 + 1200 + 0 + 560 = 11423.
        // Malkhanagar weekly camp income: 4300 + 14283 + 9296 + 3300 + 1270 = 32449.
        // Malkhanagar weekly total income: 43872. (Expense: 17000)
        val result = AccountingCalculations.reconcileWeeklyReport(
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

        assertEquals("Centre income must equal 11423.0", 11423.0, result.calculatedCentreIncome, 0.001)
        assertEquals("Camp income must equal 32449.0", 32449.0, result.calculatedCampIncome, 0.001)
        assertEquals("Total weekly income must equal 43872.0", 43872.0, result.calculatedWeeklyIncome, 0.001)
        assertEquals("Discrepancy must be 0", 0.0, result.discrepancyAmount, 0.001)
        assertTrue("Report must be matched", result.isMatched)
        assertEquals("Status must be VALIDATED", ReportStatus.VALIDATED, result.status)
    }

    @Test
    fun testWeeklyDiscrepancyMarksPendingVerification() {
        // If reported weekly income is 12000, but subcategories add up to 11110
        val result = AccountingCalculations.reconcileWeeklyReport(
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
            reportedWeeklyIncome = 12000.0
        )

        assertFalse("Report must not be matched", result.isMatched)
        assertEquals("Discrepancy must be 890.0", 890.0, result.discrepancyAmount, 0.001)
        assertEquals("Status must be PENDING_VERIFICATION", ReportStatus.PENDING_VERIFICATION, result.status)
        assertTrue("Discrepancy message must mention mismatch", result.discrepancyMessageBangla.contains("অমিল সনাক্ত"))
    }

    @Test
    fun testCostRecoveryCalculationsAndEdgeCases() {
        // 1. Normal Case
        val normal = AccountingCalculations.calculateCostRecovery(totalIncome = 15000.0, totalExpense = 10000.0)
        assertTrue(normal.isReliable)
        assertNotNull(normal.percentage)
        assertEquals(150.0, normal.percentage!!, 0.01)

        // 2. Zero Expense with Positive Income
        val zeroExpensePositiveIncome = AccountingCalculations.calculateCostRecovery(totalIncome = 5000.0, totalExpense = 0.0)
        assertFalse(zeroExpensePositiveIncome.isReliable)
        assertNull(zeroExpensePositiveIncome.percentage)
        assertTrue(zeroExpensePositiveIncome.formattedDisplayEnglish.contains("cannot calculate reliably"))
        assertTrue(zeroExpensePositiveIncome.formattedDisplayBangla.contains("নির্ভরযোগ্যভাবে গণনা সম্ভব নয়"))

        // 3. Zero Expense and Zero Income
        val zeroExpenseZeroIncome = AccountingCalculations.calculateCostRecovery(totalIncome = 0.0, totalExpense = 0.0)
        assertFalse(zeroExpenseZeroIncome.isReliable)
        assertNull(zeroExpenseZeroIncome.percentage)
        assertEquals("No financial activity", zeroExpenseZeroIncome.formattedDisplayEnglish)
        assertEquals("কোনো আর্থিক লেনদেন নেই", zeroExpenseZeroIncome.formattedDisplayBangla)
    }

    @Test
    fun testTargetSheetCalculations() {
        val centre = HealthCentre("gk-c01", "মালখানগর", "Malkhanagar", "MKH")
        val row = AccountingCalculations.calculateTargetRow(
            centre = centre,
            monthlyWorkingDays = 26,
            dailyIncomeTarget = 10000.0,
            manualMonthlyTarget = null,
            currentDayIncome = 12000.0,
            monthToDateIncome = 130000.0,
            remainingWorkingDays = 13
        )

        // D = B * C = 26 * 10,000 = 260,000
        assertEquals(260000.0, row.monthlyIncomeTarget, 0.001)
        // G = max(D - F, 0) = 260,000 - 130,000 = 130,000
        assertEquals(130000.0, row.incomeShortfall, 0.001)
        // H = (F / D) * 100 = 50.0%
        assertEquals(50.0, row.achievementPercentage, 0.001)
        // I = shortfall / remaining days = 130,000 / 13 = 10,000
        assertEquals(10000.0, row.requiredDailyIncome, 0.001)
    }

    @Test
    fun testRoleBasedAccessPermissions() {
        assertTrue(UserRole.ADMIN.canManageUsersAndCentres())
        assertTrue(UserRole.ADMIN.canEditApprovedData())
        assertTrue(UserRole.ADMIN.canPerformCorrections())
        assertTrue(UserRole.ADMIN.canViewAllCentres())

        assertFalse(UserRole.RM.canEditApprovedData())
        assertFalse(UserRole.RM.canManageUsersAndCentres())
        assertTrue(UserRole.RM.canViewAllCentres())

        assertTrue(UserRole.ACCOUNTS.canPerformCorrections())
        assertTrue(UserRole.ACCOUNTS.canVerifyReports())
        assertFalse(UserRole.ACCOUNTS.canManageUsersAndCentres())

        assertFalse(UserRole.CENTRE_USER.canViewAllCentres())
        assertFalse(UserRole.CENTRE_USER.canPerformCorrections())
    }

    @Test
    fun testBanglaDigitFormatting() {
        val english = "12345.67"
        val bangla = BanglaFormatter.toBanglaDigits(english)
        assertEquals("১২৩৪৫.৬৭", bangla)

        val formattedCurrency = BanglaFormatter.formatCurrencyBdt(11110.0)
        assertTrue("Formatted currency should contain BDT symbol", formattedCurrency.startsWith("৳"))
        assertTrue("Formatted currency should contain Bangla digits", formattedCurrency.contains("১১,১১০"))
    }
}

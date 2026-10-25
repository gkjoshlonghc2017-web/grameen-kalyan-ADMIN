package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.FinancialMetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun RegionalDashboardScreen(
    viewModel: AccountingViewModel,
    onNavigateToCentre: (String) -> Unit,
    onNavigateToPending: () -> Unit,
    onNavigateToSpreadsheet: () -> Unit
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val centres by viewModel.allCentres.collectAsState()
    val dailyReports by viewModel.allDailyReports.collectAsState()
    val pendingWeekly by viewModel.pendingWeeklyReports.collectAsState()
    val targetRows by viewModel.targetSheetRows.collectAsState()

    // Monthly aggregates from validated daily reports for this month
    val currentMonthReports = remember(dailyReports, selectedMonth) {
        dailyReports.filter { it.reportDate.startsWith(selectedMonth) && it.status == ReportStatus.VALIDATED }
    }

    val totalIncome = remember(currentMonthReports) {
        currentMonthReports.sumOf { it.totalDailyIncome }
    }
    val totalExpense = remember(currentMonthReports) {
        currentMonthReports.sumOf { it.totalDailyExpense }
    }
    val totalCentrePatients = remember(currentMonthReports) {
        currentMonthReports.sumOf { it.centrePatients }
    }
    val totalSatellitePatients = remember(currentMonthReports) {
        currentMonthReports.sumOf { it.satellitePatients }
    }
    val totalCampPatients = remember(currentMonthReports) {
        currentMonthReports.sumOf { it.campPatients }
    }
    val totalPatients = totalCentrePatients + totalSatellitePatients + totalCampPatients

    val costRecovery = remember(totalIncome, totalExpense) {
        AccountingCalculations.calculateCostRecovery(totalIncome, totalExpense)
    }

    // Target aggregates
    val totalMonthlyTarget = remember(targetRows) {
        targetRows.sumOf { it.monthlyIncomeTarget }
    }
    val overallAchievement = if (totalMonthlyTarget > 0) {
        (totalIncome / totalMonthlyTarget) * 100
    } else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Hero Title Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "কেরানীগঞ্জ অঞ্চল সারসংক্ষেপ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "মাস: ${BanglaFormatter.toBanglaDigits(selectedMonth)} • ১১টি স্বাস্থ্য কেন্দ্র",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "কস্ট রিকভারি হার (Cost Recovery): ${costRecovery.formattedDisplayBangla}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (costRecovery.isReliable) GkStatusValidated else GkStatusPending
                        )
                    )
                }
            }
        }

        // Pending verification alert banner if any
        if (pendingWeekly.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GkStatusPendingBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPending() }
                        .testTag("pending_alert_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = GkStatusPending,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "হিসাবে অমিল: ${BanglaFormatter.toBanglaDigits(pendingWeekly.size)}টি রিপোর্ট যাচাইকরণ অপেক্ষমাণ",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = GkStatusPending
                            )
                            Text(
                                text = "ক্লিক করে বিস্তারিত পর্যালোচনা ও সংশোধন করুন",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = GkTextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = GkStatusPending
                        )
                    }
                }
            }
        }

        // Financial Metrics Row 1
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialMetricCard(
                    titleBangla = "মোট আয়",
                    titleEnglish = "Total Income",
                    value = BanglaFormatter.formatCurrencyBdt(totalIncome),
                    icon = Icons.Default.AccountBalanceWallet,
                    containerColor = Color.White,
                    accentColor = GkGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                FinancialMetricCard(
                    titleBangla = "মোট ব্যয় (অবচয়সহ)",
                    titleEnglish = "Total Expense",
                    value = BanglaFormatter.formatCurrencyBdt(totalExpense),
                    icon = Icons.Default.ReceiptLong,
                    containerColor = Color.White,
                    accentColor = Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Patients breakdown card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "সেবাপ্রাপ্ত রোগী সারসংক্ষেপ (Patient Statistics)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PatientStatColumn("কেন্দ্র রোগী", totalCentrePatients, Icons.Default.LocalHospital)
                        PatientStatColumn("স্যাটেলাইট", totalSatellitePatients, Icons.Default.Commute)
                        PatientStatColumn("ক্যাম্প রোগী", totalCampPatients, Icons.Default.Campaign)
                        PatientStatColumn("সর্বমোট রোগী", totalPatients, Icons.Default.People, isHighlight = true)
                    }
                }
            }
        }

        // Target Achievement Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "আঞ্চলিক লক্ষ্যমাত্রা অগ্রগতি",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(onClick = onNavigateToSpreadsheet) {
                            Text("স্প্রেডশীট ভিউ", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    LinearProgressIndicator(
                        progress = { (overallAchievement / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GkGoldSecondary,
                        trackColor = GkGoldContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "অর্জিত: ${BanglaFormatter.formatCurrencyBdt(totalIncome)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GkTextSecondary
                        )
                        Text(
                            text = "লক্ষ্য: ${BanglaFormatter.formatCurrencyBdt(totalMonthlyTarget)} (${BanglaFormatter.formatPercentage(overallAchievement)})",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = GkGreenPrimary
                        )
                    }
                }
            }
        }

        // 11 Health Centres Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "১১টি স্বাস্থ্য কেন্দ্র (Health Centres)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "বিস্তারিত দেখতে ট্যাপ করুন",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = GkTextTertiary
                )
            }
        }

        // List of 11 Centres
        items(centres) { centre ->
            val centreReports = currentMonthReports.filter { it.centreId == centre.id }
            val cIncome = centreReports.sumOf { it.totalDailyIncome }
            val cExpense = centreReports.sumOf { it.totalDailyExpense }
            val cPatients = centreReports.sumOf { it.totalPatients }
            val cr = AccountingCalculations.calculateCostRecovery(cIncome, cExpense)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCentre(centre.id) }
                    .testTag("centre_card_${centre.id}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GkGreenPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = centre.code,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GkGreenPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = centre.nameBangla,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${centre.nameEnglish} • আইডি: ${centre.id}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = GkTextTertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "আয়: ${BanglaFormatter.formatCurrencyBdt(cIncome)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = GkGreenPrimary
                            )
                            Text(
                                text = "ব্যয়: ${BanglaFormatter.formatCurrencyBdt(cExpense)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GkTextSecondary
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = if (cr.isReliable) GkGreenContainer else GkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = cr.formattedDisplayBangla,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (cr.isReliable) GkGreenPrimary else GkTextSecondary
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "রোগী: ${BanglaFormatter.toBanglaDigits(cPatients)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = GkTextSecondary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PatientStatColumn(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isHighlight: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isHighlight) GkGoldSecondary else GkGreenPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = BanglaFormatter.toBanglaDigits(count),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) GkGoldDark else MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = GkTextTertiary
        )
    }
}

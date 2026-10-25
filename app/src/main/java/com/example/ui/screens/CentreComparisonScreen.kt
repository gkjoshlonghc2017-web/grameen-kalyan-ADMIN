package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun CentreComparisonScreen(
    viewModel: AccountingViewModel,
    onNavigateToCentre: (String) -> Unit
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val targetRows by viewModel.targetSheetRows.collectAsState()
    val dailyReports by viewModel.allDailyReports.collectAsState()

    var sortBy by remember { mutableStateOf("ACHIEVEMENT") } // ACHIEVEMENT, INCOME, RECOVERY

    val sortedCentres = remember(targetRows, dailyReports, sortBy, selectedMonth) {
        val currentMonthReports = dailyReports.filter { it.reportDate.startsWith(selectedMonth) && it.status == ReportStatus.VALIDATED }

        val items = targetRows.map { row ->
            val centreReports = currentMonthReports.filter { it.centreId == row.centreId }
            val expense = centreReports.sumOf { it.totalDailyExpense }
            val patients = centreReports.sumOf { it.totalPatients }
            val cr = AccountingCalculations.calculateCostRecovery(row.monthToDateIncome, expense)

            object {
                val row = row
                val expense = expense
                val patients = patients
                val costRecovery = cr
            }
        }

        when (sortBy) {
            "INCOME" -> items.sortedByDescending { it.row.monthToDateIncome }
            "RECOVERY" -> items.sortedByDescending { it.costRecovery.percentage ?: 0.0 }
            else -> items.sortedByDescending { it.row.achievementPercentage }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "১১টি স্বাস্থ্য কেন্দ্রের তুলনামূলক পর্যালোচনা",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "মাস: ${BanglaFormatter.toBanglaDigits(selectedMonth)} • পারফরম্যান্স র্যাংকিং ও লক্ষ্য অর্জনের হার",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Sort Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = sortBy == "ACHIEVEMENT",
                onClick = { sortBy = "ACHIEVEMENT" },
                label = { Text("লক্ষ্য অর্জন %") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
            )
            FilterChip(
                selected = sortBy == "INCOME",
                onClick = { sortBy = "INCOME" },
                label = { Text("মোট আয়") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
            )
            FilterChip(
                selected = sortBy == "RECOVERY",
                onClick = { sortBy = "RECOVERY" },
                label = { Text("কস্ট রিকভারি %") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(sortedCentres) { index, item ->
                val rank = index + 1
                val row = item.row
                val badgeColor = when (rank) {
                    1 -> GkGoldSecondary
                    2 -> Color(0xFF78909C)
                    3 -> Color(0xFFB07246)
                    else -> GkGreenPrimary.copy(alpha = 0.15f)
                }
                val badgeTextColor = if (rank <= 3) Color.White else GkGreenPrimary

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${BanglaFormatter.toBanglaDigits(rank)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = badgeTextColor
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${row.centreNameBangla} (${row.centreNameEnglish})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "সেবাপ্রাপ্ত রোগী: ${BanglaFormatter.toBanglaDigits(item.patients)} জন",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = GkTextTertiary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = BanglaFormatter.formatPercentage(row.achievementPercentage),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (row.achievementPercentage >= 100.0) GkGreenPrimary else GkGoldDark
                                    )
                                )
                                Text(
                                    text = "রিকভারি: ${item.costRecovery.formattedDisplayBangla}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = GkTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (row.achievementPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = GkGreenPrimary,
                            trackColor = GkGreenContainer
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "অর্জিত: ${BanglaFormatter.formatCurrencyBdt(row.monthToDateIncome)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                            Text(
                                text = "লক্ষ্য: ${BanglaFormatter.formatCurrencyBdt(row.monthlyIncomeTarget)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                            Text(
                                text = "ব্যয়: ${BanglaFormatter.formatCurrencyBdt(item.expense)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

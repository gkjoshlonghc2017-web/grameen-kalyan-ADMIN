package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BanglaFormatter
import com.example.model.TargetSheetRow
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun MonthlyReportsScreen(
    viewModel: AccountingViewModel,
    onNavigateToExport: () -> Unit
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val rows by viewModel.targetSheetRows.collectAsState()

    // Totals
    val totalWorkingDays = rows.map { it.monthlyWorkingDays }.average().toInt()
    val totalDailyTarget = rows.sumOf { it.dailyIncomeTarget }
    val totalMonthlyTarget = rows.sumOf { it.monthlyIncomeTarget }
    val totalCurrentDay = rows.sumOf { it.currentDayIncome }
    val totalMtdIncome = rows.sumOf { it.monthToDateIncome }
    val totalShortfall = rows.sumOf { it.incomeShortfall }
    val overallAchievement = if (totalMonthlyTarget > 0) (totalMtdIncome / totalMonthlyTarget) * 100 else 0.0
    val totalRequiredDaily = rows.sumOf { it.requiredDailyIncome }

    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Card with Month selector & Export button
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "মাসিক হিসাব ও স্প্রেডশীট (Target Sheet)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "মাস: ${BanglaFormatter.toBanglaDigits(selectedMonth)} • কলাম A হতে J অফিশিয়াল বিবরণী",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                FilledTonalButton(
                    onClick = onNavigateToExport,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = GkGoldContainer),
                    modifier = Modifier.testTag("export_from_spreadsheet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = GkOnGoldContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("এক্সপোর্ট", fontSize = 12.sp, color = GkOnGoldContainer)
                }
            }
        }

        // Horizontal scrolling table
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                LazyColumn(modifier = Modifier.width(1080.dp)) {
                    // Table Header Row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GkGreenPrimary)
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HeaderCell("A. স্বাস্থ্য কেন্দ্র", 140.dp)
                            HeaderCell("B. কার্যদিবস", 80.dp)
                            HeaderCell("C. দৈনিক লক্ষ্য", 100.dp)
                            HeaderCell("D. মাসিক লক্ষ্য", 110.dp)
                            HeaderCell("E. আজকের আয়", 100.dp)
                            HeaderCell("F. অর্জিত আয় (MTD)", 110.dp)
                            HeaderCell("G. ঘাটতি", 100.dp)
                            HeaderCell("H. অর্জনের %", 90.dp)
                            HeaderCell("J. বাকি দিন", 80.dp)
                            HeaderCell("I. প্রয়োজনীয় গড়", 110.dp)
                        }
                    }

                    // Table Body Rows
                    itemsIndexed(rows) { index, row ->
                        val isEven = index % 2 == 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isEven) Color.White else GkSurfaceVariant)
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DataCell(row.centreNameBangla, 140.dp, isBold = true, color = GkGreenPrimary)
                            DataCell(BanglaFormatter.toBanglaDigits(row.monthlyWorkingDays), 80.dp)
                            DataCell(BanglaFormatter.formatCurrencyBdt(row.dailyIncomeTarget), 100.dp)
                            DataCell(BanglaFormatter.formatCurrencyBdt(row.monthlyIncomeTarget), 110.dp, isBold = true)
                            DataCell(BanglaFormatter.formatCurrencyBdt(row.currentDayIncome), 100.dp)
                            DataCell(BanglaFormatter.formatCurrencyBdt(row.monthToDateIncome), 110.dp, isBold = true, color = GkGreenPrimary)
                            DataCell(
                                BanglaFormatter.formatCurrencyBdt(row.incomeShortfall),
                                100.dp,
                                color = if (row.incomeShortfall > 0) Color(0xFFC62828) else GkGreenPrimary
                            )
                            DataCell(
                                BanglaFormatter.formatPercentage(row.achievementPercentage),
                                90.dp,
                                isBold = true,
                                color = if (row.achievementPercentage >= 100.0) GkGreenPrimary else GkGoldDark
                            )
                            DataCell(BanglaFormatter.toBanglaDigits(row.remainingWorkingDays), 80.dp)
                            DataCell(BanglaFormatter.formatCurrencyBdt(row.requiredDailyIncome), 110.dp, isBold = true)
                        }
                    }

                    // Table Summary Footer
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GkGoldContainer)
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DataCell("সর্বমোট (অঞ্চল)", 140.dp, isBold = true, color = GkOnGoldContainer)
                            DataCell("গড় ${BanglaFormatter.toBanglaDigits(totalWorkingDays)}", 80.dp, isBold = true)
                            DataCell(BanglaFormatter.formatCurrencyBdt(totalDailyTarget), 100.dp, isBold = true)
                            DataCell(BanglaFormatter.formatCurrencyBdt(totalMonthlyTarget), 110.dp, isBold = true)
                            DataCell(BanglaFormatter.formatCurrencyBdt(totalCurrentDay), 100.dp, isBold = true)
                            DataCell(BanglaFormatter.formatCurrencyBdt(totalMtdIncome), 110.dp, isBold = true, color = GkGreenPrimary)
                            DataCell(BanglaFormatter.formatCurrencyBdt(totalShortfall), 100.dp, isBold = true, color = Color(0xFFC62828))
                            DataCell(BanglaFormatter.formatPercentage(overallAchievement), 90.dp, isBold = true, color = GkGoldDark)
                            DataCell("-", 80.dp, isBold = true)
                            DataCell(BanglaFormatter.formatCurrencyBdt(totalRequiredDaily), 110.dp, isBold = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun DataCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isBold: Boolean = false,
    color: Color = GkTextPrimary
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color,
            fontSize = 11.sp
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width)
    )
}

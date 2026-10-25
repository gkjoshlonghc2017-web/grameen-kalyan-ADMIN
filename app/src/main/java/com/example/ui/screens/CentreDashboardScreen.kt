package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentreDashboardScreen(
    viewModel: AccountingViewModel,
    onNavigateToDailyForm: (String) -> Unit,
    onNavigateToWeeklyForm: (String) -> Unit,
    onNavigateToSms: (String) -> Unit
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val centres by viewModel.allCentres.collectAsState()
    val selectedCentreId by viewModel.selectedCentreId.collectAsState()
    val dailyReports by viewModel.allDailyReports.collectAsState()
    val weeklyReports by viewModel.allWeeklyReports.collectAsState()
    val targetRows by viewModel.targetSheetRows.collectAsState()

    val currentCentre = remember(centres, selectedCentreId) {
        centres.find { it.id == selectedCentreId } ?: centres.firstOrNull() ?: HealthCentre("gk-c01", "মালখানগর", "Malkhanagar", "MKH")
    }

    val centreDailyReports = remember(dailyReports, currentCentre, selectedMonth) {
        dailyReports.filter { it.centreId == currentCentre.id && it.reportDate.startsWith(selectedMonth) }
    }

    val centreWeeklyReports = remember(weeklyReports, currentCentre) {
        weeklyReports.filter { it.centreId == currentCentre.id }
    }

    val mtdIncome = remember(centreDailyReports) {
        centreDailyReports.filter { it.status == ReportStatus.VALIDATED }.sumOf { it.totalDailyIncome }
    }
    val mtdExpense = remember(centreDailyReports) {
        centreDailyReports.filter { it.status == ReportStatus.VALIDATED }.sumOf { it.totalDailyExpense }
    }

    val costRecovery = remember(mtdIncome, mtdExpense) {
        AccountingCalculations.calculateCostRecovery(mtdIncome, mtdExpense)
    }

    val targetRow = remember(targetRows, currentCentre) {
        targetRows.find { it.centreId == currentCentre.id }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Centre Horizontal Selector Chips
            Text(
                text = "স্বাস্থ্য কেন্দ্র নির্বাচন করুন:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(centres) { c ->
                    val isSelected = c.id == currentCentre.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCentre(c.id) },
                        label = {
                            Text(
                                text = c.nameBangla,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("select_centre_chip_${c.id}")
                    )
                }
            }
        }

        // Centre Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GkGoldContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GkGoldSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentCentre.code,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentCentre.nameBangla,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = GkOnGoldContainer
                            )
                            Text(
                                text = "${currentCentre.nameEnglish} • ইউনিক আইডি: ${currentCentre.id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GkOnGoldContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToDailyForm(currentCentre.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = GkGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("centre_daily_btn")
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("দৈনিক রিপোর্ট", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onNavigateToWeeklyForm(currentCentre.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = GkTealAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("centre_weekly_btn")
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("সাপ্তাহিক রিপোর্ট", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Financial Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialMetricCard(
                    titleBangla = "মাসিক আয় (MTD)",
                    titleEnglish = "Month-to-Date Income",
                    value = BanglaFormatter.formatCurrencyBdt(mtdIncome),
                    icon = Icons.Default.TrendingUp,
                    containerColor = Color.White,
                    accentColor = GkGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                FinancialMetricCard(
                    titleBangla = "মাসিক ব্যয় (অবচয়সহ)",
                    titleEnglish = "MTD Expense",
                    value = BanglaFormatter.formatCurrencyBdt(mtdExpense),
                    icon = Icons.Default.AccountBalance,
                    containerColor = Color.White,
                    accentColor = Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Cost recovery & Target details
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "মাসিক লক্ষ্যমাত্রা ও রিকভারি স্ট্যাটাস",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "কস্ট রিকভারি (Cost Recovery):",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = costRecovery.formattedDisplayBangla,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (costRecovery.isReliable) GkGreenPrimary else GkStatusPending
                            )
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = GkOutlineLight)

                    if (targetRow != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "মাসিক লক্ষ্যমাত্রা:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = BanglaFormatter.formatCurrencyBdt(targetRow.monthlyIncomeTarget),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "অর্জিত অগ্রগতি:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${BanglaFormatter.formatPercentage(targetRow.achievementPercentage)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GkGreenPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "ঘাটতি (Shortfall):", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = BanglaFormatter.formatCurrencyBdt(targetRow.incomeShortfall),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (targetRow.incomeShortfall > 0) Color(0xFFC62828) else GkGreenPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "অবশিষ্ট দিনসমূহে প্রয়োজনীয় দৈনিক গড়:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = BanglaFormatter.formatCurrencyBdt(targetRow.requiredDailyIncome),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GkGoldDark
                                )
                            )
                        }
                    }
                }
            }
        }

        // Recent Daily Reports Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সাম্প্রতিক দৈনিক রিপোর্টসমূহ (${centreDailyReports.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { onNavigateToSms(currentCentre.id) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "এসএমএস শেয়ার",
                        tint = GkGreenPrimary
                    )
                }
            }
        }

        if (centreDailyReports.isEmpty()) {
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "এই মাসের কোনো দৈনিক রিপোর্ট পাওয়া যায়নি। উপরের 'দৈনিক রিপোর্ট' বোতামে চাপুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = GkTextTertiary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(centreDailyReports) { r ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "তারিখ: ${BanglaFormatter.toBanglaDigits(r.reportDate)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            StatusBadge(r.status)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "আয়: ${BanglaFormatter.formatCurrencyBdt(r.totalDailyIncome)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GkGreenPrimary
                                )
                            )
                            Text(
                                text = "ব্যয়: ${BanglaFormatter.formatCurrencyBdt(r.totalDailyExpense)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "মোট রোগী: ${BanglaFormatter.toBanglaDigits(r.totalPatients)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (r.warningNote != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = r.warningNote,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = GkStatusPending
                            )
                        }
                    }
                }
            }
        }

        // Weekly Reports Section
        item {
            Text(
                text = "সাপ্তাহিক রিকনসিলিয়েশন রিপোর্ট (${centreWeeklyReports.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(centreWeeklyReports) { w ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মেয়াদ: ${BanglaFormatter.toBanglaDigits(w.periodStartDate)} - ${BanglaFormatter.toBanglaDigits(w.periodEndDate)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        StatusBadge(w.status)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "দাখিলকৃত আয়: ${BanglaFormatter.formatCurrencyBdt(w.reportedWeeklyIncome)} | গণনাকৃত: ${BanglaFormatter.formatCurrencyBdt(w.calculatedWeeklyIncome)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "সাপ্তাহিক ব্যয়: ${BanglaFormatter.formatCurrencyBdt(w.totalWeeklyExpense)} (রিকনসিলিয়েশন)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = GkTextSecondary
                    )
                    if (w.discrepancyAmount != 0.0) {
                        Text(
                            text = "ব্যবধান: ${BanglaFormatter.formatCurrencyBdt(w.discrepancyAmount)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GkStatusPending
                            )
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

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun ReportHistoryScreen(
    viewModel: AccountingViewModel
) {
    val centres by viewModel.allCentres.collectAsState()
    val dailyReports by viewModel.allDailyReports.collectAsState()
    val weeklyReports by viewModel.allWeeklyReports.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    var filterType by remember { mutableStateOf("ALL") } // ALL, DAILY, WEEKLY
    var filterCentreId by remember { mutableStateOf<String?>(null) }

    var selectedDailyDetail by remember { mutableStateOf<DailyReport?>(null) }
    var selectedWeeklyDetail by remember { mutableStateOf<WeeklyReport?>(null) }

    val filteredDaily = remember(dailyReports, filterType, filterCentreId, selectedMonth) {
        if (filterType == "WEEKLY") emptyList()
        else dailyReports.filter {
            (filterCentreId == null || it.centreId == filterCentreId) &&
            it.reportDate.startsWith(selectedMonth)
        }
    }

    val filteredWeekly = remember(weeklyReports, filterType, filterCentreId) {
        if (filterType == "DAILY") emptyList()
        else weeklyReports.filter {
            filterCentreId == null || it.centreId == filterCentreId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == "ALL",
                onClick = { filterType = "ALL" },
                label = { Text("সকল (${filteredDaily.size + filteredWeekly.size})") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
            )
            FilterChip(
                selected = filterType == "DAILY",
                onClick = { filterType = "DAILY" },
                label = { Text("দৈনিক (${filteredDaily.size})") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
            )
            FilterChip(
                selected = filterType == "WEEKLY",
                onClick = { filterType = "WEEKLY" },
                label = { Text("সাপ্তাহিক (${filteredWeekly.size})") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
            )
        }

        // Centre Filter Scroll
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = filterCentreId == null,
                    onClick = { filterCentreId = null },
                    label = { Text("সকল কেন্দ্র") }
                )
            }
            items(centres) { c ->
                FilterChip(
                    selected = filterCentreId == c.id,
                    onClick = { filterCentreId = if (filterCentreId == c.id) null else c.id },
                    label = { Text(c.nameBangla) }
                )
            }
        }

        // List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Daily Items
            items(filteredDaily) { d ->
                val centre = centres.find { it.id == d.centreId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedDailyDetail = d }
                        .testTag("report_daily_${d.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = GkGreenContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "দৈনিক",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GkGreenPrimary),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${centre?.nameBangla ?: d.centreId} • ${BanglaFormatter.toBanglaDigits(d.reportDate)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            StatusBadge(d.status)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "আয়: ${BanglaFormatter.formatCurrencyBdt(d.totalDailyIncome)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = GkGreenPrimary)
                            )
                            Text(
                                text = "ব্যয়: ${BanglaFormatter.formatCurrencyBdt(d.totalDailyExpense)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "রোগী: ${BanglaFormatter.toBanglaDigits(d.totalPatients)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Weekly Items
            items(filteredWeekly) { w ->
                val centre = centres.find { it.id == w.centreId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedWeeklyDetail = w }
                        .testTag("report_weekly_${w.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = GkGoldContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "সাপ্তাহিক",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GkOnGoldContainer),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${centre?.nameBangla ?: w.centreId} (${w.periodStartDate} - ${w.periodEndDate})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            StatusBadge(w.status)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "আয়: ${BanglaFormatter.formatCurrencyBdt(w.reportedWeeklyIncome)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = GkGreenPrimary)
                            )
                            Text(
                                text = "ব্যয়: ${BanglaFormatter.formatCurrencyBdt(w.totalWeeklyExpense)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = if (w.discrepancyAmount == 0.0) "হিসাব সমন্বিত" else "ব্যবধান: ${BanglaFormatter.formatCurrencyBdt(w.discrepancyAmount)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (w.discrepancyAmount == 0.0) GkGreenPrimary else GkStatusPending
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Daily Details Dialog
    if (selectedDailyDetail != null) {
        val d = selectedDailyDetail!!
        AlertDialog(
            onDismissRequest = { selectedDailyDetail = null },
            title = { Text("দৈনিক রিপোর্টের বিস্তারিত") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("কেন্দ্র: ${d.centreId} | তারিখ: ${d.reportDate}", fontWeight = FontWeight.Bold)
                    Text("দাখিলকারী: ${d.submitterName}")
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("১. কেন্দ্র রোগী: ${d.centrePatients}")
                    Text("২. স্যাটেলাইট রোগী: ${d.satellitePatients}")
                    Text("৩. ক্যাম্প রোগী: ${d.campPatients}")
                    Text("৪. মোট দৈনিক আয়: ৳${d.totalDailyIncome}", fontWeight = FontWeight.Bold, color = GkGreenPrimary)
                    Text("৫. ঔষধ ও ল্যাব খরচ: ৳${d.medicineLabCost}")
                    Text("৬. স্যাটেলাইট সেশন: ${d.satelliteSessions}")
                    Text("৭. কার্ড সংখ্যা: ${d.cardsCount}")
                    Text("৮. মোট দৈনিক ব্যয়: ৳${d.totalDailyExpense}", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    if (d.warningNote != null) {
                        Text(d.warningNote, color = GkStatusPending, fontSize = 11.sp)
                    }
                    if (d.notes.isNotBlank()) {
                        Text("নোট: ${d.notes}")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedDailyDetail = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // Weekly Details Dialog
    if (selectedWeeklyDetail != null) {
        val w = selectedWeeklyDetail!!
        AlertDialog(
            onDismissRequest = { selectedWeeklyDetail = null },
            title = { Text("সাপ্তাহিক রিপোর্টের বিস্তারিত") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("কেন্দ্র: ${w.centreId} | মেয়াদ: ${w.periodStartDate} হতে ${w.periodEndDate}", fontWeight = FontWeight.Bold)
                    Text("দাখিলকারী: ${w.submitterName}")
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("কেন্দ্র আয় উপখাত যোগফল: ৳${w.calculatedCentreIncome}")
                    Text("ক্যাম্প আয় উপখাত যোগফল: ৳${w.calculatedCampIncome}")
                    Text("সর্বমোট গণনাকৃত আয়: ৳${w.calculatedWeeklyIncome}", fontWeight = FontWeight.Bold)
                    Text("দাখিলকৃত মোট আয়: ৳${w.reportedWeeklyIncome}", fontWeight = FontWeight.Bold, color = GkGreenPrimary)
                    Text("সাপ্তাহিক ব্যয়: ৳${w.totalWeeklyExpense}")
                    Text("ব্যবধান: ৳${w.discrepancyAmount}")
                    Text("অবস্থা: ${w.status.titleBangla}")
                    if (w.resolutionReason != null) {
                        Text("অনুমোদনের কারণ: ${w.resolutionReason}", color = GkGreenPrimary)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedWeeklyDetail = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}

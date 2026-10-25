package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BanglaFormatter
import com.example.model.WeeklyReport
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun PendingVerificationScreen(
    viewModel: AccountingViewModel
) {
    val pendingList by viewModel.pendingWeeklyReports.collectAsState()
    val centres by viewModel.allCentres.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedReportForResolution by remember { mutableStateOf<WeeklyReport?>(null) }
    var correctedIncomeStr by remember { mutableStateOf("") }
    var resolutionReason by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title banner
        Card(
            colors = CardDefaults.cardColors(containerColor = GkStatusPendingBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PendingActions, contentDescription = null, tint = GkStatusPending)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "যাচাইকরণ অপেক্ষমাণ তালিকা (Pending Verification)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GkStatusPending
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "উপখাতসমূহের যোগফল ও মোট আয়ের মধ্যে অমিল থাকা রিপোর্টসমূহ এখানে প্রদর্শিত হচ্ছে। শুধুমাত্র এডমিন ও হিসাবরক্ষক অডিট ট্রেইল সহ এটি সমন্বয় করতে পারবেন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = GkTextSecondary
                )
            }
        }

        if (pendingList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GkStatusValidated,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "কোনো যাচাইকরণ অপেক্ষমাণ রিপোর্ট নেই!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GkGreenPrimary
                    )
                    Text(
                        text = "সকল স্বাস্থ্য কেন্দ্রের উপখাত ও মোট আয় শতভাগ সমন্বিত রয়েছে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = GkTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingList) { report ->
                    val centre = centres.find { it.id == report.centreId }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("pending_item_${report.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${centre?.nameBangla ?: report.centreId} (${centre?.code ?: ""})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                StatusBadge(report.status)
                            }
                            Text(
                                text = "মেয়াদ: ${report.periodStartDate} হতে ${report.periodEndDate} • দাখিলকারী: ${report.submitterName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GkTextTertiary
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = GkOutlineLight)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "দাখিলকৃত আয়:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = BanglaFormatter.formatCurrencyBdt(report.reportedWeeklyIncome),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Column {
                                    Text(text = "উপখাত যোগফল:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = BanglaFormatter.formatCurrencyBdt(report.calculatedWeeklyIncome),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GkGreenPrimary
                                        )
                                    )
                                }
                                Column {
                                    Text(text = "ব্যবধান (Discrepancy):", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = BanglaFormatter.formatCurrencyBdt(report.discrepancyAmount),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GkStatusPending
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = GkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "উপখাত সারসংক্ষেপ: কেন্দ্র আয় = ${BanglaFormatter.formatCurrencyBdt(report.calculatedCentreIncome)} | ক্যাম্প আয় = ${BanglaFormatter.formatCurrencyBdt(report.calculatedCampIncome)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = GkTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            if (currentUser.role.canPerformCorrections()) {
                                Button(
                                    onClick = {
                                        selectedReportForResolution = report
                                        // Default to calculated sum so it easily resolves if mistake was in total
                                        correctedIncomeStr = report.calculatedWeeklyIncome.toString()
                                        resolutionReason = "উপখাতসমূহের ভাউচার নিরীক্ষান্তে সঠিক যোগফল গ্রহণ করা হলো।"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GkGoldSecondary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("resolve_btn_${report.id}")
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("সংশোধন ও অনুমোদন করুন (Reconcile & Approve)")
                                }
                            } else {
                                Text(
                                    text = "শুধুমাত্র এডমিন বা হিসাবরক্ষক এটি অনুমোদন করতে পারেন।",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = GkTextTertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Resolution Dialog
    if (selectedReportForResolution != null) {
        val report = selectedReportForResolution!!
        AlertDialog(
            onDismissRequest = { selectedReportForResolution = null },
            title = { Text("হিসাব সমন্বয় ও অনুমোদন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "পূর্বের দাখিলকৃত আয়: ${BanglaFormatter.formatCurrencyBdt(report.reportedWeeklyIncome)}\nউপখাতসমূহের যোগফল: ${BanglaFormatter.formatCurrencyBdt(report.calculatedWeeklyIncome)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = correctedIncomeStr,
                        onValueChange = { correctedIncomeStr = it },
                        label = { Text("সংশোধিত মোট সাপ্তাহিক আয় (BDT ৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("corrected_income_input")
                    )
                    OutlinedTextField(
                        value = resolutionReason,
                        onValueChange = { resolutionReason = it },
                        label = { Text("সংশোধনের সুনির্দিষ্ট কারণ (অডিট ট্রেইলের জন্য আবশ্যক)") },
                        modifier = Modifier.fillMaxWidth().testTag("correction_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val correctedVal = correctedIncomeStr.toDoubleOrNull() ?: 0.0
                        viewModel.resolveWeeklyDiscrepancy(
                            reportId = report.id,
                            correctedIncome = correctedVal,
                            reason = resolutionReason.trim()
                        )
                        selectedReportForResolution = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GkGreenPrimary),
                    modifier = Modifier.testTag("confirm_resolution_btn")
                ) {
                    Text("অনুমোদন করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedReportForResolution = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

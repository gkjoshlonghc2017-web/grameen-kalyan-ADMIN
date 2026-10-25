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
import com.example.model.TargetSheetRow
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun TargetManagementScreen(
    viewModel: AccountingViewModel
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val targetRows by viewModel.targetSheetRows.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var editingRow by remember { mutableStateOf<TargetSheetRow?>(null) }
    var workingDaysStr by remember { mutableStateOf("") }
    var dailyTargetStr by remember { mutableStateOf("") }
    var manualMonthlyStr by remember { mutableStateOf("") }
    var remainingDaysStr by remember { mutableStateOf("") }

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
                    text = "লক্ষ্যমাত্রা ও কার্যদিবস ব্যবস্থাপনা (Target Sheet)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "মাস: ${BanglaFormatter.toBanglaDigits(selectedMonth)} • ১১টি স্বাস্থ্য কেন্দ্রের দৈনিক ও মাসিক লক্ষ্য নির্ধারণ করুন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        if (!currentUser.role.canManageUsersAndCentres()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GkStatusPendingBg),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "নোট: আপনি শুধুমাত্র দেখতে পারবেন। লক্ষ্যমাত্রা পরিবর্তন করতে এডমিন লগইন প্রয়োজন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = GkStatusPending,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(targetRows) { row ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("target_item_${row.centreId}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${row.centreNameBangla} (${row.centreNameEnglish})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (currentUser.role.canManageUsersAndCentres()) {
                                IconButton(
                                    onClick = {
                                        editingRow = row
                                        workingDaysStr = row.monthlyWorkingDays.toString()
                                        dailyTargetStr = row.dailyIncomeTarget.toString()
                                        manualMonthlyStr = row.monthlyIncomeTarget.toString()
                                        remainingDaysStr = row.remainingWorkingDays.toString()
                                    },
                                    modifier = Modifier.testTag("edit_target_btn_${row.centreId}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "সম্পাদনা", tint = GkGreenPrimary)
                                }
                            }
                        }

                        Divider(color = GkOutlineLight, modifier = Modifier.padding(vertical = 6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("B. কার্যদিবস: ${BanglaFormatter.toBanglaDigits(row.monthlyWorkingDays)} দিন", style = MaterialTheme.typography.bodySmall)
                            Text("J. অবশিষ্ট দিন: ${BanglaFormatter.toBanglaDigits(row.remainingWorkingDays)} দিন", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("C. দৈনিক লক্ষ্য: ${BanglaFormatter.formatCurrencyBdt(row.dailyIncomeTarget)}", style = MaterialTheme.typography.bodySmall)
                            Text("D. মাসিক লক্ষ্য: ${BanglaFormatter.formatCurrencyBdt(row.monthlyIncomeTarget)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = GkGreenPrimary))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("F. অর্জিত আয়: ${BanglaFormatter.formatCurrencyBdt(row.monthToDateIncome)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text("H. অর্জনের হার: ${BanglaFormatter.formatPercentage(row.achievementPercentage)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = GkGoldDark))
                        }
                    }
                }
            }
        }
    }

    // Edit Target Modal
    if (editingRow != null) {
        val row = editingRow!!
        AlertDialog(
            onDismissRequest = { editingRow = null },
            title = { Text("${row.centreNameBangla} - লক্ষ্যমাত্রা সম্পাদনা") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = workingDaysStr,
                        onValueChange = { workingDaysStr = it },
                        label = { Text("মাসিক কার্যদিবস (Working Days)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dailyTargetStr,
                        onValueChange = { dailyTargetStr = it },
                        label = { Text("দৈনিক আয়ের লক্ষ্যমাত্রা (Daily Target ৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = manualMonthlyStr,
                        onValueChange = { manualMonthlyStr = it },
                        label = { Text("অনুমোদিত মাসিক লক্ষ্যমাত্রা (ঐচ্ছিক ম্যানুয়াল ৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = remainingDaysStr,
                        onValueChange = { remainingDaysStr = it },
                        label = { Text("অবশিষ্ট কার্যদিবস (Remaining Days)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wd = workingDaysStr.toIntOrNull() ?: 26
                        val dt = dailyTargetStr.toDoubleOrNull() ?: 10000.0
                        val mm = manualMonthlyStr.toDoubleOrNull()
                        val rd = remainingDaysStr.toIntOrNull() ?: 10

                        viewModel.updateTarget(
                            centreId = row.centreId,
                            workingDays = wd,
                            dailyTarget = dt,
                            manualMonthly = mm,
                            remainingDays = rd
                        )
                        editingRow = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GkGreenPrimary)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRow = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

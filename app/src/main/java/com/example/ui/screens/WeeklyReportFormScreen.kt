package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.model.AccountingCalculations
import com.example.model.BanglaFormatter
import com.example.model.HealthCentre
import com.example.model.ReportStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReportFormScreen(
    viewModel: AccountingViewModel,
    initialCentreId: String?,
    onSubmitted: () -> Unit
) {
    val centres by viewModel.allCentres.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedCentre by remember(centres, initialCentreId) {
        val cid = initialCentreId ?: currentUser.assignedCentreId ?: "gk-c11"
        mutableStateOf(centres.find { it.id == cid } ?: centres.firstOrNull())
    }

    var periodStartDate by remember { mutableStateOf("2026-10-08") }
    var periodEndDate by remember { mutableStateOf("2026-10-14") }

    // Patient and Camp Counts
    var centrePatientsStr by remember { mutableStateOf("120") }
    var satellitePatientsStr by remember { mutableStateOf("45") }
    var campPatientsStr by remember { mutableStateOf("80") }
    var campsCountStr by remember { mutableStateOf("2") }

    // 3. Centre Income Breakdown
    var c31PrescriptionStr by remember { mutableStateOf("440") }
    var c32MedicineStr by remember { mutableStateOf("2947") }
    var c33LabStr by remember { mutableStateOf("1520") }
    var c34UsgStr by remember { mutableStateOf("0") }
    var c35OtherStr by remember { mutableStateOf("620") }

    // 4. Camp Income Breakdown
    var camp41PrescriptionStr by remember { mutableStateOf("600") }
    var camp42MedicineStr by remember { mutableStateOf("3483") }
    var camp43LabStr by remember { mutableStateOf("1180") }
    var camp44UsgStr by remember { mutableStateOf("0") }
    var camp45OtherStr by remember { mutableStateOf("320") }

    // 5. Total Reported Weekly Income
    var reportedWeeklyIncomeStr by remember { mutableStateOf("11110") }

    // 6. Total Weekly Expense Including Depreciation
    var totalWeeklyExpenseStr by remember { mutableStateOf("1700") }

    var notes by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successStatus by remember { mutableStateOf(ReportStatus.VALIDATED) }

    // Real-time live reconciliation
    val c31 = c31PrescriptionStr.toDoubleOrNull() ?: 0.0
    val c32 = c32MedicineStr.toDoubleOrNull() ?: 0.0
    val c33 = c33LabStr.toDoubleOrNull() ?: 0.0
    val c34 = c34UsgStr.toDoubleOrNull() ?: 0.0
    val c35 = c35OtherStr.toDoubleOrNull() ?: 0.0

    val camp41 = camp41PrescriptionStr.toDoubleOrNull() ?: 0.0
    val camp42 = camp42MedicineStr.toDoubleOrNull() ?: 0.0
    val camp43 = camp43LabStr.toDoubleOrNull() ?: 0.0
    val camp44 = camp44UsgStr.toDoubleOrNull() ?: 0.0
    val camp45 = camp45OtherStr.toDoubleOrNull() ?: 0.0

    val reportedWeeklyIncome = reportedWeeklyIncomeStr.toDoubleOrNull() ?: 0.0
    val totalWeeklyExpense = totalWeeklyExpenseStr.toDoubleOrNull() ?: 0.0

    val reconciliation = remember(
        c31, c32, c33, c34, c35,
        camp41, camp42, camp43, camp44, camp45,
        reportedWeeklyIncome
    ) {
        AccountingCalculations.reconcileWeeklyReport(
            cPrescription = c31,
            cMedicine = c32,
            cLab = c33,
            cUsg = c34,
            cOther = c35,
            campPrescription = camp41,
            campMedicine = camp42,
            campLab = camp43,
            campUsg = camp44,
            campOther = camp45,
            reportedWeeklyIncome = reportedWeeklyIncome
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Form Title
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "সাপ্তাহিক রিকনসিলিয়েশন রিপোর্ট (Weekly Report)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "কেন্দ্র ও ক্যাম্প উপখাতসমূহের স্বয়ংক্রিয় রিকনসিলিয়েশন ও অনুমোদন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Test Presets Row (Tongibari & Malkhanagar verified cases)
        Card(
            colors = CardDefaults.cardColors(containerColor = GkGoldContainer),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "নমুনা ডেটা প্রিসেট (অফিশিয়াল ভেরিফাইড টেস্ট কেস):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GkOnGoldContainer)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Tongibari verified preset
                            selectedCentre = centres.find { it.id == "gk-c11" }
                            periodStartDate = "2026-10-08"
                            periodEndDate = "2026-10-14"
                            centrePatientsStr = "120"
                            satellitePatientsStr = "45"
                            campPatientsStr = "80"
                            campsCountStr = "2"
                            c31PrescriptionStr = "440"
                            c32MedicineStr = "2947"
                            c33LabStr = "1520"
                            c34UsgStr = "0"
                            c35OtherStr = "620"
                            camp41PrescriptionStr = "600"
                            camp42MedicineStr = "3483"
                            camp43LabStr = "1180"
                            camp44UsgStr = "0"
                            camp45OtherStr = "320"
                            reportedWeeklyIncomeStr = "11110"
                            totalWeeklyExpenseStr = "1700"
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GkGreenPrimary),
                        modifier = Modifier.weight(1f).testTag("preset_tongibari_btn")
                    ) {
                        Text("টঙ্গীবাড়ী (১১,১১০৳)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            // Malkhanagar verified preset
                            selectedCentre = centres.find { it.id == "gk-c01" }
                            periodStartDate = "2026-10-08"
                            periodEndDate = "2026-10-14"
                            centrePatientsStr = "310"
                            satellitePatientsStr = "95"
                            campPatientsStr = "240"
                            campsCountStr = "4"
                            c31PrescriptionStr = "3500"
                            c32MedicineStr = "6163"
                            c33LabStr = "1200"
                            c34UsgStr = "0"
                            c35OtherStr = "560"
                            camp41PrescriptionStr = "4300"
                            camp42MedicineStr = "14283"
                            camp43LabStr = "9296"
                            camp44UsgStr = "3300"
                            camp45OtherStr = "1270"
                            reportedWeeklyIncomeStr = "43872"
                            totalWeeklyExpenseStr = "17000"
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GkGoldSecondary),
                        modifier = Modifier.weight(1f).testTag("preset_malkhanagar_btn")
                    ) {
                        Text("মালখানগর (৪৩,৮৭২৳)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Centre Selection
        var expandedCentreDropdown by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedCentreDropdown,
            onExpandedChange = { expandedCentreDropdown = !expandedCentreDropdown }
        ) {
            OutlinedTextField(
                value = "${selectedCentre?.nameBangla ?: ""} (${selectedCentre?.code ?: ""})",
                onValueChange = {},
                readOnly = true,
                label = { Text("স্বাস্থ্য কেন্দ্র নির্বাচন") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCentreDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .testTag("weekly_centre_dropdown")
            )
            ExposedDropdownMenu(
                expanded = expandedCentreDropdown,
                onDismissRequest = { expandedCentreDropdown = false }
            ) {
                centres.forEach { c ->
                    DropdownMenuItem(
                        text = { Text("${c.nameBangla} (${c.nameEnglish})") },
                        onClick = {
                            selectedCentre = c
                            expandedCentreDropdown = false
                        }
                    )
                }
            }
        }

        // Period Date Range
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = periodStartDate,
                onValueChange = { periodStartDate = it },
                label = { Text("শুরুর তারিখ (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f).testTag("weekly_start_date")
            )
            OutlinedTextField(
                value = periodEndDate,
                onValueChange = { periodEndDate = it },
                label = { Text("শেষের তারিখ (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f).testTag("weekly_end_date")
            )
        }

        // Patient counts
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "রোগী ও ক্যাম্প সংখ্যা (Patients & Camps):",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = centrePatientsStr,
                        onValueChange = { centrePatientsStr = it },
                        label = { Text("১(ক). কেন্দ্র রোগী") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = satellitePatientsStr,
                        onValueChange = { satellitePatientsStr = it },
                        label = { Text("১(খ). স্যাটেলাইট") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = campPatientsStr,
                        onValueChange = { campPatientsStr = it },
                        label = { Text("১(গ). ক্যাম্প রোগী") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = campsCountStr,
                        onValueChange = { campsCountStr = it },
                        label = { Text("২. ক্যাম্প সংখ্যা") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 3: Centre Income
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "৩. কেন্দ্র আয় উপখাতসমূহ (Centre Income):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GkGreenPrimary)
                    )
                    Text(
                        text = "যোগফল: ${BanglaFormatter.formatCurrencyBdt(reconciliation.calculatedCentreIncome)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GkGreenPrimary)
                    )
                }
                OutlinedTextField(
                    value = c31PrescriptionStr,
                    onValueChange = { c31PrescriptionStr = it },
                    label = { Text("৩.১ কেন্দ্র+স্যাটেলাইট প্রেসক্রিপশন ফি (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = c32MedicineStr,
                    onValueChange = { c32MedicineStr = it },
                    label = { Text("৩.২ কেন্দ্র+স্যাটেলাইট ঔষধ আয় (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = c33LabStr,
                    onValueChange = { c33LabStr = it },
                    label = { Text("৩.৩ কেন্দ্র+স্যাটেলাইট ল্যাবরেটরি আয় (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = c34UsgStr,
                    onValueChange = { c34UsgStr = it },
                    label = { Text("৩.৪ ${settings.usgLabel}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = c35OtherStr,
                    onValueChange = { c35OtherStr = it },
                    label = { Text("৩.৫ অন্যান্য কেন্দ্র আয় (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Section 4: Camp Income
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "৪. ক্যাম্প আয় উপখাতসমূহ (Camp Income):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GkGoldSecondary)
                    )
                    Text(
                        text = "যোগফল: ${BanglaFormatter.formatCurrencyBdt(reconciliation.calculatedCampIncome)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GkGoldSecondary)
                    )
                }
                OutlinedTextField(
                    value = camp41PrescriptionStr,
                    onValueChange = { camp41PrescriptionStr = it },
                    label = { Text("৪.১ ক্যাম্প প্রেসক্রিপশন ফি (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = camp42MedicineStr,
                    onValueChange = { camp42MedicineStr = it },
                    label = { Text("৪.২ ক্যাম্প ঔষধ বিক্রয় আয় (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = camp43LabStr,
                    onValueChange = { camp43LabStr = it },
                    label = { Text("৪.৩ ক্যাম্প ল্যাবরেটরি আয় (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = camp44UsgStr,
                    onValueChange = { camp44UsgStr = it },
                    label = { Text("৪.৪ ক্যাম্প ইউএসজি আয় (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = camp45OtherStr,
                    onValueChange = { camp45OtherStr = it },
                    label = { Text("৪.৫ অন্যান্য ক্যাম্প আয় (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Section 5 & 6: Reported Weekly Income & Weekly Expense
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "৫ ও ৬. মোট সাপ্তাহিক আয় ও ব্যয় (Reconciliation):",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                OutlinedTextField(
                    value = reportedWeeklyIncomeStr,
                    onValueChange = { reportedWeeklyIncomeStr = it },
                    label = { Text("৫. মোট সাপ্তাহিক দাখিলকৃত আয় (Reported Total - BDT ৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("৳ ", fontWeight = FontWeight.Bold, color = GkGreenPrimary) },
                    modifier = Modifier.fillMaxWidth().testTag("field_5_reported_weekly_income")
                )
                OutlinedTextField(
                    value = totalWeeklyExpenseStr,
                    onValueChange = { totalWeeklyExpenseStr = it },
                    label = { Text("৬. মোট সাপ্তাহিক ব্যয় অবচয়সহ (Weekly Expense - BDT ৳)") },
                    supportingText = {
                        Text("নোট: সাপ্তাহিক ব্যয় একটি রিকনসিলিয়েশন রিপোর্ট; এটি দৈনিক ব্যয়ের সাথে অতিরিক্ত হিসেবে যোগ হবে না।")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("৳ ", fontWeight = FontWeight.Bold, color = Color(0xFFC62828)) },
                    modifier = Modifier.fillMaxWidth().testTag("field_6_weekly_expense")
                )
            }
        }

        // Real-Time Live Reconciliation Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (reconciliation.isMatched) GkStatusValidatedBg else GkStatusPendingBg
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("reconciliation_result_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (reconciliation.isMatched) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (reconciliation.isMatched) GkStatusValidated else GkStatusPending
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (reconciliation.isMatched) "হিসাব মিলেছে (VALIDATED)" else "অমিল রয়েছে (PENDING)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (reconciliation.isMatched) GkStatusValidated else GkStatusPending
                            )
                        )
                    }
                    StatusBadge(reconciliation.status)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reconciliation.discrepancyMessageBangla,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (reconciliation.isMatched) GkStatusValidated else GkStatusPending
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "গণনাকৃত আয়: ${BanglaFormatter.formatCurrencyBdt(reconciliation.calculatedWeeklyIncome)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "দাখিলকৃত আয়: ${BanglaFormatter.formatCurrencyBdt(reconciliation.reportedWeeklyIncome)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                val c = selectedCentre ?: return@Button
                val cp = centrePatientsStr.toIntOrNull() ?: 0
                val sp = satellitePatientsStr.toIntOrNull() ?: 0
                val campP = campPatientsStr.toIntOrNull() ?: 0
                val camps = campsCountStr.toIntOrNull() ?: 0

                viewModel.submitWeeklyReport(
                    centreId = c.id,
                    periodStartDate = periodStartDate.trim(),
                    periodEndDate = periodEndDate.trim(),
                    centrePatients = cp,
                    satellitePatients = sp,
                    campPatients = campP,
                    campsCount = camps,
                    cPrescription = c31,
                    cMedicine = c32,
                    cLab = c33,
                    cUsg = c34,
                    cOther = c35,
                    campPrescription = camp41,
                    campMedicine = camp42,
                    campLab = camp43,
                    campUsg = camp44,
                    campOther = camp45,
                    reportedWeeklyIncome = reportedWeeklyIncome,
                    totalWeeklyExpense = totalWeeklyExpense,
                    notes = notes
                ) {
                    successStatus = reconciliation.status
                    showSuccessDialog = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = GkGreenPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_weekly_report_button")
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "সাপ্তাহিক রিপোর্ট দাখিল ও যাচাই করুন",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSubmitted()
            },
            title = {
                Text(
                    text = if (successStatus == ReportStatus.VALIDATED) "রিপোর্ট যাচাইকৃত ও দাখিল সফল" else "যাচাইকরণ অপেক্ষমাণ তালিকায় যুক্ত"
                )
            },
            text = {
                Text(
                    text = if (successStatus == ReportStatus.VALIDATED) {
                        "সাপ্তাহিক রিপোর্টটি সফলভাবে যাচাই করা হয়েছে এবং নিশ্চিত হিসাবে অন্তর্ভুক্ত হয়েছে।"
                    } else {
                        "দাখিলকৃত আয়ের সাথে উপখাতসমূহের যোগফলে ব্যবধান থাকায় রিপোর্টটি 'যাচাইকরণ অপেক্ষমাণ' হিসেবে চিহ্নিত হয়েছে। এডমিন/হিসাবরক্ষক নিরীক্ষার পর এটি চূড়ান্ত হবে।"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onSubmitted()
                    }
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}

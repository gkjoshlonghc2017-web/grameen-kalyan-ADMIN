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
import com.example.model.BanglaFormatter
import com.example.model.HealthCentre
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReportFormScreen(
    viewModel: AccountingViewModel,
    initialCentreId: String?,
    onSubmitted: () -> Unit
) {
    val centres by viewModel.allCentres.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedCentre by remember(centres, initialCentreId) {
        val cid = initialCentreId ?: currentUser.assignedCentreId ?: "gk-c01"
        mutableStateOf(centres.find { it.id == cid } ?: centres.firstOrNull())
    }

    var reportDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var reportingPeriod by remember { mutableStateOf("দৈনিক (Daily)") }

    // 8 Numbered Form Fields
    var centrePatientsStr by remember { mutableStateOf("35") }
    var satellitePatientsStr by remember { mutableStateOf("12") }
    var campPatientsStr by remember { mutableStateOf("20") }
    var totalDailyIncomeStr by remember { mutableStateOf("15000") }
    var medicineLabCostStr by remember { mutableStateOf("5200") }
    var satelliteSessionsStr by remember { mutableStateOf("1") }
    var cardsCountStr by remember { mutableStateOf("10") }
    var totalDailyExpenseStr by remember { mutableStateOf("9800") }
    var notes by remember { mutableStateOf("") }

    val incomeVal = totalDailyIncomeStr.toDoubleOrNull() ?: 0.0
    val expenseVal = totalDailyExpenseStr.toDoubleOrNull() ?: 0.0
    val medCostVal = medicineLabCostStr.toDoubleOrNull() ?: 0.0

    // Warning if field 5 > field 8
    val hasMedCostWarning = medCostVal > expenseVal && expenseVal > 0

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

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
                    text = "দৈনিক রিপোর্ট ফর্ম (Daily Report)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "৮টি নির্ধারিত ক্ষেত্র সতর্কতার সাথে পূরণ করুন। হিসাবের স্বচ্ছতা নিশ্চিত করুন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
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
                    .testTag("daily_centre_dropdown")
            )
            ExposedDropdownMenu(
                expanded = expandedCentreDropdown,
                onDismissRequest = { expandedCentreDropdown = false }
            ) {
                centres.forEach { c ->
                    DropdownMenuItem(
                        text = { Text("${c.nameBangla} (${c.nameEnglish} - ${c.code})") },
                        onClick = {
                            selectedCentre = c
                            expandedCentreDropdown = false
                        }
                    )
                }
            }
        }

        // Date and Period
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = reportDate,
                onValueChange = { reportDate = it },
                label = { Text("রিপোর্ট তারিখ (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f).testTag("daily_date_field")
            )
            OutlinedTextField(
                value = reportingPeriod,
                onValueChange = { reportingPeriod = it },
                label = { Text("রিপোর্টিং পিরিয়ড") },
                modifier = Modifier.weight(1f)
            )
        }

        Divider(color = GkOutlineLight)

        // 8 Numbered Fields Group
        Text(
            text = "নির্ধারিত ৮টি ফিল্ড এন্ট্রি:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GkGreenPrimary)
        )

        // 1. Centre patients
        OutlinedTextField(
            value = centrePatientsStr,
            onValueChange = { centrePatientsStr = it },
            label = { Text("১. কেন্দ্র রোগী (Centre Patients - পূর্ণসংখ্যা)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("field_1_centre_patients")
        )

        // 2. Satellite patients
        OutlinedTextField(
            value = satellitePatientsStr,
            onValueChange = { satellitePatientsStr = it },
            label = { Text("২. স্যাটেলাইট রোগী (Satellite Patients - পূর্ণসংখ্যা)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("field_2_satellite_patients")
        )

        // 3. Camp patients
        OutlinedTextField(
            value = campPatientsStr,
            onValueChange = { campPatientsStr = it },
            label = { Text("৩. ক্যাম্প রোগী (Camp Patients - পূর্ণসংখ্যা)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("field_3_camp_patients")
        )

        // 4. Total daily income
        OutlinedTextField(
            value = totalDailyIncomeStr,
            onValueChange = { totalDailyIncomeStr = it },
            label = { Text("৪. মোট দৈনিক আয় (Total Daily Income - BDT ৳)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Text("৳ ", fontWeight = FontWeight.Bold, color = GkGreenPrimary) },
            modifier = Modifier.fillMaxWidth().testTag("field_4_daily_income")
        )

        // 5. Medicine and laboratory cost
        OutlinedTextField(
            value = medicineLabCostStr,
            onValueChange = { medicineLabCostStr = it },
            label = { Text("৫. ঔষধ ও ল্যাবরেটরি খরচ (Medicine & Lab Cost - BDT ৳)") },
            supportingText = {
                Text("একটি আলাদা রিপোর্টকৃত উপাদান; এটি স্বয়ংক্রিয়ভাবে ফিল্ড ৮ এ যোগ হবে না।")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Text("৳ ", fontWeight = FontWeight.Bold, color = GkGoldSecondary) },
            modifier = Modifier.fillMaxWidth().testTag("field_5_med_cost")
        )

        // 6. Number of satellite sessions
        OutlinedTextField(
            value = satelliteSessionsStr,
            onValueChange = { satelliteSessionsStr = it },
            label = { Text("৬. স্যাটেলাইট সেশন সংখ্যা (Satellite Sessions - পূর্ণসংখ্যা)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("field_6_satellite_sessions")
        )

        // 7. Number of cards
        OutlinedTextField(
            value = cardsCountStr,
            onValueChange = { cardsCountStr = it },
            label = { Text("৭. কার্ড সংখ্যা (Number of Cards - পূর্ণসংখ্যা)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("field_7_cards_count")
        )

        // 8. Total daily expense including depreciation
        OutlinedTextField(
            value = totalDailyExpenseStr,
            onValueChange = { totalDailyExpenseStr = it },
            label = { Text("৮. মোট দৈনিক ব্যয় অবচয়সহ (Total Daily Expense - BDT ৳)") },
            supportingText = {
                Text("সতর্কতা: এতে ইতোমধ্যে অবচয় অন্তর্ভুক্ত আছে। দ্বিতীয়বার অবচয় যোগ করবেন না।")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Text("৳ ", fontWeight = FontWeight.Bold, color = Color(0xFFC62828)) },
            modifier = Modifier.fillMaxWidth().testTag("field_8_daily_expense")
        )

        // Warning banner if field 5 > field 8
        if (hasMedCostWarning) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GkStatusPendingBg),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = GkStatusPending)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "সতর্কতা: ফিল্ড ৫ (ঔষধ ও ল্যাব খরচ ৳$medCostVal) ফিল্ড ৮ (মোট ব্যয় ৳$expenseVal) এর চেয়ে বেশি। হিসাবরক্ষণ সংজ্ঞার কারণে অনুমোদিত রিভিউসহ দাখিল করা যাবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = GkStatusPending
                    )
                }
            }
        }

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("মন্তব্য / বিশেষ বিবরণ (ঐচ্ছিক)") },
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Submit Button
        Button(
            onClick = {
                val c = selectedCentre
                if (c == null) {
                    errorMessage = "অনুগ্রহ করে স্বাস্থ্য কেন্দ্র নির্বাচন করুন।"
                    return@Button
                }
                val cp = centrePatientsStr.toIntOrNull() ?: 0
                val sp = satellitePatientsStr.toIntOrNull() ?: 0
                val campP = campPatientsStr.toIntOrNull() ?: 0
                val ss = satelliteSessionsStr.toIntOrNull() ?: 0
                val cd = cardsCountStr.toIntOrNull() ?: 0

                isSubmitting = true
                errorMessage = null

                viewModel.submitDailyReport(
                    centreId = c.id,
                    reportDate = reportDate.trim(),
                    reportingPeriod = reportingPeriod.trim(),
                    centrePatients = cp,
                    satellitePatients = sp,
                    campPatients = campP,
                    totalDailyIncome = incomeVal,
                    medicineLabCost = medCostVal,
                    satelliteSessions = ss,
                    cardsCount = cd,
                    totalDailyExpense = expenseVal,
                    notes = notes
                ) {
                    isSubmitting = false
                    showSuccessDialog = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = GkGreenPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_daily_report_button")
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "দৈনিক রিপোর্ট দাখিল করুন (Submit)",
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
            title = { Text("দাখিল সফল") },
            text = { Text("দৈনিক রিপোর্ট সফলভাবে সংরক্ষিত হয়েছে এবং হিসাব বিবরণীতে অন্তর্ভুক্ত করা হয়েছে।") },
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

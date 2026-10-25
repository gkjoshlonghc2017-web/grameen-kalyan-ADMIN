package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccountingCalculations
import com.example.model.BanglaFormatter
import com.example.model.DailyReport
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSharingScreen(
    viewModel: AccountingViewModel,
    initialCentreId: String?
) {
    val context = LocalContext.current
    val centres by viewModel.allCentres.collectAsState()
    val dailyReports by viewModel.allDailyReports.collectAsState()
    val weeklyReports by viewModel.allWeeklyReports.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    var selectedCentreId by remember(centres, initialCentreId) {
        mutableStateOf(initialCentreId ?: centres.firstOrNull()?.id ?: "gk-c11")
    }

    val currentCentre = centres.find { it.id == selectedCentreId } ?: centres.firstOrNull()

    // Find the latest daily report for this centre
    val latestDaily = remember(dailyReports, currentCentre) {
        dailyReports.filter { it.centreId == currentCentre?.id }.maxByOrNull { it.createdAt }
    }

    var recipientNumber by remember(settings) {
        val firstNum = settings.recipientPhoneNumbers.split(",").firstOrNull()?.trim() ?: "+8801711000000"
        mutableStateOf(firstNum)
    }

    var smsMessageBody by remember(latestDaily, currentCentre) {
        val text = if (latestDaily != null && currentCentre != null) {
            AccountingCalculations.formatDailyReportSms(latestDaily, currentCentre)
        } else {
            "গ্রামীণ কল্যাণ কেরানীগঞ্জ\nকেন্দ্র: ${currentCentre?.nameBangla ?: ""}\nআজকের কোনো রিপোর্ট নথিভুক্ত হয়নি।"
        }
        mutableStateOf(text)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "এসএমএস ও রিপোর্ট শেয়ারিং",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "অনলাইন ক্লাউড দাখিলের পাশাপাশি হেড অফিস বা আঞ্চলিক কর্মকর্তাদের কাছে স্ট্যান্ডার্ড ফরম্যাটে বার্তা পাঠানো যায়।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Centre Picker
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = "${currentCentre?.nameBangla ?: ""} (${currentCentre?.code ?: ""})",
                onValueChange = {},
                readOnly = true,
                label = { Text("স্বাস্থ্য কেন্দ্র") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                centres.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c.nameBangla) },
                        onClick = {
                            selectedCentreId = c.id
                            expanded = false
                        }
                    )
                }
            }
        }

        // Recipient Number
        OutlinedTextField(
            value = recipientNumber,
            onValueChange = { recipientNumber = it },
            label = { Text("অনুমোদিত প্রাপক নম্বর (Recipient Phone)") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GkGreenPrimary) },
            supportingText = { Text("সেটিংস প্যানেলে অনুমোদিত নম্বরের তালিকা কনফিগার করা যায়।") },
            modifier = Modifier.fillMaxWidth().testTag("sms_recipient_number")
        )

        // SMS Content Preview
        OutlinedTextField(
            value = smsMessageBody,
            onValueChange = { smsMessageBody = it },
            label = { Text("এসএমএস বার্তা (প্রিভিউ ও সম্পাদনযোগ্য)") },
            minLines = 8,
            modifier = Modifier.fillMaxWidth().testTag("sms_body_text")
        )

        // Send via SMS Composer Button
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${recipientNumber.trim()}")
                    putExtra("sms_body", smsMessageBody)
                }
                context.startActivity(Intent.createChooser(intent, "এসএমএস অ্যাপ নির্বাচন করুন"))
            },
            colors = ButtonDefaults.buttonColors(containerColor = GkGreenPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("launch_sms_composer_btn")
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("এসএমএস কম্পোজার ওপেন করুন (Open SMS App)")
        }

        // Share via any app button
        OutlinedButton(
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, smsMessageBody)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "রিপোর্ট শেয়ার করুন")
                context.startActivity(shareIntent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("অন্যান্য অ্যাপে শেয়ার করুন (WhatsApp / Email)")
        }
    }
}

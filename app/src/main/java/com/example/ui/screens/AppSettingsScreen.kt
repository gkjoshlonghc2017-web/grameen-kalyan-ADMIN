package com.example.ui.screens

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
import com.example.model.AppSettings
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun AppSettingsScreen(
    viewModel: AccountingViewModel
) {
    val settings by viewModel.appSettings.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var workingDaysStr by remember(settings) { mutableStateOf(settings.defaultWorkingDays.toString()) }
    var phoneNumbersStr by remember(settings) { mutableStateOf(settings.recipientPhoneNumbers) }
    var usgLabelStr by remember(settings) { mutableStateOf(settings.usgLabel) }

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
                    Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অ্যাপ ও একাউন্টিং পলিসি সেটিংস",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ক্লাউড সিঙ্ক, কার্যদিবস ক্যালেন্ডার ও এসএমএস প্রাপক ব্যবস্থাপনা।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Cloud Sync Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = GkGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ক্লাউড ফায়ারস্টোর সিঙ্ক স্থিতি",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    TextButton(onClick = { viewModel.syncWithCloud() }) {
                        Text("এখনই সিঙ্ক করুন", color = GkGreenPrimary)
                    }
                }
                Surface(
                    color = GkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = syncState,
                        style = MaterialTheme.typography.bodySmall,
                        color = GkGreenPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Accounting Policy Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = GkGoldContainer),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "কস্ট রিকভারি হিসাবরক্ষণ নীতি (Cost Recovery Policy):",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GkOnGoldContainer)
                )
                Text(
                    text = "• সূত্র: (মোট আয় / মোট ব্যয় অবচয়সহ) × ১০০\n• দৈনিক ফিল্ড ৮ এবং সাপ্তাহিক ফিল্ড ৬ এ ইতিমধ্যে অবচয় অন্তর্ভুক্ত বিধায় দ্বিতীয়বার অবচয় যোগ নিষিদ্ধ।\n• ফিল্ড ৫ (ঔষধ ও ল্যাব) একটি পৃথক উপাদান, তা ফিল্ড ৮ এ দ্বিতীয়বার যোগ হয় না।",
                    style = MaterialTheme.typography.bodySmall,
                    color = GkOnGoldContainer.copy(alpha = 0.9f)
                )
            }
        }

        // Editable Settings Form
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "কনফিগারেশন ফিল্ডসমূহ:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = workingDaysStr,
                    onValueChange = { workingDaysStr = it },
                    label = { Text("ডিফল্ট মাসিক কার্যদিবস (Default Working Days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = currentUser.role.canManageUsersAndCentres(),
                    modifier = Modifier.fillMaxWidth().testTag("setting_working_days")
                )

                OutlinedTextField(
                    value = phoneNumbersStr,
                    onValueChange = { phoneNumbersStr = it },
                    label = { Text("এসএমএস প্রাপক ফোন নম্বরসমূহ (কমা দিয়ে পৃথক)") },
                    supportingText = { Text("উদাহরণ: +8801711000000, +8801811000000") },
                    enabled = currentUser.role.canManageUsersAndCentres(),
                    modifier = Modifier.fillMaxWidth().testTag("setting_sms_recipients")
                )

                OutlinedTextField(
                    value = usgLabelStr,
                    onValueChange = { usgLabelStr = it },
                    label = { Text("ফিল্ড ৩.৪ / ৪.৪ ইউএসজি লেবেল (Configurable USG Label)") },
                    supportingText = { Text("সংজ্ঞায়িত লেবেল: পরিমাণ বা সংখ্যা") },
                    enabled = currentUser.role.canManageUsersAndCentres(),
                    modifier = Modifier.fillMaxWidth().testTag("setting_usg_label")
                )

                if (currentUser.role.canManageUsersAndCentres()) {
                    Button(
                        onClick = {
                            val wd = workingDaysStr.toIntOrNull() ?: 26
                            viewModel.updateSettings(
                                AppSettings(
                                    defaultWorkingDays = wd,
                                    recipientPhoneNumbers = phoneNumbersStr.trim(),
                                    usgLabel = usgLabelStr.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GkGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_settings_btn")
                    ) {
                        Text("সেটিংস সংরক্ষণ করুন")
                    }
                } else {
                    Text(
                        text = "সেটিংস পরিবর্তন করতে এডমিন অনুমতি প্রয়োজন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = GkTextTertiary
                    )
                }
            }
        }
    }
}

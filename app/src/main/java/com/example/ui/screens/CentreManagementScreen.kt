package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.HealthCentre
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun CentreManagementScreen(
    viewModel: AccountingViewModel
) {
    val centres by viewModel.allCentres.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var editingCentre by remember { mutableStateOf<HealthCentre?>(null) }
    var editNameBangla by remember { mutableStateOf("") }
    var editNameEnglish by remember { mutableStateOf("") }
    var editCode by remember { mutableStateOf("") }
    var editReason by remember { mutableStateOf("") }

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
                    text = "কেরানীগঞ্জের ১১টি স্বাস্থ্য কেন্দ্র ব্যবস্থাপনা",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "কেন্দ্রসমূহের ইউনিক আইডি অপরিবর্তনশীল; শুধুমাত্র প্রদর্শন নাম ও কোড সম্পাদনাযোগ্য।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(centres) { centre ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("centre_manage_${centre.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${centre.nameBangla} (${centre.code})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "ইংরেজি নাম: ${centre.nameEnglish}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GkTextSecondary
                            )
                            Text(
                                text = "ডাটাবেজ ইউনিক আইডি: ${centre.id}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                color = GkGreenPrimary
                            )
                        }

                        if (currentUser.role.canManageUsersAndCentres()) {
                            IconButton(
                                onClick = {
                                    editingCentre = centre
                                    editNameBangla = centre.nameBangla
                                    editNameEnglish = centre.nameEnglish
                                    editCode = centre.code
                                    editReason = "প্রশাসনিক প্রয়োজনে কেন্দ্র তথ্য হালনাগাদ"
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "সম্পাদনা", tint = GkGreenPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingCentre != null) {
        val c = editingCentre!!
        AlertDialog(
            onDismissRequest = { editingCentre = null },
            title = { Text("কেন্দ্র তথ্য সম্পাদনা") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ইউনিক আইডি: ${c.id} (স্থায়ী ডাটাবেজ কি)", style = MaterialTheme.typography.bodySmall, color = GkTextTertiary)
                    OutlinedTextField(
                        value = editNameBangla,
                        onValueChange = { editNameBangla = it },
                        label = { Text("বাংলা প্রদর্শন নাম") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editNameEnglish,
                        onValueChange = { editNameEnglish = it },
                        label = { Text("ইংরেজি নাম") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCode,
                        onValueChange = { editCode = it },
                        label = { Text("সংক্ষিপ্ত কোড (Code)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editReason,
                        onValueChange = { editReason = it },
                        label = { Text("হালনাগাদের কারণ (অডিট ট্রেইল)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = c.copy(
                            nameBangla = editNameBangla.trim(),
                            nameEnglish = editNameEnglish.trim(),
                            code = editCode.trim().uppercase()
                        )
                        viewModel.updateCentre(updated, editReason.trim())
                        editingCentre = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GkGreenPrimary)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCentre = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

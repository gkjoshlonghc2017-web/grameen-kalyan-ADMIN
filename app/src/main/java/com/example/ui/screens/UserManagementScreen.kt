package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: AccountingViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val centres by viewModel.allCentres.collectAsState()

    var selectedRoleToSwitch by remember { mutableStateOf(currentUser.role) }
    var selectedCentreForUser by remember { mutableStateOf(centres.firstOrNull()?.id ?: "gk-c11") }

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
                    text = "ব্যবহারকারী ও ভূমিকা ব্যবস্থাপনা (RBAC)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "বর্তমানে সক্রিয়: ${currentUser.name} (${currentUser.role.displayNameBangla})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Quick Role Switcher Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ভূমিকা পরিবর্তন করে নিরাপত্তা যাচাই করুন:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                UserRole.values().forEach { role ->
                    val isSelected = selectedRoleToSwitch == role
                    Surface(
                        onClick = { selectedRoleToSwitch = role },
                        color = if (isSelected) GkGreenContainer else GkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedRoleToSwitch = role }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = role.displayNameBangla,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) GkGreenPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                val permDesc = when (role) {
                                    UserRole.ADMIN -> "সম্পূর্ণ অ্যাক্সেস, কেন্দ্র, লক্ষ্যমাত্রা, সংশোধন, অডিট ও সেটিংস।"
                                    UserRole.RM -> "সকল কেন্দ্র দর্শন, আঞ্চলিক ড্যাশবোর্ড ও তুলনা; হিসাব পরিবর্তনের অনুমতি নেই।"
                                    UserRole.ACCOUNTS -> "আর্থিক রিপোর্ট, রিকনসিলিয়েশন ও অনুমোদিত আর্থিক সংশোধন।"
                                    UserRole.CENTRE_USER -> "শুধুমাত্র নির্ধারিত কেন্দ্রের দৈনিক ও সাপ্তাহিক রিপোর্ট দাখিল।"
                                }
                                Text(text = permDesc, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = GkTextSecondary)
                            }
                        }
                    }
                }

                if (selectedRoleToSwitch == UserRole.CENTRE_USER) {
                    Text(
                        text = "নির্ধারিত স্বাস্থ্য কেন্দ্র:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    var expanded by remember { mutableStateOf(false) }
                    val currentCentre = centres.find { it.id == selectedCentreForUser }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = "${currentCentre?.nameBangla ?: ""} (${currentCentre?.code ?: ""})",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            centres.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.nameBangla) },
                                    onClick = {
                                        selectedCentreForUser = c.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val cid = if (selectedRoleToSwitch == UserRole.CENTRE_USER) selectedCentreForUser else null
                        viewModel.switchRole(selectedRoleToSwitch, cid)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GkGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("apply_role_switch_btn")
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("এই ভূমিকায় প্রবেশ করুন")
                }
            }
        }
    }
}

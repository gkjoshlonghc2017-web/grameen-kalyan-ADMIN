package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AccountingViewModel,
    onLoginSuccess: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val centres by viewModel.allCentres.collectAsState()

    var selectedRole by remember { mutableStateOf(currentUser.role) }
    var selectedCentreId by remember { mutableStateOf("gk-c11") } // default Tongibari
    var emailInput by remember { mutableStateOf(currentUser.email) }
    var passwordInput by remember { mutableStateOf("••••••••") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Brand Emblem
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(GkGreenPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "গ্রামীণ কল্যাণ কেরানীগঞ্জ",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = GkGreenPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "স্বাস্থ্য কেন্দ্রসমূহের হিসাব ও অডিট ব্যবস্থাপনা",
            style = MaterialTheme.typography.bodyMedium,
            color = GkTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Role Selection Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "প্রবেশের ভূমিকা নির্বাচন করুন (Select Role):",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                UserRole.values().forEach { role ->
                    val isSelected = selectedRole == role
                    Surface(
                        onClick = { selectedRole = role },
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
                                onClick = { selectedRole = role }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = role.displayNameBangla,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) GkGreenPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = role.displayNameEnglish,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = GkTextSecondary
                                )
                            }
                        }
                    }
                }

                if (selectedRole == UserRole.CENTRE_USER) {
                    var expanded by remember { mutableStateOf(false) }
                    val currentCentre = centres.find { it.id == selectedCentreId }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = "${currentCentre?.nameBangla ?: ""} (${currentCentre?.code ?: ""})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("নির্ধারিত কেন্দ্র") },
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
                }

                Button(
                    onClick = {
                        val cid = if (selectedRole == UserRole.CENTRE_USER) selectedCentreId else null
                        viewModel.switchRole(selectedRole, cid)
                        onLoginSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GkGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_submit_btn")
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অ্যাপে প্রবেশ করুন (Enter Dashboard)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

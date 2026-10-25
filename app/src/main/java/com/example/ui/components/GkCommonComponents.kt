package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.navigation.AppScreen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GkTopAppBar(
    title: String,
    currentUser: User,
    selectedMonth: String,
    onMenuClick: () -> Unit,
    onSyncClick: () -> Unit,
    onMonthChange: (String) -> Unit
) {
    var showMonthMenu by remember { mutableStateOf(false) }
    val months = listOf("2026-10", "2026-09", "2026-08", "2026-11", "2026-12")

    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${currentUser.name} • ${currentUser.role.displayNameEnglish}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = GkGoldLight,
                    maxLines = 1
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "মেনু খুলুন",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            // Month dropdown trigger
            Box {
                FilterChip(
                    selected = true,
                    onClick = { showMonthMenu = true },
                    label = {
                        Text(
                            text = BanglaFormatter.toBanglaDigits(selectedMonth),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GkOnGoldContainer
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "মাস নির্বাচন",
                            modifier = Modifier.size(16.dp),
                            tint = GkOnGoldContainer
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GkGoldContainer
                    ),
                    border = null,
                    modifier = Modifier.testTag("month_filter_chip")
                )

                DropdownMenu(
                    expanded = showMonthMenu,
                    onDismissRequest = { showMonthMenu = false }
                ) {
                    months.forEach { m ->
                        DropdownMenuItem(
                            text = { Text("মাস: ${BanglaFormatter.toBanglaDigits(m)}") },
                            onClick = {
                                onMonthChange(m)
                                showMonthMenu = false
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = onSyncClick,
                modifier = Modifier.testTag("sync_action_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "ক্লাউড সিঙ্ক করুন",
                    tint = GkGoldLight
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun StatusBadge(status: ReportStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        ReportStatus.VALIDATED -> Pair(GkStatusValidatedBg, GkStatusValidated)
        ReportStatus.PENDING_VERIFICATION -> Pair(GkStatusPendingBg, GkStatusPending)
        ReportStatus.REJECTED -> Pair(GkStatusRejectedBg, GkStatusRejected)
        ReportStatus.SYNCING, ReportStatus.QUEUED_OFFLINE -> Pair(GkStatusSyncingBg, GkStatusSyncing)
        ReportStatus.DRAFT, ReportStatus.SUBMITTED -> Pair(GkSurfaceVariant, GkTextSecondary)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.titleBangla,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun FinancialMetricCard(
    titleBangla: String,
    titleEnglish: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GkOutlineLight))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleBangla,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = titleEnglish,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = GkTextTertiary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                )
            }
        }
    }
}

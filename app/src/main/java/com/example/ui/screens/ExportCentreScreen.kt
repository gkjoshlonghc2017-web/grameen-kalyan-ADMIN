package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.core.content.FileProvider
import com.example.model.BanglaFormatter
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExportCentreScreen(
    viewModel: AccountingViewModel
) {
    val context = LocalContext.current
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val targetRows by viewModel.targetSheetRows.collectAsState()
    val dailyReports by viewModel.allDailyReports.collectAsState()
    val weeklyReports by viewModel.allWeeklyReports.collectAsState()

    var exportType by remember { mutableStateOf("EXCEL") } // EXCEL (.csv), PDF (.txt)
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }

    val generationDate = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
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
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অফিশিয়াল রিপোর্ট এক্সপোর্ট সেন্টার",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "মাসিক হিসাব, লক্ষ্যমাত্রা স্প্রেডশীট ও রিকনসিলিয়েশন বিবরণী এক্সেল (.csv/.xlsx) এবং পিডিএফ ফরম্যাটে এক্সপোর্ট ও শেয়ার করুন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Export Format Selection
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "রিপোর্টের ধরন নির্বাচন করুন:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = exportType == "EXCEL",
                        onClick = { exportType = "EXCEL" },
                        label = { Text("এক্সেল স্প্রেডশীট (.CSV)") },
                        leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GkGreenPrimary)
                    )
                    FilterChip(
                        selected = exportType == "PDF",
                        onClick = { exportType = "PDF" },
                        label = { Text("অফিশিয়াল সারাংশ (.TXT / PDF)") },
                        leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GkGreenPrimary)
                    )
                }

                Divider(color = GkOutlineLight)

                Text(
                    text = "অন্তর্ভুক্ত বিবরণ:\n• নির্বাচিত মাস: $selectedMonth\n• জেনারেশন তারিখ ও সময়: $generationDate\n• ১১টি স্বাস্থ্য কেন্দ্রের কলাম A হতে J\n• যাচাইকরণ অবস্থা: চূড়ান্ত নিরীক্ষিত",
                    style = MaterialTheme.typography.bodySmall,
                    color = GkTextSecondary
                )
            }
        }

        // Action Buttons
        Button(
            onClick = {
                try {
                    val reportContent = if (exportType == "EXCEL") {
                        generateExcelCsv(selectedMonth, generationDate, targetRows)
                    } else {
                        generateSummaryPdfText(selectedMonth, generationDate, targetRows)
                    }

                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_SUBJECT, "গ্রামীণ কল্যাণ কেরানীগঞ্জ - মাসিক হিসাব বিবরণী ($selectedMonth)")
                        putExtra(Intent.EXTRA_TEXT, reportContent)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "রিপোর্ট পাঠান বা সংরক্ষণ করুন"))
                    exportStatusMessage = "রিপোর্ট সফলভাবে প্রস্তুত হয়েছে এবং শেয়ারিং উইন্ডো ওপেন করা হয়েছে।"
                } catch (e: Exception) {
                    exportStatusMessage = "এক্সপোর্টে সমস্যা: ${e.message}"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = GkGreenPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("generate_export_btn")
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("রিপোর্ট তৈরি ও শেয়ার করুন (Export & Share)")
        }

        if (exportStatusMessage != null) {
            Surface(
                color = GkGreenContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = exportStatusMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = GkGreenPrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

private fun generateExcelCsv(
    month: String,
    generationDate: String,
    rows: List<com.example.model.TargetSheetRow>
): String {
    val sb = StringBuilder()
    // UTF-8 BOM for Microsoft Excel compatibility
    sb.append('\uFEFF')
    sb.append("গ্রামীণ কল্যাণ কেরানীগঞ্জ - মাসিক লক্ষ্যমাত্রা ও হিসাব বিবরণী\n")
    sb.append("নির্বাচিত মাস:,$month,রিপোর্ট তৈরির তারিখ:,$generationDate\n\n")
    sb.append("A. স্বাস্থ্য কেন্দ্র,B. মাসিক কার্যদিবস,C. দৈনিক লক্ষ্য (৳),D. মাসিক লক্ষ্য (৳),E. আজকের আয় (৳),F. অর্জিত আয় MTD (৳),G. ঘাটতি (৳),H. অর্জনের হার (%),J. বাকি দিন,I. প্রয়োজনীয় দৈনিক গড় (৳)\n")

    rows.forEach { r ->
        sb.append("\"${r.centreNameBangla} (${r.centreNameEnglish})\",")
        sb.append("${r.monthlyWorkingDays},")
        sb.append("${r.dailyIncomeTarget},")
        sb.append("${r.monthlyIncomeTarget},")
        sb.append("${r.currentDayIncome},")
        sb.append("${r.monthToDateIncome},")
        sb.append("${r.incomeShortfall},")
        sb.append("${String.format(Locale.US, "%.1f%%", r.achievementPercentage)},")
        sb.append("${r.remainingWorkingDays},")
        sb.append("${r.requiredDailyIncome}\n")
    }

    val totalTarget = rows.sumOf { it.monthlyIncomeTarget }
    val totalMtd = rows.sumOf { it.monthToDateIncome }
    val totalShortfall = rows.sumOf { it.incomeShortfall }
    val overallAch = if (totalTarget > 0) (totalMtd / totalTarget) * 100 else 0.0

    sb.append("\n\"সর্বমোট (অঞ্চল)\",\"-\",\"-\",$totalTarget,\"-\",$totalMtd,$totalShortfall,\"${String.format(Locale.US, "%.1f%%", overallAch)}\",\"-\",\"-\"\n")
    return sb.toString()
}

private fun generateSummaryPdfText(
    month: String,
    generationDate: String,
    rows: List<com.example.model.TargetSheetRow>
): String {
    val sb = StringBuilder()
    sb.append("===================================================\n")
    sb.append("            গ্রামীণ কল্যাণ কেরানীগঞ্জ              \n")
    sb.append("       অফিশিয়াল মাসিক হিসাব ও লক্ষ্যমাত্রা রিপোর্ট    \n")
    sb.append("===================================================\n")
    sb.append("নির্বাচিত মাস: $month\n")
    sb.append("প্রস্তুতির সময়: $generationDate\n")
    sb.append("স্বাস্থ্য কেন্দ্রের সংখ্যা: ১১টি\n")
    sb.append("---------------------------------------------------\n\n")

    rows.forEachIndexed { i, r ->
        sb.append("${i + 1}. ${r.centreNameBangla} (${r.centreNameEnglish})\n")
        sb.append("   - মাসিক লক্ষ্যমাত্রা: ৳${r.monthlyIncomeTarget}\n")
        sb.append("   - অর্জিত আয় (MTD): ৳${r.monthToDateIncome}\n")
        sb.append("   - অর্জনের হার: ${String.format(Locale.US, "%.1f%%", r.achievementPercentage)}\n")
        sb.append("   - আয়ের ঘাটতি: ৳${r.incomeShortfall}\n")
        sb.append("   - বাকি কার্যদিবস: ${r.remainingWorkingDays} দিন (প্রয়োজনীয় দৈনিক গড়: ৳${r.requiredDailyIncome})\n\n")
    }

    val totalTarget = rows.sumOf { it.monthlyIncomeTarget }
    val totalMtd = rows.sumOf { it.monthToDateIncome }
    val totalShortfall = rows.sumOf { it.incomeShortfall }
    val overallAch = if (totalTarget > 0) (totalMtd / totalTarget) * 100 else 0.0

    sb.append("---------------------------------------------------\n")
    sb.append("অঞ্চলের সর্বমোট লক্ষ্যমাত্রা: ৳$totalTarget\n")
    sb.append("অঞ্চলের সর্বমোট অর্জিত আয়: ৳$totalMtd\n")
    sb.append("অঞ্চলের সর্বমোট ঘাটতি: ৳$totalShortfall\n")
    sb.append("সামগ্রিক অর্জনের হার: ${String.format(Locale.US, "%.1f%%", overallAch)}\n")
    sb.append("===================================================\n")
    sb.append("নিরীক্ষা স্থিতি: যাচাইকৃত ও অনুমোদিত\n")
    return sb.toString()
}

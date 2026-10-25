package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppScreen(
    val titleBangla: String,
    val titleEnglish: String,
    val icon: ImageVector,
    val requiresAdmin: Boolean = false,
    val category: String = "মেইন"
) {
    LOGIN("লগইন ও ভূমিকা", "Login & Roles", Icons.Default.Lock, category = "অ্যাকাউন্ট"),
    REGIONAL_DASHBOARD("আঞ্চলিক ড্যাশবোর্ড", "Regional Dashboard", Icons.Default.Dashboard, category = "ড্যাশবোর্ড"),
    CENTRE_DASHBOARD("কেন্দ্র ড্যাশবোর্ড", "Centre Dashboard", Icons.Default.LocalHospital, category = "ড্যাশবোর্ড"),
    DAILY_REPORT_FORM("দৈনিক রিপোর্ট দাখিল", "Daily Report Form", Icons.Default.PostAdd, category = "রিপোর্টিং"),
    WEEKLY_REPORT_FORM("সাপ্তাহিক রিপোর্ট দাখিল", "Weekly Report Form", Icons.Default.DateRange, category = "রিপোর্টিং"),
    REPORT_HISTORY("রিপোর্ট ইতিহাস", "Report History", Icons.Default.History, category = "রিপোর্টিং"),
    PENDING_QUEUE("যাচাইকরণ তালিকা", "Pending Verification", Icons.Default.PendingActions, category = "হিসাব ও অডিট"),
    TARGET_MANAGEMENT("লক্ষ্যমাত্রা নির্ধারণ", "Target Management", Icons.Default.TrackChanges, requiresAdmin = true, category = "ব্যবস্থাপনা"),
    MONTHLY_REPORTS("মাসিক স্প্রেডশীট", "Monthly Reports", Icons.Default.TableChart, category = "হিসাব ও অডিট"),
    CENTRE_COMPARISON("কেন্দ্রভিত্তিক তুলনা", "Centre Comparison", Icons.Default.CompareArrows, category = "বিশ্লেষণ"),
    USER_MANAGEMENT("ব্যবহারকারী ব্যবস্থাপনা", "User & Roles", Icons.Default.People, requiresAdmin = true, category = "ব্যবস্থাপনা"),
    CENTRE_MANAGEMENT("স্বাস্থ্য কেন্দ্রসমূহ", "Centres Management", Icons.Default.Domain, requiresAdmin = true, category = "ব্যবস্থাপনা"),
    SMS_SHARING("এসএমএস শেয়ারিং", "SMS Sharing", Icons.Default.Sms, category = "যোগাযোগ"),
    EXPORT_CENTRE("রিপোর্ট এক্সপোর্ট", "Export Centre", Icons.Default.FileDownload, category = "রিপোর্টিং"),
    AUDIT_LOG("অডিট লগ", "Audit Trail", Icons.Default.Assignment, category = "হিসাব ও অডিট"),
    APP_SETTINGS("সেটিংস ও সিঙ্ক", "Settings & Sync", Icons.Default.Settings, category = "ব্যবস্থাপনা")
}

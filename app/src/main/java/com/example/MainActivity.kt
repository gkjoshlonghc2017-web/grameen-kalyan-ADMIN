package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BanglaFormatter
import com.example.ui.components.GkTopAppBar
import com.example.ui.navigation.AppScreen
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AccountingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: AccountingViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreen by remember { mutableStateOf(AppScreen.REGIONAL_DASHBOARD) }
    var screenArgsCentreId by remember { mutableStateOf<String?>(null) }

    val currentUser by viewModel.currentUser.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val pendingWeekly by viewModel.pendingWeeklyReports.collectAsState()

    // Listen for ViewModel notifications
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Handle Back Press on sub-screens
    BackHandler(enabled = currentScreen != AppScreen.REGIONAL_DASHBOARD) {
        currentScreen = AppScreen.REGIONAL_DASHBOARD
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Drawer Header
                    Surface(
                        color = GkGreenPrimary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .statusBarsPadding()
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = GkGoldSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "গ্রামীণ কল্যাণ কেরানীগঞ্জ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "ব্যবহারকারী: ${currentUser.name}\nভূমিকা: ${currentUser.role.displayNameBangla}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = GkGoldLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Group screens by category
                    val categories = listOf("ড্যাশবোর্ড", "রিপোর্টিং", "হিসাব ও অডিট", "ব্যবস্থাপনা", "অ্যাকাউন্ট")

                    categories.forEach { cat ->
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GkTextTertiary
                            ),
                            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                        )

                        AppScreen.values().filter { it.category == cat }.forEach { screen ->
                            val isSelected = currentScreen == screen
                            val isPendingQueue = screen == AppScreen.PENDING_QUEUE
                            val pendingCount = pendingWeekly.size

                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) GkGreenPrimary else GkTextSecondary
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.titleBangla,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                badge = {
                                    if (isPendingQueue && pendingCount > 0) {
                                        Badge(containerColor = GkStatusPending) {
                                            Text(
                                                text = BanglaFormatter.toBanglaDigits(pendingCount),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                },
                                selected = isSelected,
                                onClick = {
                                    currentScreen = screen
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 2.dp)
                                    .testTag("drawer_item_${screen.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                GkTopAppBar(
                    title = currentScreen.titleBangla,
                    currentUser = currentUser,
                    selectedMonth = selectedMonth,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSyncClick = { viewModel.syncWithCloud() },
                    onMonthChange = { newMonth -> viewModel.selectMonth(newMonth) }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    AppScreen.REGIONAL_DASHBOARD -> RegionalDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToCentre = { cid ->
                            viewModel.selectCentre(cid)
                            currentScreen = AppScreen.CENTRE_DASHBOARD
                        },
                        onNavigateToPending = { currentScreen = AppScreen.PENDING_QUEUE },
                        onNavigateToSpreadsheet = { currentScreen = AppScreen.MONTHLY_REPORTS }
                    )
                    AppScreen.CENTRE_DASHBOARD -> CentreDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToDailyForm = { cid ->
                            screenArgsCentreId = cid
                            currentScreen = AppScreen.DAILY_REPORT_FORM
                        },
                        onNavigateToWeeklyForm = { cid ->
                            screenArgsCentreId = cid
                            currentScreen = AppScreen.WEEKLY_REPORT_FORM
                        },
                        onNavigateToSms = { cid ->
                            screenArgsCentreId = cid
                            currentScreen = AppScreen.SMS_SHARING
                        }
                    )
                    AppScreen.DAILY_REPORT_FORM -> DailyReportFormScreen(
                        viewModel = viewModel,
                        initialCentreId = screenArgsCentreId,
                        onSubmitted = { currentScreen = AppScreen.REPORT_HISTORY }
                    )
                    AppScreen.WEEKLY_REPORT_FORM -> WeeklyReportFormScreen(
                        viewModel = viewModel,
                        initialCentreId = screenArgsCentreId,
                        onSubmitted = { currentScreen = AppScreen.REPORT_HISTORY }
                    )
                    AppScreen.REPORT_HISTORY -> ReportHistoryScreen(viewModel = viewModel)
                    AppScreen.PENDING_QUEUE -> PendingVerificationScreen(viewModel = viewModel)
                    AppScreen.TARGET_MANAGEMENT -> TargetManagementScreen(viewModel = viewModel)
                    AppScreen.MONTHLY_REPORTS -> MonthlyReportsScreen(
                        viewModel = viewModel,
                        onNavigateToExport = { currentScreen = AppScreen.EXPORT_CENTRE }
                    )
                    AppScreen.CENTRE_COMPARISON -> CentreComparisonScreen(
                        viewModel = viewModel,
                        onNavigateToCentre = { cid ->
                            viewModel.selectCentre(cid)
                            currentScreen = AppScreen.CENTRE_DASHBOARD
                        }
                    )
                    AppScreen.USER_MANAGEMENT -> UserManagementScreen(viewModel = viewModel)
                    AppScreen.CENTRE_MANAGEMENT -> CentreManagementScreen(viewModel = viewModel)
                    AppScreen.SMS_SHARING -> SmsSharingScreen(
                        viewModel = viewModel,
                        initialCentreId = screenArgsCentreId
                    )
                    AppScreen.EXPORT_CENTRE -> ExportCentreScreen(viewModel = viewModel)
                    AppScreen.AUDIT_LOG -> AuditLogScreen(viewModel = viewModel)
                    AppScreen.APP_SETTINGS -> AppSettingsScreen(viewModel = viewModel)
                    AppScreen.LOGIN -> LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { currentScreen = AppScreen.REGIONAL_DASHBOARD }
                    )
                }
            }
        }
    }
}

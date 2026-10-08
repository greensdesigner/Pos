package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.UpdateUiState
import com.example.ui.components.AppUpdateDialog
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InvoiceDialog
import com.example.ui.screens.LockScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ReturnsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.GreensStockTheme
import com.example.ui.viewmodel.GreensStockViewModel
import com.example.util.BanglaStrings
import com.example.util.EnglishStrings

enum class ScreenNav(
    val route: String,
    val titleEn: String,
    val titleBn: String,
    val icon: ImageVector
) {
    DASHBOARD("dashboard", "Dashboard", "ড্যাশবোর্ড", Icons.Default.Dashboard),
    POS("pos", "POS", "বিক্রয়", Icons.Default.PointOfSale),
    INVENTORY("inventory", "Inventory", "স্টক", Icons.Default.Inventory2),
    RETURNS("returns", "Returns", "ফেরত", Icons.Default.AssignmentReturn),
    EXPENSES("expenses", "Expenses", "খরচ", Icons.Default.AccountBalanceWallet),
    LEDGER("ledger", "Dues", "বাকি খাতা", Icons.Default.People),
    REPORTS("reports", "Reports", "রিপোর্ট", Icons.Default.Assessment),
    SETTINGS("settings", "Settings", "সেটিংস", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: GreensStockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GreensStockTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: GreensStockViewModel) {
    val isBangla by viewModel.isBangla.collectAsStateWithLifecycle()
    val isTerminalLocked by viewModel.isTerminalLocked.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val activeInvoice by viewModel.activeInvoice.collectAsStateWithLifecycle()
    val storeConfig by viewModel.storeConfig.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val updateUiState by viewModel.updateUiState.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(ScreenNav.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // App In-App Auto Update Dialog Overlay
    if (updateUiState is UpdateUiState.UpdateAvailable ||
        updateUiState is UpdateUiState.Downloading ||
        updateUiState is UpdateUiState.ReadyToInstall
    ) {
        AppUpdateDialog(
            updateUiState = updateUiState,
            currentVersionName = viewModel.currentVersionName,
            isBangla = isBangla,
            onUpdateNow = { info -> viewModel.downloadAndInstallUpdate(info) },
            onInstallNow = { file -> viewModel.installDownloadedApk(file) },
            onDismiss = { viewModel.dismissUpdatePrompt() }
        )
    }

    // Terminal Quick Lock Screen Overlay
    if (isTerminalLocked) {
        BackHandler { /* Prevent backing out of lock screen */ }
        LockScreen(
            currentRole = currentRole,
            isBangla = isBangla,
            onUnlock = { pin -> viewModel.unlockTerminal(pin) }
        )
        return
    }

    // Invoice Receipt Dialog
    if (activeInvoice != null) {
        InvoiceDialog(
            sale = activeInvoice!!.first,
            items = activeInvoice!!.second,
            storeConfig = storeConfig,
            isBangla = isBangla,
            onDismiss = { viewModel.closeInvoiceDialog() }
        )
    }

    // Handle back button to return to dashboard
    BackHandler(enabled = currentScreen != ScreenNav.DASHBOARD) {
        currentScreen = ScreenNav.DASHBOARD
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        val bottomTabs = listOf(
                            ScreenNav.DASHBOARD,
                            ScreenNav.POS,
                            ScreenNav.INVENTORY,
                            ScreenNav.LEDGER,
                            ScreenNav.SETTINGS
                        )
                        bottomTabs.forEach { screen ->
                            NavigationBarItem(
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                icon = { Icon(screen.icon, contentDescription = screen.titleEn) },
                                label = { Text(if (isBangla) screen.titleBn else screen.titleEn) },
                                modifier = Modifier.testTag("nav_item_${screen.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Adaptive NavigationRail for tablet / wide screens
                if (isWideScreen) {
                    NavigationRail(
                        modifier = Modifier.testTag("navigation_rail")
                    ) {
                        ScreenNav.values().forEach { screen ->
                            NavigationRailItem(
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                icon = { Icon(screen.icon, contentDescription = screen.titleEn) },
                                label = { Text(if (isBangla) screen.titleBn else screen.titleEn) },
                                modifier = Modifier.testTag("rail_item_${screen.route}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (currentScreen) {
                        ScreenNav.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToPos = { currentScreen = ScreenNav.POS },
                            onNavigateToInventory = { currentScreen = ScreenNav.INVENTORY },
                            onNavigateToSettings = { currentScreen = ScreenNav.SETTINGS },
                            onNavigateToReports = { currentScreen = ScreenNav.REPORTS },
                            onNavigateToExpenses = { currentScreen = ScreenNav.EXPENSES },
                            onNavigateToReturns = { currentScreen = ScreenNav.RETURNS }
                        )
                        ScreenNav.POS -> PosScreen(viewModel = viewModel)
                        ScreenNav.INVENTORY -> InventoryScreen(viewModel = viewModel)
                        ScreenNav.RETURNS -> ReturnsScreen(viewModel = viewModel)
                        ScreenNav.EXPENSES -> ExpensesScreen(viewModel = viewModel)
                        ScreenNav.LEDGER -> CustomersScreen(viewModel = viewModel)
                        ScreenNav.REPORTS -> ReportsScreen(viewModel = viewModel)
                        ScreenNav.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

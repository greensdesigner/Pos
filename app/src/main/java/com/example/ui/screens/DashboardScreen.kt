package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.saas.CloudSyncState
import com.example.model.FinancialMetrics
import com.example.model.UserRole
import com.example.ui.theme.EmeraldDarkPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.LossContainer
import com.example.ui.theme.LossOnContainer
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitOnContainer
import com.example.ui.viewmodel.DateFilter
import com.example.ui.viewmodel.GreensStockViewModel
import com.example.util.BanglaStrings
import com.example.util.EnglishStrings
import com.example.util.Localization
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: GreensStockViewModel,
    onNavigateToPos: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToReturns: () -> Unit = {}
) {
    val isBangla by viewModel.isBangla.collectAsStateWithLifecycle()
    val strings = if (isBangla) BanglaStrings else EnglishStrings
    val metrics by viewModel.financialMetrics.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val filteredSales by viewModel.filteredSales.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val customDateRange by viewModel.customDateRange.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val storeConfig by viewModel.storeConfig.collectAsStateWithLifecycle()

    var showCustomDateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header: Store Info, Active Role, and Terminal Lock
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.appName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isBangla) currentRole.titleBn else currentRole.titleEn,
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("lang_toggle_btn")
                    ) {
                        Text(
                            text = if (isBangla) "EN" else "বাং",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("top_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = EmeraldPrimary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.lockTerminal() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("lock_terminal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Terminal",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Hostinger Multi-Tenant SaaS Cloud Sync Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hostinger_saas_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when (syncState) {
                                CloudSyncState.SYNCING -> Color(0xFFF59E0B)
                                CloudSyncState.SUCCESS -> ProfitGreen
                                CloudSyncState.ERROR -> LossRed
                                else -> EmeraldPrimary
                            }
                        ) {
                            Box(modifier = Modifier.size(10.dp))
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Hostinger SaaS Cloud",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(shape = RoundedCornerShape(4.dp), color = EmeraldPrimary.copy(alpha = 0.15f)) {
                                    Text(
                                        text = storeConfig?.tenantId ?: "GS-STORE-01",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = when (syncState) {
                                    CloudSyncState.SYNCING -> if (isBangla) "Hostinger ক্লাউডে সিঙ্ক হচ্ছে..." else "Syncing with Hostinger..."
                                    CloudSyncState.SUCCESS -> if (isBangla) "হোস্টিংগার ক্লাউড ডাটাবেস সিঙ্কড ✓" else "Hostinger Cloud Database Synced ✓"
                                    CloudSyncState.ERROR -> if (isBangla) "সিঙ্ক সংযোগ ব্যর্থ (অফলাইন মোড চালু)" else "Sync Failed (Operating in Offline Mode)"
                                    else -> if (isBangla) "রুম অফলাইন-ফার্স্ট • ক্লাউড সিঙ্ক প্রস্তুত" else "Room Offline-First • Cloud Sync Ready"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToSettings,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("banner_settings_btn")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(13.dp), tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(if (isBangla) "সেটিংস" else "Setup", fontSize = 11.sp, color = EmeraldPrimary)
                        }

                        Button(
                            onClick = { viewModel.syncWithHostinger() },
                            enabled = syncState != CloudSyncState.SYNCING,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("dashboard_sync_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            if (syncState == CloudSyncState.SYNCING) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBangla) "সিঙ্ক" else "Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Navigation Shortcuts
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_shortcuts_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBangla) "কুইক অ্যাকশন ও মডিউলসমূহ" else "Quick Actions & Modules",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickActionItem(
                            icon = Icons.Default.PointOfSale,
                            label = if (isBangla) "বিক্রয়" else "POS",
                            onClick = onNavigateToPos,
                            badgeColor = EmeraldPrimary
                        )
                        QuickActionItem(
                            icon = Icons.Default.Inventory2,
                            label = if (isBangla) "স্টক" else "Stock",
                            onClick = onNavigateToInventory,
                            badgeColor = Color(0xFF0284C7)
                        )
                        QuickActionItem(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = if (isBangla) "খরচ" else "Expense",
                            onClick = onNavigateToExpenses,
                            badgeColor = Color(0xFFD97706)
                        )
                        QuickActionItem(
                            icon = Icons.Default.Assessment,
                            label = if (isBangla) "রিপোর্ট" else "Reports",
                            onClick = onNavigateToReports,
                            badgeColor = Color(0xFF7C3AED)
                        )
                        QuickActionItem(
                            icon = Icons.Default.Settings,
                            label = if (isBangla) "সেটিংস" else "Settings",
                            onClick = onNavigateToSettings,
                            badgeColor = Color(0xFF059669)
                        )
                    }
                }
            }
        }

        // Date Filter Chips & Custom Date Indicator
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DateFilter.values()) { filter ->
                        val isSelected = dateFilter == filter
                        ElevatedFilterChip(
                            selected = isSelected,
                            onClick = {
                                if (filter == DateFilter.CUSTOM) {
                                    showCustomDateDialog = true
                                } else {
                                    viewModel.setDateFilter(filter)
                                }
                            },
                            leadingIcon = if (filter == DateFilter.CUSTOM) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            label = { Text(if (isBangla) filter.labelBn else filter.labelEn) },
                            modifier = Modifier.testTag("filter_chip_${filter.name}")
                        )
                    }
                }

                // If CUSTOM filter is active, show the selected date range banner with edit action
                if (dateFilter == DateFilter.CUSTOM) {
                    val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.US)
                    val startStr = dateFmt.format(Date(customDateRange.first))
                    val endStr = dateFmt.format(Date(customDateRange.second))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldPrimary.copy(alpha = 0.12f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomDateDialog = true }
                            .testTag("custom_date_indicator")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isBangla) "নির্বাচিত তারিখ: $startStr হতে $endStr" else "Selected Range: $startStr to $endStr",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = if (isBangla) "তারিখ পরিবর্তন করুন" else "Change Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 6 Financial Overview Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Net Revenue & Current Profit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinancialCard(
                        title = strings.netRevenue,
                        amount = Localization.formatCurrency(metrics.grossRevenue, isBangla),
                        subtitle = "${metrics.totalOrders} ${if (isBangla) "অর্ডার" else "Orders"}",
                        icon = Icons.Default.AttachMoney,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    // Current Profit (Only visible to Admin / Manager)
                    if (currentRole.canViewProfitMargins) {
                        FinancialCard(
                            title = strings.currentProfit,
                            amount = Localization.formatCurrency(metrics.currentProfit, isBangla),
                            subtitle = if (isBangla) "ক্ষতি সমন্বয় পরবর্তী" else "Net of Loss Offset",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            containerColor = ProfitContainer,
                            contentColor = ProfitOnContainer,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        FinancialCard(
                            title = strings.currentProfit,
                            amount = "••••••",
                            subtitle = if (isBangla) "স্টাফের অনুমতি নেই" else "Restricted (Staff)",
                            icon = Icons.Default.Lock,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Row 2: Current Loss (with Automated Offset Rule) & Final Net Profit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Crucial Current Loss Card
                    if (currentRole.canViewProfitMargins) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("current_loss_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (metrics.isLossCovered) ProfitContainer.copy(alpha = 0.6f) else LossContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.currentLoss,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (metrics.isLossCovered) ProfitOnContainer else LossOnContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        imageVector = if (metrics.isLossCovered) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = if (metrics.isLossCovered) ProfitGreen else LossRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = Localization.formatCurrency(metrics.currentLoss, isBangla),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (metrics.isLossCovered) ProfitOnContainer else LossOnContainer
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Automated Loss Covered Badge
                                if (metrics.isLossCovered) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ProfitGreen
                                    ) {
                                        Text(
                                            text = strings.lossCoveredBadge,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = LossRed
                                    ) {
                                        Text(
                                            text = strings.uncoveredLossBadge,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        FinancialCard(
                            title = strings.currentLoss,
                            amount = "••••••",
                            subtitle = if (isBangla) "স্টাফের অনুমতি নেই" else "Restricted",
                            icon = Icons.Default.Lock,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Final Net Profit
                    if (currentRole.canViewFinancialReports) {
                        val isPositiveNet = metrics.netProfit >= 0
                        FinancialCard(
                            title = strings.netProfit,
                            amount = Localization.formatCurrency(metrics.netProfit, isBangla),
                            subtitle = if (isBangla) "খরচ ও রিফান্ড বাদে" else "After Exp. & Refunds",
                            icon = if (isPositiveNet) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            containerColor = if (isPositiveNet) MaterialTheme.colorScheme.secondaryContainer else LossContainer,
                            contentColor = if (isPositiveNet) MaterialTheme.colorScheme.onSecondaryContainer else LossOnContainer,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        FinancialCard(
                            title = strings.netProfit,
                            amount = "••••••",
                            subtitle = if (isBangla) "মালিকের ড্যাশবোর্ড" else "Admin Only",
                            icon = Icons.Default.Lock,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Row 3: Total Expenses & Total Refunds
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinancialCard(
                        title = strings.totalExpenses,
                        amount = Localization.formatCurrency(metrics.totalExpenses, isBangla),
                        subtitle = if (isBangla) "দোকানের সকল খরচ" else "All Operational Costs",
                        icon = Icons.Default.AccountBalanceWallet,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    FinancialCard(
                        title = strings.totalRefunds,
                        amount = Localization.formatCurrency(metrics.totalRefunds, isBangla),
                        subtitle = if (isBangla) "ফেরতকৃত পণ্য" else "Returned Products",
                        icon = Icons.Default.AssignmentReturn,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Automated Loss Coverage Explanation & Breakdown Card
        if (currentRole.canViewProfitMargins) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "লাভ-ক্ষতি সমন্বয় ও কাভারেজ বিশ্লেষণ" else "Profit-Loss Coverage Analysis",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (metrics.isLossCovered) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ProfitContainer
                                ) {
                                    Text(
                                        text = strings.lossCoveredBadge,
                                        color = ProfitOnContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Ratio calculation
                        val totalSalesVolume = metrics.grossSalesProfit + metrics.grossSalesLoss
                        val profitRatio = if (totalSalesVolume > 0) (metrics.grossSalesProfit / totalSalesVolume).toFloat() else 1f

                        LinearProgressIndicator(
                            progress = { profitRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ProfitGreen,
                            trackColor = LossRed
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${strings.grossSalesProfit}: ${Localization.formatCurrency(metrics.grossSalesProfit, isBangla)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ProfitGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${strings.grossSalesLoss}: ${Localization.formatCurrency(metrics.grossSalesLoss, isBangla)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = LossRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBangla) {
                                if (metrics.isLossCovered)
                                    "✓ স্বয়ংক্রিয় সমন্বয় নীতি: লাভ ক্ষতির চেয়ে বেশি হওয়ায় বর্তমান ক্ষতি স্বয়ংক্রিয়ভাবে ৳০.০০ দেখাচ্ছে এবং অবশিষ্ট নিট লাভ যোগ হচ্ছে।"
                                else
                                    "⚠ সতর্কবার্তা: ক্ষতি লাভের চেয়ে বেশি হওয়ায় অবশিষ্ট অপূরিত ক্ষতি বর্তমান ক্ষতি হিসেবে প্রদর্শিত হচ্ছে।"
                            } else {
                                if (metrics.isLossCovered)
                                    "✓ Automated Offset Rule: Total profit exceeds loss; current loss is ৳0.00 and remaining profit is protected."
                                else
                                    "⚠ Notice: Losses exceed profit. Uncovered balance is displayed under Current Loss."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Interactive Sales & Profit Trend Canvas Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "বিক্রয় ও মুনাফার ট্রেন্ড চার্ট" else "Sales & Profit Performance Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBangla) "দৈনিক বিক্রয় গতিবিধি এবং প্রফিট লাইন" else "Daily sales volume with profit trajectories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Canvas Chart
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        val points = listOf(220f, 480f, 310f, 650f, 520f, 780f, 920f)
                        val profitPoints = listOf(70f, 150f, 90f, 210f, 170f, 260f, 310f)

                        val maxVal = 1000f
                        val width = size.width
                        val height = size.height

                        // Grid lines
                        for (i in 1..3) {
                            val y = height * (i / 4f)
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f
                            )
                        }

                        // Sales Area & Line
                        val salesPath = Path()
                        val stepX = width / (points.size - 1)
                        points.forEachIndexed { index, value ->
                            val x = index * stepX
                            val y = height - (value / maxVal * height)
                            if (index == 0) salesPath.moveTo(x, y) else salesPath.lineTo(x, y)
                        }

                        // Gradient fill
                        val fillPath = Path().apply {
                            addPath(salesPath)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(EmeraldPrimary.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )

                        // Sales line stroke
                        drawPath(
                            path = salesPath,
                            color = EmeraldPrimary,
                            style = Stroke(width = 4f, cap = StrokeCap.Round)
                        )

                        // Profit line stroke
                        val profitPath = Path()
                        profitPoints.forEachIndexed { index, value ->
                            val x = index * stepX
                            val y = height - (value / maxVal * height)
                            if (index == 0) profitPath.moveTo(x, y) else profitPath.lineTo(x, y)
                            drawCircle(color = ProfitGreen, radius = 5f, center = Offset(x, y))
                        }
                        drawPath(
                            path = profitPath,
                            color = ProfitGreen,
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(10.dp).background(EmeraldPrimary, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "মোট বিক্রয়" else "Gross Sales", fontSize = 12.sp)

                        Spacer(modifier = Modifier.width(20.dp))

                        Box(modifier = Modifier.size(10.dp).background(ProfitGreen, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "নিট লাভ" else "Net Profit", fontSize = 12.sp)
                    }
                }
            }
        }

        // Low Stock Warnings Widget
        if (lowStockProducts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToInventory() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = LossRed
                                )
                                Text(
                                    text = if (isBangla) "স্বল্প স্টক সতর্কতা (${lowStockProducts.size}টি পণ্য)" else "Low Stock Alerts (${lowStockProducts.size} items)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (isBangla) "দেখুন >" else "View >",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        lowStockProducts.take(3).forEach { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isBangla && product.banglaName.isNotBlank()) product.banglaName else product.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${product.stockQuantity} ${product.unit} (Alert: ${product.minStockLevel})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LossRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Sales Activity
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "সাম্প্রতিক বিক্রয়সমূহ" else "Recent Sales Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val displaySales = if (filteredSales.isNotEmpty()) filteredSales else sales
                    if (displaySales.isEmpty()) {
                        Text(
                            text = if (isBangla) "কোন বিক্রয় রেকর্ড নেই" else "No sales recorded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        displaySales.take(5).forEach { sale ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = sale.invoiceNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${sale.customerName} • ${sale.paymentMethod}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Localization.formatCurrency(sale.netPayable, isBangla),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                    if (sale.dueAmount > 0) {
                                        Text(
                                            text = "Due: ${Localization.formatCompactCurrency(sale.dueAmount, isBangla)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LossRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomDateDialog) {
        CustomDateRangeDialog(
            initialStart = customDateRange.first,
            initialEnd = customDateRange.second,
            isBangla = isBangla,
            onDismiss = { showCustomDateDialog = false },
            onApply = { start, end ->
                viewModel.setCustomDateRange(start, end)
                showCustomDateDialog = false
            }
        )
    }
}

@Composable
fun CustomDateRangeDialog(
    initialStart: Long,
    initialEnd: Long,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onApply: (Long, Long) -> Unit
) {
    var startDate by remember { mutableLongStateOf(initialStart) }
    var endDate by remember { mutableLongStateOf(initialEnd) }

    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("custom_date_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "কাস্টম তারিখ নির্ধারণ" else "Select Custom Date Range",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Close")
                    }
                }

                // Quick Presets
                Text(
                    text = if (isBangla) "দ্রুত বাছাই (Presets):" else "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                val now = System.currentTimeMillis()
                val presets = listOf(
                    Triple(
                        if (isBangla) "গতকাল" else "Yesterday",
                        run {
                            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                            cal.timeInMillis
                        },
                        run {
                            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
                            cal.timeInMillis
                        }
                    ),
                    Triple(
                        if (isBangla) "গত ৩ দিন" else "Last 3 Days",
                        now - 3 * 86400000L,
                        now
                    ),
                    Triple(
                        if (isBangla) "চলতি মাস" else "This Month",
                        run {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.DAY_OF_MONTH, 1)
                            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                            cal.timeInMillis
                        },
                        now
                    ),
                    Triple(
                        if (isBangla) "গত মাস" else "Last Month",
                        run {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.MONTH, -1)
                            cal.set(Calendar.DAY_OF_MONTH, 1)
                            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                            cal.timeInMillis
                        },
                        run {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.DAY_OF_MONTH, 1)
                            cal.add(Calendar.DAY_OF_MONTH, -1)
                            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
                            cal.timeInMillis
                        }
                    )
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presets) { (label, start, end) ->
                        ElevatedFilterChip(
                            selected = false,
                            onClick = {
                                startDate = start
                                endDate = end
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                // Date Cards with manual adjustment buttons
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBangla) "শুরুর তারিখ (Start Date)" else "Start Date",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dateFmt.format(Date(startDate)),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                            Row {
                                OutlinedButton(
                                    onClick = { startDate -= 86400000L },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) { Text(if (isBangla) "-১ দিন" else "-1 Day", fontSize = 11.sp) }
                                Spacer(modifier = Modifier.width(4.dp))
                                OutlinedButton(
                                    onClick = { if (startDate + 86400000L <= endDate) startDate += 86400000L },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) { Text(if (isBangla) "+১ দিন" else "+1 Day", fontSize = 11.sp) }
                            }
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBangla) "শেষ তারিখ (End Date)" else "End Date",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dateFmt.format(Date(endDate)),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                            Row {
                                OutlinedButton(
                                    onClick = { if (endDate - 86400000L >= startDate) endDate -= 86400000L },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) { Text(if (isBangla) "-১ দিন" else "-1 Day", fontSize = 11.sp) }
                                Spacer(modifier = Modifier.width(4.dp))
                                OutlinedButton(
                                    onClick = { endDate += 86400000L },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) { Text(if (isBangla) "+১ দিন" else "+1 Day", fontSize = 11.sp) }
                            }
                        }
                    }
                }

                // Duration Summary
                val daysDiff = ((endDate - startDate) / 86400000L).coerceAtLeast(1)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPrimary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBangla) "মোট নির্বাচিত সময়কাল: ${Localization.toBanglaDigits(daysDiff.toString())} দিন" else "Total Selected: $daysDiff days",
                        color = EmeraldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                        Text(if (isBangla) "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onApply(startDate, endDate) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("apply_custom_date_btn")
                    ) {
                        Text(if (isBangla) "প্রয়োগ করুন" else "Apply Range")
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialCard(
    title: String,
    amount: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.75f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    badgeColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = badgeColor.copy(alpha = 0.12f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import com.example.model.UpdateUiState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.StoreConfigEntity
import com.example.data.saas.CloudSyncState
import com.example.model.UserRole
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.GreensStockViewModel
import com.example.util.BanglaStrings
import com.example.util.EnglishStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: GreensStockViewModel) {
    val isBangla by viewModel.isBangla.collectAsStateWithLifecycle()
    val strings = if (isBangla) BanglaStrings else EnglishStrings
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val storeConfig by viewModel.storeConfig.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val isTestingConnection by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val testConnectionResult by viewModel.testConnectionResult.collectAsStateWithLifecycle()
    val isInspectingDatabase by viewModel.isInspectingDatabase.collectAsStateWithLifecycle()
    val databaseInspectionResult by viewModel.databaseInspectionResult.collectAsStateWithLifecycle()
    val isPullingData by viewModel.isPullingData.collectAsStateWithLifecycle()
    val updateUiState by viewModel.updateUiState.collectAsStateWithLifecycle()
    val isDownloadingUpdate by viewModel.isDownloadingUpdate.collectAsStateWithLifecycle()

    var storeName by remember(storeConfig) { mutableStateOf(storeConfig?.storeName ?: "GreensStock Supermart") }
    var banglaStoreName by remember(storeConfig) { mutableStateOf(storeConfig?.banglaStoreName ?: "গ্রিনসস্টক সুপারমার্ট") }
    var phone by remember(storeConfig) { mutableStateOf(storeConfig?.phone ?: "+880 1712-345678") }
    var address by remember(storeConfig) { mutableStateOf(storeConfig?.address ?: "House #12, Road #4, Dhanmondi, Dhaka") }
    var receiptFooter by remember(storeConfig) { mutableStateOf(storeConfig?.receiptFooter ?: "Thank you! Please come again.") }
    var terminalPin by remember(storeConfig) { mutableStateOf(storeConfig?.terminalPin ?: "1234") }

    // Hostinger Multi-Tenant SaaS State
    var tenantId by remember(storeConfig) { mutableStateOf(storeConfig?.tenantId ?: "GS-STORE-01") }
    var hostingerUrl by remember(storeConfig) { mutableStateOf(storeConfig?.hostingerServerUrl ?: "https://yourdomain.com/greensstock_api.php") }
    var hostingerApiKey by remember(storeConfig) { mutableStateOf(storeConfig?.hostingerApiKey ?: "gs_hostinger_secret_key") }
    var outletName by remember(storeConfig) { mutableStateOf(storeConfig?.outletName ?: "Main Branch") }

    var showHostingerGuideDialog by remember { mutableStateOf(false) }
    var customManifestUrl by remember(storeConfig) { mutableStateOf(storeConfig?.customUpdateManifestUrl ?: "") }
    var showUpdateGuideDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hostinger Multi-Tenant SaaS Cloud Database Hub
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hostinger_saas_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = EmeraldPrimary)
                            Column {
                                Text(
                                    text = if (isBangla) "Hostinger ক্লাউড ডাটাবেস (SaaS)" else "Hostinger Cloud Database (SaaS)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBangla) "মাল্টি-টেন্যান্ট সিঙ্ক • যেকোনো ডিভাইসে লাইভ" else "Multi-Tenant Cloud Sync Engine",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Subscription Badge
                        Surface(shape = RoundedCornerShape(6.dp), color = EmeraldPrimary) {
                            Text(
                                text = storeConfig?.subscriptionPlan ?: "ENTERPRISE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Tenant ID / Store Code
                    OutlinedTextField(
                        value = tenantId,
                        onValueChange = { tenantId = it },
                        label = { Text(if (isBangla) "দোকানের টেন্যান্ট আইডি (Tenant ID / Store Code)" else "Store Tenant ID (e.g. GS-STORE-01)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Hostinger Server URL
                    OutlinedTextField(
                        value = hostingerUrl,
                        onValueChange = { hostingerUrl = it },
                        label = { Text(if (isBangla) "Hostinger API URL (আপনার ডোমেইন)" else "Hostinger API URL") },
                        placeholder = { Text("https://yourdomain.com/greensstock_api.php") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Hostinger API Secret Key
                    OutlinedTextField(
                        value = hostingerApiKey,
                        onValueChange = { hostingerApiKey = it },
                        label = { Text(if (isBangla) "Hostinger API Secret Key" else "Hostinger API Secret Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Outlet / Branch Name
                    OutlinedTextField(
                        value = outletName,
                        onValueChange = { outletName = it },
                        label = { Text(if (isBangla) "আউটলেট বা শাখার নাম" else "Outlet / Branch Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Test Connection Result feedback
                    if (testConnectionResult != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (testConnectionResult!!.startsWith("✓")) ProfitGreen.copy(alpha = 0.12f) else LossRed.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = testConnectionResult!!,
                                color = if (testConnectionResult!!.startsWith("✓")) ProfitGreen else LossRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.testHostingerConnection(hostingerUrl) },
                            enabled = !isTestingConnection,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text(if (isBangla) "সংযোগ টেস্ট করুন" else "Test Connection", fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.updateSaaSCredentials(tenantId, hostingerUrl, hostingerApiKey, outletName)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isBangla) "ক্রেডেনশিয়াল সংরক্ষণ" else "Save Settings", fontSize = 11.sp)
                        }
                    }

                    // Sync Now Button
                    Button(
                        onClick = { viewModel.syncWithHostinger(tenantId, hostingerUrl, hostingerApiKey) },
                        enabled = syncState != CloudSyncState.SYNCING,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (syncState == CloudSyncState.SYNCING) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBangla) "Hostinger-এ সিঙ্ক হচ্ছে..." else "Syncing with Hostinger...")
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBangla) "এখনই Hostinger ক্লাউডে সিঙ্ক করুন" else "Sync to Hostinger Cloud Now")
                        }
                    }

                    // Existing Web Database Bridge Tools
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldPrimary.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = if (isBangla) "বিদ্যমান ওয়েব ডাটাবেজ ইন্টিগ্রেশন (Web DB Bridge)" else "Existing Web Database Integration",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                            Text(
                                text = if (isBangla) "একই Hostinger ডাটাবেজে ওয়েব ও অ্যান্ড্রয়েড অ্যাপ একসাথে চলবে। নিচের টুল দিয়ে টেবিল চেক করুন ও ডাটা নামান:"
                                else "Share the same Hostinger MySQL database between your existing web app & this Android app:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.inspectHostingerDatabase(hostingerUrl) },
                                    enabled = !isInspectingDatabase,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    if (isInspectingDatabase) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text(if (isBangla) "টেবিল স্ক্যান করুন" else "Inspect Tables", fontSize = 11.sp)
                                    }
                                }

                                Button(
                                    onClick = { viewModel.pullDataFromHostinger(tenantId, hostingerUrl, hostingerApiKey) },
                                    enabled = !isPullingData,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    if (isPullingData) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text(if (isBangla) "ওয়েব ডাটা আনুন" else "Pull Web Data", fontSize = 11.sp)
                                    }
                                }
                            }

                            // Database Inspection Result Card
                            if (databaseInspectionResult != null) {
                                val ins = databaseInspectionResult!!
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (ins.success) ProfitGreen.copy(alpha = 0.12f) else LossRed.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = if (ins.success) "✓ ডাটাবেস: ${ins.databaseName}" else "✕ " + ins.message,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (ins.success) ProfitGreen else LossRed
                                        )
                                        if (ins.success) {
                                            Text(
                                                text = if (isBangla) "প্রাপ্ত টেবিলসমূহ: ${ins.detectedTables.joinToString(", ")}"
                                                else "Tables: ${ins.detectedTables.joinToString(", ")}",
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = if (isBangla) "পণ্য সংখ্যা: ${ins.totalProductsFound}টি (টেবিল: ${ins.detectedProductTable})"
                                                else "Total Products: ${ins.totalProductsFound} (table: ${ins.detectedProductTable})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            // 2-Way Full Sync Button
                            Button(
                                onClick = { viewModel.twoWaySyncWithHostinger(tenantId, hostingerUrl, hostingerApiKey) },
                                enabled = syncState != CloudSyncState.SYNCING,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBangla) "দ্বিমুখী পূর্ণাঙ্গ সিঙ্ক (Two-Way Sync)" else "Two-Way Full Sync",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Hostinger Setup Guide & Scripts Button
                    OutlinedButton(
                        onClick = { showHostingerGuideDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBangla) "হোস্টিংগার সেটআপ গাইড ও পিএইচপি কোড দেখুন" else "Hostinger Setup Guide & PHP Code",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Last Sync Time Display
                    if (storeConfig != null && storeConfig!!.lastSyncTimestamp > 0) {
                        val lastSyncStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(storeConfig!!.lastSyncTimestamp))
                        Text(
                            text = if (isBangla) "সর্বশেষ সফল সিঙ্ক: $lastSyncStr" else "Last Successful Sync: $lastSyncStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // App Auto Update Management Hub (স্বয়ংক্রিয় আপডেট সেন্টার)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_update_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(EmeraldPrimary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isBangla) "অ্যাপ অটো আপডেট (Auto Update)" else "App Auto Update",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBangla) "স্বয়ংক্রিয় সংস্করণ পরীক্ষা ও ১-ক্লিকে ইনস্টলেশন" else "Automated version checks & OTA updates",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Version Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "v${viewModel.currentVersionName} (Build ${viewModel.currentVersionCode})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Auto-Update Switch Toggle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBangla) "স্বয়ংক্রিয় আপডেট চেক সক্রিয় রাখুন" else "Automatic Update Checks",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isBangla) "অ্যাপ ওপেন করার সময় নতুন কোনো আপডেট থাকলে স্বয়ংক্রিয়ভাবে নোটিফাই করবে"
                                    else "Checks for new releases on startup and notifies automatically",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Switch(
                                checked = storeConfig?.autoCheckUpdates ?: true,
                                onCheckedChange = { viewModel.toggleAutoCheckUpdates(it) },
                                modifier = Modifier.testTag("auto_update_switch")
                            )
                        }
                    }

                    // Live Update Status Banner
                    when (val state = updateUiState) {
                        is UpdateUiState.Checking -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldPrimary.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = EmeraldPrimary
                                    )
                                    Text(
                                        text = if (isBangla) "সার্ভার থেকে নতুন ভার্সন চেক করা হচ্ছে..." else "Checking server for new version releases...",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        is UpdateUiState.UpToDate -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ProfitGreen.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text(
                                            text = if (isBangla) "আপনার অ্যাপটি সম্পূর্ণ আপ-টু-ডেট আছে (v${state.currentVersionName})!"
                                            else "App is completely up to date (v${state.currentVersionName})!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ProfitGreen
                                        )
                                        Text(
                                            text = if (isBangla) "নতুন কোনো আপডেটের প্রয়োজন নেই। সমস্ত ফিচার সচল আছে।"
                                            else "No new updates needed at this time.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        is UpdateUiState.UpdateAvailable -> {
                            val info = state.updateInfo
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = EmeraldPrimary.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.NewReleases, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                            Text(
                                                text = if (isBangla) "নতুন সংস্করণ উপলব্ধ: v${info.latestVersionName}" else "New Version Available: v${info.latestVersionName}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldPrimary
                                            )
                                        }

                                        if (info.formattedSize.isNotBlank()) {
                                            Surface(shape = RoundedCornerShape(6.dp), color = EmeraldPrimary) {
                                                Text(
                                                    text = info.formattedSize,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (info.releaseNotes.isNotBlank()) {
                                        Text(
                                            text = info.releaseNotes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.downloadAndInstallUpdate(info) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isBangla) "স্বয়ংক্রিয় ডাউনলোড ও ইনস্টল করুন" else "Download & Install Update",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        is UpdateUiState.Downloading -> {
                            val percent = state.progressPercent
                            val downloadedMb = String.format(Locale.US, "%.1f", state.downloadedBytes / (1024.0 * 1024.0))
                            val totalMb = String.format(Locale.US, "%.1f", state.totalBytes / (1024.0 * 1024.0))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldPrimary.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isBangla) "নতুন ভার্সন ডাউনলোড হচ্ছে..." else "Downloading update...",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "$percent%",
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary,
                                            fontSize = 12.sp
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { (percent / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp),
                                        color = EmeraldPrimary
                                    )

                                    Text(
                                        text = "$downloadedMb MB / $totalMb MB • " +
                                                if (isBangla) "ডাউনলোড শেষে স্বয়ংক্রিয়ভাবে ইনস্টলার চালু হবে"
                                                else "Installer will launch upon completion",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        is UpdateUiState.ReadyToInstall -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ProfitGreen.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isBangla) "✓ নতুন APK ডাউনলোড সম্পন্ন হয়েছে!" else "✓ Download Complete!",
                                        fontWeight = FontWeight.Bold,
                                        color = ProfitGreen
                                    )
                                    Button(
                                        onClick = { viewModel.installDownloadedApk(state.apkFile) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(if (isBangla) "এখনই ইনস্টল করুন (Install Now)" else "Install Now")
                                    }
                                }
                            }
                        }

                        is UpdateUiState.Error -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LossRed.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = LossRed, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LossRed,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        else -> {}
                    }

                    // Action Buttons (Check Now & Simulate Test)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.checkForAppUpdates(
                                    isManual = true,
                                    customServerUrl = hostingerUrl,
                                    customManifestUrl = customManifestUrl
                                )
                            },
                            enabled = updateUiState !is UpdateUiState.Checking && !isDownloadingUpdate,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("check_updates_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (updateUiState is UpdateUiState.Checking) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBangla) "আপডেট চেক করুন" else "Check Updates",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.simulateUpdateAvailableForTesting() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_update_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBangla) "টেস্ট ডেমো" else "Test Demo",
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Optional Custom Update Manifest URL
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customManifestUrl,
                                onValueChange = { customManifestUrl = it },
                                label = { Text(if (isBangla) "কাস্টম আপডেট URL / JSON (ঐচ্ছিক)" else "Custom Update Manifest / JSON (Optional)", fontSize = 11.sp) },
                                placeholder = { Text("https://yourdomain.com/version.json", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = { viewModel.updateCustomManifestUrl(customManifestUrl) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (isBangla) "সংরক্ষণ" else "Save", fontSize = 11.sp)
                            }
                        }

                        Text(
                            text = if (isBangla) "ডিফল্টভাবে আপনার Hostinger ক্লাউড ডাটাবেজ API (greensstock_api.php?action=check_update) থেকে আপডেট চেক হয়।"
                            else "Default checks via Hostinger SaaS API (greensstock_api.php?action=check_update).",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Last Update Check Time
                    if (storeConfig != null && storeConfig!!.lastUpdateCheckTimestamp > 0) {
                        val lastCheckStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(storeConfig!!.lastUpdateCheckTimestamp))
                        Text(
                            text = if (isBangla) "সর্বশেষ আপডেট চেক: $lastCheckStr" else "Last Update Check: $lastCheckStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // How Auto-Update Works Info Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (isBangla) "অটো আপডেট কীভাবে কাজ করে?" else "How Auto Update Works?",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (isBangla)
                                    "১. নতুন কোনো সংস্করণ বা ফিচার রিলিজ হলে অ্যাপ চালু করতেই স্ক্রিনে আপডেট ডায়ালগ চলে আসবে।\n" +
                                            "২. ব্যবহারকারীকে ম্যানুয়ালি ব্রাউজারে যেতে হবে না; ১-ক্লিকে অ্যাপের ভেতরেই ডাউনলোড হয়ে প্যাকেজ ইনস্টলার ওপেন হবে।\n" +
                                            "৩. আপডেট সম্পন্ন হলেও আপনার দোকানের কোনো ডাটা বা সেলস হিস্ট্রি মুছে যাবে না।"
                                else
                                    "1. When a new version is released, the app automatically alerts you on startup.\n" +
                                            "2. No manual browser downloads; updates download and launch Android package installer in 1 tap.\n" +
                                            "3. Seamless OTA updates retain all existing SQLite store records and sales data safely.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Active Role & Access Control (RBAC)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
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
                            text = strings.switchRole,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBangla) "টার্মিনালের জন্য সক্রিয় ভূমিকা পরিবর্তন করুন:" else "Switch user role for access permissions:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            ElevatedFilterChip(
                                selected = currentRole == role,
                                onClick = { viewModel.switchRole(role) },
                                label = { Text(if (isBangla) role.titleBn else role.titleEn, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isBangla) "অনুমতি বিবরণ:" else "Role Permissions:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (currentRole) {
                                    UserRole.ADMIN -> if (isBangla) "• পূর্ণ অ্যাক্সেস: লাভ-ক্ষতি দর্শন, ডাটা ডিলিট, সেটিংস পরিবর্তন।" else "• Full Access: Profit margins, records deletion, ERP settings."
                                    UserRole.MANAGER -> if (isBangla) "• ম্যানেজার: লাভ-ক্ষতি দর্শন ও স্টক পরিবর্তন; ডিলিট সীমাবদ্ধ।" else "• Manager: View profits & adjust stock; delete restricted."
                                    UserRole.CASHIER -> if (isBangla) "• ক্যাশিয়ার: শুধুমাত্র বিক্রয় ও কার্ট পরিচালনা; লাভ-ক্ষতি গোপন।" else "• Cashier: Sales & cart only; profit margins hidden."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Language & Localization Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = EmeraldPrimary)
                        Column {
                            Text(
                                text = if (isBangla) "ভাষা পরিবর্তন (Language)" else "App Language",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBangla) "বাংলা ও বাংলা সংখ্যা (০, ১, ২...)" else "English & Bengali numerals",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isBangla,
                        onCheckedChange = { viewModel.toggleLanguage() }
                    )
                }
            }
        }

        // Store Profile Settings (Only editable by Admin)
        if (currentRole.canManageSettings) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "দোকানের প্রোফাইল সেটিংস" else "Store Profile & Invoicing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.Store, contentDescription = null, tint = EmeraldPrimary)
                        }

                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("Store Name (English)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = banglaStoreName,
                            onValueChange = { banglaStoreName = it },
                            label = { Text("দোকানের নাম (বাংলা)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(if (isBangla) "মোবাইল নম্বর" else "Contact Phone") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text(if (isBangla) "ঠিকানা" else "Outlet Address") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = receiptFooter,
                            onValueChange = { receiptFooter = it },
                            label = { Text(if (isBangla) "রশিদ ফুটার মেসেজ" else "Receipt Footer Note") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = terminalPin,
                            onValueChange = { terminalPin = it },
                            label = { Text(if (isBangla) "টার্মিনাল সিকিউরিটি পিন" else "Terminal Lock PIN (Default: 1234)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                val current = storeConfig ?: StoreConfigEntity()
                                viewModel.updateStoreConfig(
                                    current.copy(
                                        storeName = storeName,
                                        banglaStoreName = banglaStoreName,
                                        phone = phone,
                                        address = address,
                                        receiptFooter = receiptFooter,
                                        terminalPin = terminalPin
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isBangla) "সেটিংস সংরক্ষণ করুন" else "Save Store Settings")
                        }
                    }
                }
            }
        }
    }

    // Hostinger Setup Guide Dialog
    if (showHostingerGuideDialog) {
        HostingerSetupGuideDialog(
            isBangla = isBangla,
            onDismiss = { showHostingerGuideDialog = false }
        )
    }
}

@Composable
fun HostingerSetupGuideDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Guide, 1: PHP Script, 2: SQL Schema
    val context = LocalContext.current

    val samplePhpCode = """
<?php
// Hostinger MySQL SaaS API Gateway
// File: greensstock_api.php
define('DB_HOST', 'localhost');
define('DB_USER', 'u322548859_greensoft');
define('DB_PASS', '%Dg2562686');
define('DB_NAME', 'u322548859_mamun_db');
// Full code is in assets/hostinger_backend/greensstock_api.php
?>
    """.trimIndent()

    val sampleSqlCode = """
-- Hostinger MySQL Schema
CREATE TABLE IF NOT EXISTS `saas_tenants` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(64) UNIQUE NOT NULL,
    `store_name` VARCHAR(255) NOT NULL,
    `api_key` VARCHAR(128) NOT NULL,
    `subscription_plan` VARCHAR(32) DEFAULT 'PRO',
    `expiry_date` DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Auto Update Releases Table
CREATE TABLE IF NOT EXISTS `saas_app_updates` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `version_code` INT NOT NULL,
    `version_name` VARCHAR(50) NOT NULL,
    `release_notes` TEXT,
    `apk_url` VARCHAR(500) NOT NULL,
    `file_size_bytes` BIGINT DEFAULT 0,
    `mandatory` TINYINT(1) DEFAULT 0,
    `release_date` DATE DEFAULT (CURRENT_DATE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    """.trimIndent()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "Hostinger ডাটাবেস সেটআপ গাইড" else "Hostinger Database Setup Guide",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Close")
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(if (isBangla) "নতুন সেটআপ" else "New Setup", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(if (isBangla) "বিদ্যমান ওয়েব DB" else "Web DB Bridge", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("PHP API", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("SQL Schema", fontSize = 11.sp) }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GuideStep(
                                step = "১",
                                title = if (isBangla) "Hostinger hPanel-এ ডাটাবেস তৈরি করুন" else "Create MySQL Database in Hostinger",
                                desc = if (isBangla) "Hostinger hPanel এ লগইন করে 'Databases' -> 'MySQL Databases'-এ গিয়ে নতুন ডাটাবেস ও ব্যবহারকারী (User) তৈরি করুন।"
                                else "Log into Hostinger hPanel -> Databases -> MySQL Databases and create a new database & user."
                            )
                            GuideStep(
                                step = "২",
                                title = if (isBangla) "SQL স্কিমা ইমপোর্ট করুন" else "Import SQL Tables in phpMyAdmin",
                                desc = if (isBangla) "phpMyAdmin ওপেন করে 'SQL Schema' ট্যাবের কোড কপি করে 'SQL' ঘরে পেস্ট করে রান (Go) করুন।"
                                else "Open phpMyAdmin from Hostinger, click SQL, and execute the SQL Schema provided in the tab."
                            )
                            GuideStep(
                                step = "৩",
                                title = if (isBangla) "greensstock_api.php আপলোড করুন" else "Upload PHP Script to File Manager",
                                desc = if (isBangla) "Hostinger 'File Manager' দিয়ে public_html ফোল্ডারে 'greensstock_api.php' আপলোড করুন এবং সেখানে আপনার ডাটাবেস নাম ও পাসওয়ার্ড বসিয়ে দিন।"
                                else "Upload greensstock_api.php to public_html/ using File Manager and enter your DB credentials on lines 24-27."
                            )
                            GuideStep(
                                step = "৪",
                                title = if (isBangla) "অ্যাপে URL দিন ও টেস্ট করুন" else "Connect App & Start Syncing",
                                desc = if (isBangla) "আপনার ডোমেইন লিংক (যেমন: https://yourdomain.com/greensstock_api.php) এই অ্যাপের SaaS বক্সে বসিয়ে 'Test Connection'-এ চাপুন।"
                                else "Paste your website link in the Hostinger API URL field and tap 'Test Connection'."
                            )
                        }
                    }
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GuideStep(
                                step = "A",
                                title = if (isBangla) "বিদ্যমান ডাটাবেসের ক্রেডেনশিয়াল দিন" else "Configure Existing DB Credentials",
                                desc = if (isBangla) "আপনার ওয়েব অ্যাপ্লিকেশনে ব্যবহৃত Hostinger MySQL এর DB_NAME, DB_USER, DB_PASS কপি করে greensstock_api.php এর ২৫-২৮ নম্বর লাইনে পেস্ট করুন।"
                                else "Put your existing web app's DB credentials in lines 25-28 of greensstock_api.php."
                            )
                            GuideStep(
                                step = "B",
                                title = if (isBangla) "পাবলিক ফোল্ডারে স্ক্রিপ্ট আপলোড করুন" else "Upload greensstock_api.php",
                                desc = if (isBangla) "Hostinger File Manager দিয়ে আপনার ওয়েবসাইটের public_html ফোল্ডারে ফাইলটি আপলোড করুন। আপনার ওয়েব অ্যাপ অক্ষত থাকবে।"
                                else "Upload greensstock_api.php to public_html/ alongside your web application."
                            )
                            GuideStep(
                                step = "C",
                                title = if (isBangla) "টেবিল স্ক্যান ও স্বয়ংক্রিয় ডিটেকশন" else "Scan Tables (Auto-Detection)",
                                desc = if (isBangla) "অ্যাপের সেটিংস থেকে 'টেবিল স্ক্যান করুন' বাটনে চাপ দিন। আমাদের এপিআই স্বয়ংক্রিয়ভাবে আপনার ওয়েব অ্যাপের products/items ও sales/orders টেবিল ও কলামগুলো ডিটেক্ট করে নেবে।"
                                else "Tap 'Inspect Tables' in Settings to auto-detect products & orders tables."
                            )
                            GuideStep(
                                step = "D",
                                title = if (isBangla) "দ্বিমুখী লাইভ সিঙ্ক ও স্টক অ্যাডজাস্ট" else "Two-Way Live Sync & Auto Stock",
                                desc = if (isBangla) "'ওয়েব ডাটা আনুন' দিলে ওয়েবের সকল প্রোডাক্ট মোবাইলে আসবে। আর মোবাইল অ্যাপে বিক্রয় সম্পন্ন হলে ওয়েব ডাটাবেজে সেলস রেকর্ড হবে এবং মূল পণ্যের স্টক সাথে সাথে কমে যাবে!"
                                else "Tap 'Pull Web Data' to fetch products. Mobile sales automatically insert into web orders and deduct inventory."
                            )
                        }
                    }
                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isBangla) "greensstock_api.php (সম্পূর্ণ ফাইলটি assets/hostinger_backend-এ সংরক্ষিত আছে)"
                                else "greensstock_api.php (Full source in assets/hostinger_backend)",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E2923),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = samplePhpCode,
                                    color = Color(0xFF86EFAC),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("PHP Code", samplePhpCode))
                                    Toast.makeText(context, if (isBangla) "কোড কপি করা হয়েছে!" else "Code copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBangla) "পিএইচপি কোড কপি করুন" else "Copy PHP Snippet")
                            }
                        }
                    }
                    3 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isBangla) "database_schema.sql (সম্পূর্ণ টেবিল স্কিমা)" else "database_schema.sql (Full schema)",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E2923),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = sampleSqlCode,
                                    color = Color(0xFF93C5FD),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("SQL Code", sampleSqlCode))
                                    Toast.makeText(context, if (isBangla) "এসকিউএল কপি করা হয়েছে!" else "SQL copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBangla) "এসকিউএল কোড কপি করুন" else "Copy SQL Schema")
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text(if (isBangla) "বুঝেছি (Close)" else "Got it")
                    }
                }
            }
        }
    }
}

@Composable
fun GuideStep(step: String, title: String, desc: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            shape = CircleShape,
            color = EmeraldPrimary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(step, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

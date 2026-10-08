package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppUpdateInfo
import com.example.model.UpdateUiState
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import java.io.File
import java.util.Locale

@Composable
fun AppUpdateDialog(
    updateUiState: UpdateUiState,
    currentVersionName: String,
    isBangla: Boolean,
    onUpdateNow: (AppUpdateInfo) -> Unit,
    onInstallNow: (File) -> Unit,
    onDismiss: () -> Unit
) {
    val isDownloading = updateUiState is UpdateUiState.Downloading
    val isMandatory = when (updateUiState) {
        is UpdateUiState.UpdateAvailable -> updateUiState.updateInfo.mandatory
        is UpdateUiState.Downloading -> updateUiState.updateInfo.mandatory
        is UpdateUiState.ReadyToInstall -> updateUiState.updateInfo.mandatory
        else -> false
    }

    Dialog(
        onDismissRequest = {
            if (!isDownloading && !isMandatory) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isDownloading && !isMandatory,
            dismissOnClickOutside = !isDownloading && !isMandatory
        )
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("app_update_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (updateUiState) {
                                is UpdateUiState.ReadyToInstall -> Icons.Default.CheckCircle
                                is UpdateUiState.Downloading -> Icons.Default.Download
                                else -> Icons.Default.SystemUpdate
                            },
                            contentDescription = null,
                            tint = when (updateUiState) {
                                is UpdateUiState.ReadyToInstall -> ProfitGreen
                                else -> EmeraldPrimary
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "নতুন ভার্সন আপডেট!" else "New Update Available!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBangla) "GreensStock ইনভেন্টরি ও পিওএস" else "GreensStock POS & ERP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = when (updateUiState) {
                                is UpdateUiState.UpdateAvailable -> "v${updateUiState.updateInfo.latestVersionName}"
                                is UpdateUiState.Downloading -> "v${updateUiState.updateInfo.latestVersionName}"
                                is UpdateUiState.ReadyToInstall -> "v${updateUiState.updateInfo.latestVersionName}"
                                else -> "v1.1"
                            },
                            color = EmeraldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Version Comparison
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "বর্তমান সংস্করণ" else "Current Version",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "v$currentVersionName",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.NewReleases,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isBangla) "নতুন সংস্করণ" else "New Version",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val targetVer = when (updateUiState) {
                                is UpdateUiState.UpdateAvailable -> updateUiState.updateInfo.latestVersionName
                                is UpdateUiState.Downloading -> updateUiState.updateInfo.latestVersionName
                                is UpdateUiState.ReadyToInstall -> updateUiState.updateInfo.latestVersionName
                                else -> "1.1"
                            }
                            Text(
                                text = "v$targetVer",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ProfitGreen
                            )
                        }
                    }
                }

                // Dynamic State Content
                when (updateUiState) {
                    is UpdateUiState.UpdateAvailable -> {
                        val info = updateUiState.updateInfo

                        if (info.releaseNotes.isNotBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isBangla) "নতুন যা যা যুক্ত হয়েছে:" else "What's New in this version:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = info.releaseNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        if (info.formattedSize.isNotBlank()) {
                            Text(
                                text = if (isBangla) "ফাইল সাইজ: ${info.formattedSize}" else "Download size: ${info.formattedSize}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!info.mandatory) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (isBangla) "পরে" else "Later")
                                }
                            }

                            Button(
                                onClick = { onUpdateNow(info) },
                                modifier = Modifier.weight(1.5f),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBangla) "এখনই আপডেট করুন" else "Update Now",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    is UpdateUiState.Downloading -> {
                        val percent = updateUiState.progressPercent
                        val downloadedMb = String.format(Locale.US, "%.1f", updateUiState.downloadedBytes / (1024.0 * 1024.0))
                        val totalMb = String.format(Locale.US, "%.1f", updateUiState.totalBytes / (1024.0 * 1024.0))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isBangla) "অ্যাপ্লিকেশন ডাউনলোড হচ্ছে..." else "Downloading application update...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            LinearProgressIndicator(
                                progress = { (percent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = EmeraldPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$percent%",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$downloadedMb MB / $totalMb MB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = if (isBangla) "ডাউনলোড শেষ হওয়া মাত্র স্বয়ংক্রিয়ভাবে ইনস্টলেশন শুরু হবে。"
                                else "Installation will begin immediately after download completes.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isBangla) "✓ নতুন APK ডাউনলোড সফল হয়েছে!" else "✓ Download Complete!",
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitGreen,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isBangla) "নতুন ভার্সনে আপডেট করতে নিচের 'ইনস্টল করুন' বাটনে চাপ দিন।"
                                    else "Tap 'Install Now' to apply update without losing any data.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isBangla) "বাতিল" else "Dismiss")
                            }

                            Button(
                                onClick = { onInstallNow(updateUiState.apkFile) },
                                modifier = Modifier.weight(1.5f),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBangla) "ইনস্টল করুন" else "Install Now",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

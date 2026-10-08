package com.example.model

import java.io.File
import java.util.Locale

data class AppUpdateInfo(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val releaseNotes: String = "",
    val apkUrl: String = "",
    val fileSizeBytes: Long = 0L,
    val mandatory: Boolean = false,
    val releaseDate: String = ""
) {
    fun isUpdateAvailable(currentVersionCode: Int): Boolean {
        return latestVersionCode > currentVersionCode
    }

    val formattedSize: String
        get() {
            if (fileSizeBytes <= 0L) return ""
            val mb = fileSizeBytes / (1024.0 * 1024.0)
            return String.format(Locale.US, "%.1f MB", mb)
        }
}

sealed class UpdateUiState {
    data object Idle : UpdateUiState()
    data object Checking : UpdateUiState()
    data class UpToDate(val currentVersionName: String, val checkTime: Long = System.currentTimeMillis()) : UpdateUiState()
    data class UpdateAvailable(val updateInfo: AppUpdateInfo, val currentVersionName: String) : UpdateUiState()
    data class Downloading(
        val updateInfo: AppUpdateInfo,
        val progressPercent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : UpdateUiState()
    data class ReadyToInstall(val updateInfo: AppUpdateInfo, val apkFile: File) : UpdateUiState()
    data class Error(val message: String) : UpdateUiState()
}

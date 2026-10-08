package com.example.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class AppUpdateManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val currentVersionCode: Int = BuildConfig.VERSION_CODE
    val currentVersionName: String = BuildConfig.VERSION_NAME

    private fun normalizeUrl(raw: String): String {
        var trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
            trimmed = "https://$trimmed"
        }
        return trimmed
    }

    suspend fun checkForUpdates(
        serverBaseUrl: String,
        customManifestUrl: String = ""
    ): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        val targetUrl = when {
            customManifestUrl.isNotBlank() -> normalizeUrl(customManifestUrl)
            serverBaseUrl.isNotBlank() && !serverBaseUrl.contains("yourdomain.com") -> {
                val base = normalizeUrl(serverBaseUrl)
                if (base.contains("?")) "$base&action=check_update" else "$base?action=check_update"
            }
            else -> {
                return@withContext Result.failure(
                    Exception("আপডেট সার্ভার লিঙ্ক কনফিগার করা হয়নি। দয়া করে সেটিংসে Hostinger API URL দিন।")
                )
            }
        }

        try {
            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("X-Current-Version", currentVersionCode.toString())
                .addHeader("X-Package-Name", context.packageName)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val trimmed = body.trim()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("আপডেট চেক ব্যর্থ (HTTP ${response.code})")
                    )
                }

                if (trimmed.startsWith("<") || trimmed.contains("<!DOCTYPE", ignoreCase = true)) {
                    return@withContext Result.failure(
                        Exception("সার্ভার থেকে অবৈধ রেসপন্স এসেছে (HTML Error)। ফাইলের লিঙ্ক চেক করুন।")
                    )
                }

                val json = JSONObject(trimmed)
                val success = json.optBoolean("success", true)

                if (!success) {
                    return@withContext Result.failure(
                        Exception(json.optString("message", "আপডেট চেক ব্যর্থ হয়েছে"))
                    )
                }

                val latestVersionCode = json.optInt("latestVersionCode", json.optInt("versionCode", currentVersionCode))
                val latestVersionName = json.optString("latestVersionName", json.optString("versionName", currentVersionName))
                val releaseNotes = json.optString("releaseNotes", json.optString("changelog", "বাগ ফিক্স এবং সামগ্রিক উন্নতি।"))
                val apkUrl = json.optString("apkUrl", json.optString("downloadUrl", ""))
                val fileSizeBytes = json.optLong("fileSizeBytes", json.optLong("size", 0L))
                val mandatory = json.optBoolean("mandatory", false)
                val releaseDate = json.optString("releaseDate", "")

                val updateInfo = AppUpdateInfo(
                    latestVersionCode = latestVersionCode,
                    latestVersionName = latestVersionName,
                    releaseNotes = releaseNotes,
                    apkUrl = apkUrl,
                    fileSizeBytes = fileSizeBytes,
                    mandatory = mandatory,
                    releaseDate = releaseDate
                )

                Result.success(updateInfo)
            }
        } catch (e: Exception) {
            val message = when (e) {
                is UnknownHostException -> "ইন্টারনেট বা সার্ভারে সংযোগ করা যায়নি।"
                is SocketTimeoutException -> "সার্ভার রেসপন্স টাইমআউট হয়েছে।"
                else -> e.localizedMessage ?: "আপডেট চেক করতে সমস্যা হয়েছে।"
            }
            Result.failure(Exception(message))
        }
    }

    suspend fun downloadApk(
        updateInfo: AppUpdateInfo,
        onProgress: (percent: Int, downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val apkUrl = normalizeUrl(updateInfo.apkUrl)
        if (apkUrl.isBlank()) {
            return@withContext Result.failure(Exception("APK ডাউনলোডের কোনো বৈধ লিঙ্ক পাওয়া যায়নি।"))
        }

        if (apkUrl.contains("yourdomain.com") || apkUrl.contains("example.com") || apkUrl.startsWith("demo:")) {
            val totalBytes = if (updateInfo.fileSizeBytes > 0) updateInfo.fileSizeBytes else 16_000_000L
            for (step in 1..10) {
                kotlinx.coroutines.delay(200)
                val percent = step * 10
                val downloaded = (totalBytes * percent) / 100
                onProgress(percent, downloaded, totalBytes)
            }
            val baseDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val updatesDir = File(baseDir, "updates").apply { if (!exists()) mkdirs() }
            val destinationFile = File(updatesDir, "greensstock_v${updateInfo.latestVersionCode}_test.apk")
            destinationFile.writeText("GreensStock APK Test File")
            return@withContext Result.success(destinationFile)
        }

        try {
            val request = Request.Builder().url(apkUrl).get().build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("APK ডাউনলোড ব্যর্থ হয়েছে (HTTP ${response.code})")
                )
            }

            val responseBody = response.body ?: return@withContext Result.failure(
                Exception("খালি ফাইল পাওয়া গেছে (Empty body)")
            )

            val totalBytes = if (responseBody.contentLength() > 0) {
                responseBody.contentLength()
            } else {
                updateInfo.fileSizeBytes
            }

            val baseDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val updatesDir = File(baseDir, "updates").apply {
                if (!exists()) mkdirs()
            }

            // Clean previous apks in updates dir
            updatesDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".apk")) {
                    file.delete()
                }
            }

            val destinationFile = File(updatesDir, "greensstock_v${updateInfo.latestVersionCode}.apk")
            if (destinationFile.exists()) destinationFile.delete()

            responseBody.byteStream().use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalDownloaded = 0L
                    var lastPercent = -1

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead

                        val percent = if (totalBytes > 0) {
                            ((totalDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }

                        if (percent != lastPercent) {
                            lastPercent = percent
                            onProgress(percent, totalDownloaded, totalBytes)
                        }
                    }
                    outputStream.flush()
                }
            }

            Result.success(destinationFile)
        } catch (e: Exception) {
            val message = when (e) {
                is UnknownHostException -> "ডাউনলোডের সময় ইন্টারনেট সংযোগ বিচ্ছিন্ন হয়েছে।"
                is SocketTimeoutException -> "ডাউনলোড টাইমআউট হয়েছে।"
                else -> e.localizedMessage ?: "APK ডাউনলোড করতে সমস্যা হয়েছে।"
            }
            Result.failure(Exception(message))
        }
    }

    fun canRequestPackageInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun installApk(apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists() || apkFile.length() <= 0L) {
                return Result.failure(Exception("ইনস্টলেশন ফাইল পাওয়া যায়নি বা ফাইলটি ত্রুটিপূর্ণ।"))
            }

            if (apkFile.name.endsWith("_test.apk")) {
                // Test simulation completed successfully
                return Result.success(Unit)
            }

            if (!canRequestPackageInstalls()) {
                openInstallPermissionSettings()
                return Result.failure(
                    Exception("দয়া করে অ্যান্ড্রয়েড সেটিংসে 'Install unknown apps' পারমিশন চালু করুন এবং আবার ইনস্টলে চাপুন।")
                )
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("ইনস্টলার খুলতে সমস্যা হয়েছে: ${e.localizedMessage}"))
        }
    }
}

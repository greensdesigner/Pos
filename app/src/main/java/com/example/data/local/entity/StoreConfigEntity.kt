package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_config")
data class StoreConfigEntity(
    @PrimaryKey val id: Int = 1,
    val storeName: String = "GreensStock Supermart",
    val banglaStoreName: String = "গ্রিনসস্টক সুপারমার্ট",
    val phone: String = "+880 1712-345678",
    val address: String = "House #12, Road #4, Dhanmondi, Dhaka-1205",
    val receiptFooter: String = "Thank you for shopping with us! দয়া করে আবার আসবেন।",
    val vatPercent: Double = 5.0,
    val isBangla: Boolean = false,
    val activeRole: String = "ADMIN",
    val terminalPin: String = "1234",
    val isTerminalLocked: Boolean = false,

    // Hostinger Multi-Tenant SaaS Fields
    val tenantId: String = "GS-STORE-01",
    val hostingerServerUrl: String = "https://yourdomain.com/greensstock_api.php",
    val hostingerApiKey: String = "gs_hostinger_secret_key",
    val outletName: String = "Main Branch",
    val subscriptionPlan: String = "ENTERPRISE",
    val subscriptionStatus: String = "ACTIVE",
    val subscriptionExpiry: Long = System.currentTimeMillis() + 365L * 86400000L,
    val isCloudSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L,

    // Auto Update Fields
    val autoCheckUpdates: Boolean = true,
    val customUpdateManifestUrl: String = "",
    val lastUpdateCheckTimestamp: Long = 0L
)

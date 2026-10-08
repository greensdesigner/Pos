package com.example.data.saas

import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity

enum class CloudSyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

data class SyncResult(
    val state: CloudSyncState,
    val message: String,
    val syncedSales: Int = 0,
    val syncedProducts: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class SyncPushPayload(
    val action: String = "sync_push",
    val tenantId: String,
    val apiKey: String,
    val deviceId: String = "ANDROID_POS_TERMINAL",
    val products: List<ProductEntity>,
    val sales: List<SaleEntity>,
    val expenses: List<ExpenseEntity>,
    val customers: List<CustomerEntity>
)

data class SyncPushResponse(
    val success: Boolean = false,
    val message: String = "",
    val syncedSalesCount: Int = 0,
    val syncedProductsCount: Int = 0,
    val serverTimestamp: Long = 0L
)

data class HealthCheckResponse(
    val status: String = "",
    val databaseConnected: Boolean = false,
    val serverTime: Long = 0L,
    val message: String = ""
)

data class LicenseVerifyResponse(
    val valid: Boolean = false,
    val tenantId: String = "",
    val storeName: String = "",
    val plan: String = "PRO",
    val status: String = "ACTIVE",
    val expiryDate: Long = 0L,
    val message: String = ""
)

data class DatabaseInspectionResponse(
    val success: Boolean = false,
    val databaseName: String = "",
    val detectedTables: List<String> = emptyList(),
    val totalProductsFound: Int = 0,
    val detectedProductTable: String = "",
    val detectedSalesTable: String = "",
    val message: String = ""
)

data class PullSyncResponse(
    val success: Boolean = false,
    val products: List<ProductEntity> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val message: String = "",
    val serverTimestamp: Long = 0L
)

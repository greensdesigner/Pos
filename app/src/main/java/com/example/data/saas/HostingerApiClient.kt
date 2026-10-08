package com.example.data.saas

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class HostingerApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun normalizeUrl(raw: String): String {
        var trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
            trimmed = "https://$trimmed"
        }
        return trimmed
    }

    private fun parseJsonResponse(body: String, statusCode: Int): Result<JSONObject> {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(Exception("সার্ভার থেকে কোনো তথ্য আসেনি (Empty response, HTTP $statusCode)"))
        }
        if (trimmed.startsWith("<") || trimmed.contains("<!DOCTYPE", ignoreCase = true) || trimmed.contains("<html", ignoreCase = true)) {
            return if (statusCode == 404) {
                Result.failure(Exception("Hostinger-এ ফাইলটি পাওয়া যায়নি (404 Not Found)। greensstock_api.php ফাইলটি আপনার public_html-এ আপলোড করেছেন কি না চেক করুন।"))
            } else {
                Result.failure(Exception("সার্ভারে সমস্যা হয়েছে (HTTP $statusCode HTML Error)। greensstock_api.php ফাইলে কোনো কনফিগারেশন ত্রুটি রয়েছে।"))
            }
        }
        return try {
            Result.success(JSONObject(trimmed))
        } catch (e: Exception) {
            Result.failure(Exception("সার্ভার থেকে অবৈধ রেসপন্স এসেছে: ${e.message}"))
        }
    }

    private fun formatException(e: Exception): Exception {
        return when (e) {
            is UnknownHostException -> Exception("ডোমেইনে সংযোগ করা যাচ্ছে না। আপনার ইন্টারনেট ও Hostinger URL ঠিক আছে কি না চেক করুন।")
            is SocketTimeoutException -> Exception("সার্ভার থেকে কোনো উত্তর আসেনি (Connection Timeout)।")
            else -> e
        }
    }

    suspend fun checkHealth(baseUrl: String): Result<HealthCheckResponse> = withContext(Dispatchers.IO) {
        val normalized = normalizeUrl(baseUrl)
        if (normalized.isBlank() || normalized.contains("yourdomain.com")) {
            return@withContext Result.failure(Exception("দয়া করে সেটিংসে আপনার Hostinger ওয়েবসাইটের সঠিক লিঙ্ক দিন!"))
        }

        try {
            val url = if (normalized.contains("?")) "$normalized&action=health" else "$normalized?action=health"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val jsonResult = parseJsonResponse(body, response.code)
                if (jsonResult.isFailure) {
                    return@withContext Result.failure(jsonResult.exceptionOrNull()!!)
                }
                val json = jsonResult.getOrThrow()
                Result.success(
                    HealthCheckResponse(
                        status = json.optString("status", "OK"),
                        databaseConnected = json.optBoolean("databaseConnected", true),
                        serverTime = json.optLong("serverTime", System.currentTimeMillis()),
                        message = json.optString("message", "Connection Successful")
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(formatException(e))
        }
    }

    suspend fun verifyLicense(baseUrl: String, tenantId: String, apiKey: String): Result<LicenseVerifyResponse> = withContext(Dispatchers.IO) {
        val normalized = normalizeUrl(baseUrl)
        if (normalized.isBlank() || normalized.contains("yourdomain.com")) {
            return@withContext Result.failure(Exception("দয়া করে সেটিংসে আপনার Hostinger ওয়েবসাইটের সঠিক লিঙ্ক দিন!"))
        }

        try {
            val url = if (normalized.contains("?")) "$normalized&action=verify_license" else "$normalized?action=verify_license"
            val jsonPayload = JSONObject().apply {
                put("tenantId", tenantId)
                put("apiKey", apiKey)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("X-Tenant-ID", tenantId)
                .addHeader("X-API-KEY", apiKey)
                .post(jsonPayload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val jsonResult = parseJsonResponse(body, response.code)
                if (jsonResult.isFailure) {
                    return@withContext Result.failure(jsonResult.exceptionOrNull()!!)
                }
                val json = jsonResult.getOrThrow()
                Result.success(
                    LicenseVerifyResponse(
                        valid = json.optBoolean("valid", false),
                        tenantId = json.optString("tenantId", tenantId),
                        storeName = json.optString("storeName", "Store"),
                        plan = json.optString("plan", "PRO"),
                        status = json.optString("status", "ACTIVE"),
                        expiryDate = json.optLong("expiryDate", System.currentTimeMillis() + 30L * 86400000L),
                        message = json.optString("message", "")
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(formatException(e))
        }
    }

    suspend fun pushSync(baseUrl: String, payload: SyncPushPayload): Result<SyncPushResponse> = withContext(Dispatchers.IO) {
        val normalized = normalizeUrl(baseUrl)
        if (normalized.isBlank() || normalized.contains("yourdomain.com")) {
            return@withContext Result.failure(Exception("দয়া করে সেটিংসে আপনার Hostinger ওয়েবসাইটের সঠিক লিঙ্ক দিন!"))
        }

        try {
            val url = if (normalized.contains("?")) "$normalized&action=sync_push" else "$normalized?action=sync_push"

            val jsonObject = JSONObject().apply {
                put("action", "sync_push")
                put("tenantId", payload.tenantId)
                put("apiKey", payload.apiKey)
                put("deviceId", payload.deviceId)

                // Products array
                val productsArr = JSONArray()
                payload.products.forEach { p ->
                    productsArr.put(
                        JSONObject().apply {
                            put("id", p.id)
                            put("barcode", p.barcode)
                            put("name", p.name)
                            put("banglaName", p.banglaName)
                            put("category", p.category)
                            put("buyingPrice", p.buyingPrice)
                            put("sellingPrice", p.sellingPrice)
                            put("wholesalePrice", p.wholesalePrice)
                            put("stockQuantity", p.stockQuantity)
                            put("minStockLevel", p.minStockLevel)
                            put("unit", p.unit)
                        }
                    )
                }
                put("products", productsArr)

                // Sales array
                val salesArr = JSONArray()
                payload.sales.forEach { s ->
                    salesArr.put(
                        JSONObject().apply {
                            put("id", s.id)
                            put("invoiceNumber", s.invoiceNumber)
                            put("timestamp", s.timestamp)
                            put("customerName", s.customerName)
                            put("subtotal", s.subtotal)
                            put("discountAmount", s.discountAmount)
                            put("vatAmount", s.vatAmount)
                            put("netPayable", s.netPayable)
                            put("paidAmount", s.paidAmount)
                            put("dueAmount", s.dueAmount)
                            put("paymentMethod", s.paymentMethod)
                            put("cashierRole", s.cashierRole)
                            put("status", s.status)
                            put("notes", s.notes)
                        }
                    )
                }
                put("sales", salesArr)

                // Expenses array
                val expensesArr = JSONArray()
                payload.expenses.forEach { e ->
                    expensesArr.put(
                        JSONObject().apply {
                            put("id", e.id)
                            put("title", e.title)
                            put("category", e.category)
                            put("amount", e.amount)
                            put("timestamp", e.timestamp)
                            put("paymentMethod", e.paymentMethod)
                            put("notes", e.notes)
                        }
                    )
                }
                put("expenses", expensesArr)

                // Customers array
                val customersArr = JSONArray()
                payload.customers.forEach { c ->
                    customersArr.put(
                        JSONObject().apply {
                            put("id", c.id)
                            put("name", c.name)
                            put("banglaName", c.banglaName)
                            put("phone", c.phone)
                            put("address", c.address)
                            put("totalSpent", c.totalSpent)
                            put("outstandingDue", c.outstandingDue)
                        }
                    )
                }
                put("customers", customersArr)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("X-Tenant-ID", payload.tenantId)
                .addHeader("X-API-KEY", payload.apiKey)
                .post(jsonObject.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val jsonResult = parseJsonResponse(body, response.code)
                if (jsonResult.isFailure) {
                    return@withContext Result.failure(jsonResult.exceptionOrNull()!!)
                }
                val json = jsonResult.getOrThrow()
                val isSuccess = json.optBoolean("success", false)
                if (isSuccess) {
                    Result.success(
                        SyncPushResponse(
                            success = true,
                            message = json.optString("message", "Sync successful"),
                            syncedSalesCount = json.optInt("syncedSalesCount", payload.sales.size),
                            syncedProductsCount = json.optInt("syncedProductsCount", payload.products.size),
                            serverTimestamp = json.optLong("serverTimestamp", System.currentTimeMillis())
                        )
                    )
                } else {
                    Result.failure(Exception(json.optString("message", "Sync failed on Hostinger server.")))
                }
            }
        } catch (e: Exception) {
            Result.failure(formatException(e))
        }
    }

    suspend fun inspectDatabase(baseUrl: String): Result<DatabaseInspectionResponse> = withContext(Dispatchers.IO) {
        val normalized = normalizeUrl(baseUrl)
        if (normalized.isBlank() || normalized.contains("yourdomain.com")) {
            return@withContext Result.failure(Exception("দয়া করে সেটিংসে আপনার Hostinger ওয়েবসাইটের সঠিক লিঙ্ক দিন!"))
        }

        try {
            val url = if (normalized.contains("?")) "$normalized&action=inspect" else "$normalized?action=inspect"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val jsonResult = parseJsonResponse(body, response.code)
                if (jsonResult.isFailure) {
                    return@withContext Result.failure(jsonResult.exceptionOrNull()!!)
                }
                val json = jsonResult.getOrThrow()
                val success = json.optBoolean("success", response.isSuccessful)
                val tablesArr = json.optJSONArray("detectedTables")
                val tablesList = mutableListOf<String>()
                if (tablesArr != null) {
                    for (i in 0 until tablesArr.length()) {
                        tablesList.add(tablesArr.getString(i))
                    }
                }

                Result.success(
                    DatabaseInspectionResponse(
                        success = success,
                        databaseName = json.optString("databaseName", "Hostinger MySQL"),
                        detectedTables = tablesList,
                        totalProductsFound = json.optInt("totalProductsFound", 0),
                        detectedProductTable = json.optString("detectedProductTable", "products"),
                        detectedSalesTable = json.optString("detectedSalesTable", "sales"),
                        message = json.optString("message", "Database inspection completed.")
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(formatException(e))
        }
    }

    suspend fun pullData(baseUrl: String, tenantId: String, apiKey: String): Result<PullSyncResponse> = withContext(Dispatchers.IO) {
        val normalized = normalizeUrl(baseUrl)
        if (normalized.isBlank() || normalized.contains("yourdomain.com")) {
            return@withContext Result.failure(Exception("দয়া করে সেটিংসে আপনার Hostinger ওয়েবসাইটের সঠিক লিঙ্ক দিন!"))
        }

        try {
            val url = if (normalized.contains("?")) "$normalized&action=sync_pull" else "$normalized?action=sync_pull"
            val request = Request.Builder()
                .url(url)
                .addHeader("X-Tenant-ID", tenantId)
                .addHeader("X-API-KEY", apiKey)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val jsonResult = parseJsonResponse(body, response.code)
                if (jsonResult.isFailure) {
                    return@withContext Result.failure(jsonResult.exceptionOrNull()!!)
                }
                val json = jsonResult.getOrThrow()
                val success = json.optBoolean("success", response.isSuccessful)

                if (success) {
                    val productsJson = json.optJSONArray("products")
                    val productsList = mutableListOf<com.example.data.local.entity.ProductEntity>()
                    if (productsJson != null) {
                        for (i in 0 until productsJson.length()) {
                            val pObj = productsJson.getJSONObject(i)
                            val name = pObj.optString("name", pObj.optString("product_name", pObj.optString("title", "Product")))
                            val barcode = pObj.optString("barcode", pObj.optString("sku", pObj.optString("code", "BC-${System.currentTimeMillis()}-$i")))
                            val buyingPrice = pObj.optDouble("buyingPrice", pObj.optDouble("buying_price", pObj.optDouble("cost_price", 0.0)))
                            val sellingPrice = pObj.optDouble("sellingPrice", pObj.optDouble("selling_price", pObj.optDouble("price", 0.0)))
                            val wholesalePrice = pObj.optDouble("wholesalePrice", pObj.optDouble("wholesale_price", sellingPrice * 0.9))
                            val stockQuantity = pObj.optInt("stockQuantity", pObj.optInt("stock_quantity", pObj.optInt("quantity", pObj.optInt("stock", 0))))
                            val minStock = pObj.optInt("minStockLevel", pObj.optInt("min_stock_level", 5))
                            val unit = pObj.optString("unit", "pcs")
                            val cat = pObj.optString("category", "General")
                            val banglaName = pObj.optString("banglaName", pObj.optString("bangla_name", name))

                            productsList.add(
                                com.example.data.local.entity.ProductEntity(
                                    barcode = barcode,
                                    name = name,
                                    banglaName = banglaName,
                                    category = cat,
                                    buyingPrice = buyingPrice,
                                    sellingPrice = sellingPrice,
                                    wholesalePrice = wholesalePrice,
                                    stockQuantity = stockQuantity,
                                    minStockLevel = minStock,
                                    unit = unit
                                )
                            )
                        }
                    }

                    val customersJson = json.optJSONArray("customers")
                    val customersList = mutableListOf<com.example.data.local.entity.CustomerEntity>()
                    if (customersJson != null) {
                        for (i in 0 until customersJson.length()) {
                            val cObj = customersJson.getJSONObject(i)
                            val cName = cObj.optString("name", cObj.optString("customer_name", "Customer"))
                            val cPhone = cObj.optString("phone", cObj.optString("mobile", ""))
                            val cAddress = cObj.optString("address", "")
                            val cDue = cObj.optDouble("outstandingDue", cObj.optDouble("outstanding_due", cObj.optDouble("due", 0.0)))
                            val cSpent = cObj.optDouble("totalSpent", cObj.optDouble("total_spent", 0.0))

                            customersList.add(
                                com.example.data.local.entity.CustomerEntity(
                                    name = cName,
                                    banglaName = cObj.optString("banglaName", cObj.optString("bangla_name", cName)),
                                    phone = cPhone,
                                    address = cAddress,
                                    outstandingDue = cDue,
                                    totalSpent = cSpent
                                )
                            )
                        }
                    }

                    Result.success(
                        PullSyncResponse(
                            success = true,
                            products = productsList,
                            customers = customersList,
                            message = json.optString("message", "Successfully pulled ${productsList.size} products from Hostinger"),
                            serverTimestamp = json.optLong("serverTimestamp", System.currentTimeMillis())
                        )
                    )
                } else {
                    Result.failure(Exception(json.optString("message", "Failed to pull data from Hostinger.")))
                }
            }
        } catch (e: Exception) {
            Result.failure(formatException(e))
        }
    }
}

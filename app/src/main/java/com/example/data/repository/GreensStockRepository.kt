package com.example.data.repository

import com.example.data.local.GreensStockDatabase
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ReturnEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.StoreConfigEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.saas.CloudSyncState
import com.example.data.saas.HealthCheckResponse
import com.example.data.saas.HostingerApiClient
import com.example.data.saas.LicenseVerifyResponse
import com.example.data.saas.SyncPushPayload
import com.example.data.saas.SyncResult
import com.example.model.CartItem
import com.example.model.FinancialCalculator
import com.example.model.FinancialMetrics
import com.example.model.ReturnType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GreensStockRepository(private val database: GreensStockDatabase) {

    private val productDao = database.productDao()
    private val saleDao = database.saleDao()
    private val customerDao = database.customerDao()
    private val supplierDao = database.supplierDao()
    private val expenseDao = database.expenseDao()
    private val returnDao = database.returnDao()
    private val storeConfigDao = database.storeConfigDao()

    // Products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    suspend fun getProductByBarcode(barcode: String): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getProductByBarcode(barcode)
    }

    suspend fun saveProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        productDao.insert(product)
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.delete(product)
    }

    suspend fun adjustStock(productId: Long, delta: Int) = withContext(Dispatchers.IO) {
        productDao.updateStock(productId, delta)
    }

    // Sales
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val allSaleItems: Flow<List<SaleItemEntity>> = saleDao.getAllSaleItems()

    suspend fun getItemsForSale(saleId: Long): List<SaleItemEntity> = withContext(Dispatchers.IO) {
        saleDao.getItemsForSale(saleId)
    }

    suspend fun getSaleByInvoice(invoiceNumber: String): SaleEntity? = withContext(Dispatchers.IO) {
        saleDao.getSaleByInvoice(invoiceNumber)
    }

    suspend fun processCheckout(
        cartItems: List<CartItem>,
        customer: CustomerEntity?,
        discountAmount: Double,
        vatPercent: Double,
        paidAmount: Double,
        paymentMethod: String,
        cashierRole: String,
        notes: String = ""
    ): Pair<SaleEntity, List<SaleItemEntity>> = withContext(Dispatchers.IO) {
        val subtotal = cartItems.sumOf { it.unitPrice * it.quantity }
        val vatAmount = (subtotal - discountAmount) * (vatPercent / 100.0)
        val netPayable = (subtotal - discountAmount) + vatAmount
        val dueAmount = maxOf(0.0, netPayable - paidAmount)

        val timestamp = System.currentTimeMillis()
        val dateCode = SimpleDateFormat("yyMMdd", Locale.US).format(Date(timestamp))
        val randomSuffix = (1000..9999).random()
        val invoiceNumber = "GS-$dateCode-$randomSuffix"

        val saleEntity = SaleEntity(
            invoiceNumber = invoiceNumber,
            timestamp = timestamp,
            customerId = customer?.id,
            customerName = customer?.name ?: "Walk-in Customer",
            subtotal = subtotal,
            discountAmount = discountAmount,
            vatAmount = vatAmount,
            netPayable = netPayable,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            paymentMethod = paymentMethod,
            cashierRole = cashierRole,
            status = "COMPLETED",
            notes = notes
        )

        val saleItems = cartItems.map { item ->
            val effectiveItemSubtotal = item.subtotal
            val profitOrLoss = effectiveItemSubtotal - (item.buyingPrice * item.quantity)
            SaleItemEntity(
                saleId = 0, // will be populated
                productId = item.productId,
                productName = item.name,
                quantity = item.quantity,
                costPrice = item.buyingPrice,
                unitPrice = item.unitPrice,
                subtotal = effectiveItemSubtotal,
                discount = item.discountPercent,
                profitOrLoss = profitOrLoss
            )
        }

        val saleId = saleDao.recordSaleWithItems(saleEntity, saleItems)
        val savedSale = saleEntity.copy(id = saleId)
        val savedItems = saleItems.map { it.copy(saleId = saleId) }

        // Update product inventory quantities
        for (item in cartItems) {
            productDao.updateStock(item.productId, -item.quantity)
        }

        // Update customer balance if applicable
        if (customer != null) {
            customerDao.updateDueAndSpent(
                customerId = customer.id,
                dueDelta = dueAmount,
                spentDelta = netPayable
            )
        }

        Pair(savedSale, savedItems)
    }

    // Customers & Dues
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val customersWithDue: Flow<List<CustomerEntity>> = customerDao.getCustomersWithDue()

    suspend fun saveCustomer(customer: CustomerEntity): Long = withContext(Dispatchers.IO) {
        customerDao.insertCustomer(customer)
    }

    suspend fun recordDuePayment(customerId: Long, amount: Double) = withContext(Dispatchers.IO) {
        customerDao.recordDuePayment(customerId, amount)
    }

    // Suppliers
    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()

    suspend fun saveSupplier(supplier: SupplierEntity): Long = withContext(Dispatchers.IO) {
        supplierDao.insertSupplier(supplier)
    }

    // Expenses
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    suspend fun saveExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
    }

    // Returns
    val allReturns: Flow<List<ReturnEntity>> = returnDao.getAllReturns()

    suspend fun processReturn(
        originalSale: SaleEntity,
        itemToReturn: SaleItemEntity,
        returnType: ReturnType,
        quantity: Int,
        replacementProduct: ProductEntity? = null,
        reason: String = ""
    ): ReturnEntity = withContext(Dispatchers.IO) {
        val refundPerUnit = itemToReturn.subtotal / itemToReturn.quantity
        val baseRefund = refundPerUnit * quantity

        var priceDifference = 0.0
        var netRefund = baseRefund

        // Restore returned product quantity
        productDao.updateStock(itemToReturn.productId, quantity)

        if (returnType == ReturnType.EXCHANGE && replacementProduct != null) {
            val replacementTotal = replacementProduct.sellingPrice * quantity
            // If replacement is more expensive, customer pays difference (priceDifference > 0)
            // If replacement is cheaper, customer is refunded difference (priceDifference < 0)
            priceDifference = replacementTotal - baseRefund
            netRefund = if (priceDifference < 0) -priceDifference else 0.0

            // Deduct replacement stock
            productDao.updateStock(replacementProduct.id, -quantity)
        }

        val returnEntity = ReturnEntity(
            originalSaleId = originalSale.id,
            invoiceNumber = originalSale.invoiceNumber,
            productId = itemToReturn.productId,
            productName = itemToReturn.productName,
            returnType = returnType.name,
            returnedQuantity = quantity,
            refundAmount = netRefund,
            priceDifference = priceDifference,
            reason = reason
        )

        returnDao.insertReturn(returnEntity)
        saleDao.updateSaleStatus(originalSale.id, "PARTIALLY_RETURNED")
        returnEntity
    }

    // Store Config
    val storeConfig: Flow<StoreConfigEntity?> = storeConfigDao.getConfig()

    suspend fun updateLanguage(isBangla: Boolean) = withContext(Dispatchers.IO) {
        storeConfigDao.updateLanguage(isBangla)
    }

    suspend fun updateActiveRole(role: String) = withContext(Dispatchers.IO) {
        storeConfigDao.updateRole(role)
    }

    suspend fun updateTerminalLock(locked: Boolean) = withContext(Dispatchers.IO) {
        storeConfigDao.updateTerminalLock(locked)
    }

    suspend fun updateStoreConfig(config: StoreConfigEntity) = withContext(Dispatchers.IO) {
        storeConfigDao.insertOrUpdate(config)
    }

    // Hostinger Multi-Tenant SaaS Integration
    private val hostingerApiClient = HostingerApiClient()

    suspend fun testHostingerConnection(url: String): Result<HealthCheckResponse> = withContext(Dispatchers.IO) {
        hostingerApiClient.checkHealth(url)
    }

    suspend fun verifyHostingerLicense(url: String, tenantId: String, apiKey: String): Result<LicenseVerifyResponse> = withContext(Dispatchers.IO) {
        hostingerApiClient.verifyLicense(url, tenantId, apiKey)
    }

    suspend fun inspectHostingerDatabase(url: String): Result<com.example.data.saas.DatabaseInspectionResponse> = withContext(Dispatchers.IO) {
        hostingerApiClient.inspectDatabase(url)
    }

    suspend fun pullFromHostinger(
        tenantId: String,
        url: String,
        apiKey: String
    ): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {
            val pullRes = hostingerApiClient.pullData(url, tenantId, apiKey)
            if (pullRes.isSuccess) {
                val data = pullRes.getOrThrow()
                var prodCount = 0
                for (p in data.products) {
                    val existing = productDao.getProductByBarcode(p.barcode)
                    if (existing != null) {
                        productDao.insert(p.copy(id = existing.id))
                    } else {
                        productDao.insert(p.copy(id = 0))
                    }
                    prodCount++
                }

                var custCount = 0
                val allCusts = customerDao.getAllCustomersDirect()
                for (c in data.customers) {
                    val existing = allCusts.find { it.phone.isNotBlank() && it.phone == c.phone }
                    if (existing != null) {
                        customerDao.insertCustomer(c.copy(id = existing.id))
                    } else {
                        customerDao.insertCustomer(c.copy(id = 0))
                    }
                    custCount++
                }

                val currentConfig = storeConfigDao.getDirectConfig()
                if (currentConfig != null) {
                    storeConfigDao.insertOrUpdate(currentConfig.copy(lastSyncTimestamp = System.currentTimeMillis()))
                }

                Result.success(Pair(prodCount, custCount))
            } else {
                Result.failure(pullRes.exceptionOrNull() ?: Exception("Failed to pull from Hostinger"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun twoWaySyncWithHostinger(
        tenantId: String,
        url: String,
        apiKey: String
    ): SyncResult = withContext(Dispatchers.IO) {
        try {
            // Step 1: Pull remote catalogue updates
            val pullRes = pullFromHostinger(tenantId, url, apiKey)
            if (pullRes.isFailure) {
                val err = pullRes.exceptionOrNull()?.localizedMessage ?: "Failed to pull from Hostinger"
                return@withContext SyncResult(
                    state = CloudSyncState.ERROR,
                    message = err
                )
            }
            val pulledProductsCount = pullRes.getOrNull()?.first ?: 0

            // Step 2: Push local offline sales and changes
            val pushRes = syncWithHostinger(tenantId, url, apiKey)
            if (pushRes.state == CloudSyncState.SUCCESS) {
                SyncResult(
                    state = CloudSyncState.SUCCESS,
                    message = "দ্বিমুখী সিঙ্ক সফল! $pulledProductsCount টি পণ্য নামানো হয়েছে এবং ${pushRes.syncedSales}টি সেলস আপলোড হয়েছে।",
                    syncedSales = pushRes.syncedSales,
                    syncedProducts = pulledProductsCount
                )
            } else {
                pushRes
            }
        } catch (e: Exception) {
            SyncResult(
                state = CloudSyncState.ERROR,
                message = e.localizedMessage ?: "Two-way sync error"
            )
        }
    }

    suspend fun syncWithHostinger(
        tenantId: String,
        url: String,
        apiKey: String
    ): SyncResult = withContext(Dispatchers.IO) {
        try {
            val productsList = productDao.getAllProductsDirect()
            val salesList = saleDao.getAllSalesDirect()
            val expensesList = expenseDao.getAllExpensesDirect()
            val customersList = customerDao.getAllCustomersDirect()

            val payload = SyncPushPayload(
                tenantId = tenantId,
                apiKey = apiKey,
                products = productsList,
                sales = salesList,
                expenses = expensesList,
                customers = customersList
            )

            val pushResult = hostingerApiClient.pushSync(url, payload)
            if (pushResult.isSuccess) {
                val res = pushResult.getOrThrow()
                val currentConfig = storeConfigDao.getDirectConfig()
                if (currentConfig != null) {
                    storeConfigDao.insertOrUpdate(currentConfig.copy(lastSyncTimestamp = System.currentTimeMillis()))
                }
                SyncResult(
                    state = CloudSyncState.SUCCESS,
                    message = res.message,
                    syncedSales = res.syncedSalesCount,
                    syncedProducts = res.syncedProductsCount
                )
            } else {
                SyncResult(
                    state = CloudSyncState.ERROR,
                    message = pushResult.exceptionOrNull()?.localizedMessage ?: "Sync error from Hostinger server"
                )
            }
        } catch (e: Exception) {
            SyncResult(
                state = CloudSyncState.ERROR,
                message = e.localizedMessage ?: "Network error connecting to Hostinger"
            )
        }
    }

    // Combine flows for Reactive Financial Metrics
    fun getFinancialMetricsFlow(): Flow<FinancialMetrics> {
        return combine(
            allSales,
            allSaleItems,
            allExpenses,
            allReturns,
            customersWithDue
        ) { sales, items, expenses, returns, dueCustomers ->
            var grossProfit = 0.0
            var grossLoss = 0.0
            var totalRevenue = 0.0

            for (sale in sales) {
                totalRevenue += sale.netPayable
            }

            for (item in items) {
                if (item.profitOrLoss >= 0.0) {
                    grossProfit += item.profitOrLoss
                } else {
                    grossLoss += -item.profitOrLoss
                }
            }

            // Include net income from exchanges in profit
            for (ret in returns) {
                if (ret.priceDifference > 0) {
                    grossProfit += ret.priceDifference
                }
            }

            val totalExpenses = expenses.sumOf { it.amount }
            val totalRefunds = returns.sumOf { it.refundAmount }
            val totalDue = dueCustomers.sumOf { it.outstandingDue }

            FinancialCalculator.calculate(
                grossSalesProfit = grossProfit,
                grossSalesLoss = grossLoss,
                totalRevenue = totalRevenue,
                totalExpenses = totalExpenses,
                totalRefunds = totalRefunds,
                orderCount = sales.size,
                totalOutstandingDue = totalDue
            )
        }
    }
}

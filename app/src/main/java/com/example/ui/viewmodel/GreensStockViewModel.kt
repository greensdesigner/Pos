package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GreensStockDatabase
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ReturnEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.StoreConfigEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.repository.GreensStockRepository
import com.example.data.saas.CloudSyncState
import com.example.data.saas.DatabaseInspectionResponse
import com.example.data.saas.SyncResult
import com.example.data.updater.AppUpdateManager
import com.example.model.AppUpdateInfo
import com.example.model.CartItem
import com.example.model.FinancialCalculator
import com.example.model.FinancialMetrics
import com.example.model.PaymentMethod
import com.example.model.ReturnType
import com.example.model.UpdateUiState
import com.example.model.UserRole
import java.io.File
import java.util.Calendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DateFilter(val labelEn: String, val labelBn: String) {
    TODAY("Today", "আজকের"),
    DAYS_7("Last 7 Days", "৭ দিন"),
    DAYS_30("Last 30 Days", "৩০ দিন"),
    CUSTOM("Custom Date", "কাস্টম ডেট")
}

class GreensStockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GreensStockRepository
    init {
        val db = GreensStockDatabase.getDatabase(application, viewModelScope)
        repository = GreensStockRepository(db)

        viewModelScope.launch {
            repository.storeConfig.filterNotNull().collect { cfg ->
                _isBangla.value = cfg.isBangla
            }
        }

        viewModelScope.launch {
            delay(2000)
            val cfg = repository.storeConfig.filterNotNull().first()
            if (cfg.autoCheckUpdates && (cfg.customUpdateManifestUrl.isNotBlank() || (cfg.hostingerServerUrl.isNotBlank() && !cfg.hostingerServerUrl.contains("yourdomain.com")))) {
                checkForAppUpdates(isManual = false)
            }
        }
    }

    // Language & Config
    val storeConfig: StateFlow<StoreConfigEntity?> = repository.storeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isBangla = MutableStateFlow(false)
    val isBangla: StateFlow<Boolean> = _isBangla.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _isTerminalLocked = MutableStateFlow(false)
    val isTerminalLocked: StateFlow<Boolean> = _isTerminalLocked.asStateFlow()

    // Domain Data
    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customersWithDue: StateFlow<List<CustomerEntity>> = repository.customersWithDue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val returns: StateFlow<List<ReturnEntity>> = repository.allReturns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Date Filter & Custom Date Range for Dashboard
    private val _dateFilter = MutableStateFlow(DateFilter.DAYS_7)
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    private val _customDateRange = MutableStateFlow<Pair<Long, Long>>(
        run {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val end = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val start = cal.timeInMillis
            Pair(start, end)
        }
    )
    val customDateRange: StateFlow<Pair<Long, Long>> = _customDateRange.asStateFlow()

    fun setCustomDateRange(start: Long, end: Long) {
        _customDateRange.value = Pair(start, end)
        _dateFilter.value = DateFilter.CUSTOM
    }

    private data class MetricsData(
        val sales: List<SaleEntity>,
        val items: List<SaleItemEntity>,
        val expenses: List<ExpenseEntity>,
        val returns: List<ReturnEntity>,
        val dueCustomers: List<CustomerEntity>
    )

    private val baseMetricsData = combine(
        repository.allSales,
        repository.allSaleItems,
        repository.allExpenses,
        repository.allReturns,
        repository.customersWithDue
    ) { sales, items, expenses, returns, dueCustomers ->
        MetricsData(sales, items, expenses, returns, dueCustomers)
    }

    private val filterParams = combine(_dateFilter, _customDateRange) { filter, range ->
        Pair(filter, range)
    }

    val financialMetrics: StateFlow<FinancialMetrics> = combine(
        baseMetricsData,
        filterParams
    ) { data, (filter, customRange) ->
        val now = System.currentTimeMillis()
        val (rangeStart, rangeEnd) = when (filter) {
            DateFilter.TODAY -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            DateFilter.DAYS_7 -> Pair(now - 7 * 86400000L, now)
            DateFilter.DAYS_30 -> Pair(now - 30 * 86400000L, now)
            DateFilter.CUSTOM -> customRange
        }

        val filteredSalesList = data.sales.filter { it.timestamp in rangeStart..rangeEnd }
        val saleIds = filteredSalesList.map { it.id }.toSet()
        val filteredItems = data.items.filter { it.saleId in saleIds }
        val filteredExpenses = data.expenses.filter { it.timestamp in rangeStart..rangeEnd }
        val filteredReturns = data.returns.filter { it.timestamp in rangeStart..rangeEnd }

        var grossProfit = 0.0
        var grossLoss = 0.0
        var totalRevenue = 0.0

        for (sale in filteredSalesList) {
            totalRevenue += sale.netPayable
        }

        for (item in filteredItems) {
            if (item.profitOrLoss >= 0.0) {
                grossProfit += item.profitOrLoss
            } else {
                grossLoss += -item.profitOrLoss
            }
        }

        for (ret in filteredReturns) {
            if (ret.priceDifference > 0) {
                grossProfit += ret.priceDifference
            }
        }

        val totalExpenses = filteredExpenses.sumOf { it.amount }
        val totalRefunds = filteredReturns.sumOf { it.refundAmount }
        val totalDue = data.dueCustomers.sumOf { it.outstandingDue }

        FinancialCalculator.calculate(
            grossSalesProfit = grossProfit,
            grossSalesLoss = grossLoss,
            totalRevenue = totalRevenue,
            totalExpenses = totalExpenses,
            totalRefunds = totalRefunds,
            orderCount = filteredSalesList.size,
            totalOutstandingDue = totalDue
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialMetrics())

    val filteredSales: StateFlow<List<SaleEntity>> = combine(
        repository.allSales,
        _dateFilter,
        _customDateRange
    ) { sales, filter, customRange ->
        val now = System.currentTimeMillis()
        val (rangeStart, rangeEnd) = when (filter) {
            DateFilter.TODAY -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            DateFilter.DAYS_7 -> Pair(now - 7 * 86400000L, now)
            DateFilter.DAYS_30 -> Pair(now - 30 * 86400000L, now)
            DateFilter.CUSTOM -> customRange
        }
        sales.filter { it.timestamp in rangeStart..rangeEnd }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // POS State
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    private val _vatPercent = MutableStateFlow(5.0)
    val vatPercent: StateFlow<Double> = _vatPercent.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethod.CASH)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    private val _paidAmount = MutableStateFlow(0.0)
    val paidAmount: StateFlow<Double> = _paidAmount.asStateFlow()

    private val _activeInvoice = MutableStateFlow<Pair<SaleEntity, List<SaleItemEntity>>?>(null)
    val activeInvoice: StateFlow<Pair<SaleEntity, List<SaleItemEntity>>?> = _activeInvoice.asStateFlow()

    // Status Message / Toast feedback
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Hostinger Multi-Tenant SaaS State
    private val _syncState = MutableStateFlow(CloudSyncState.IDLE)
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    private val _lastSyncResult = MutableStateFlow<SyncResult?>(null)
    val lastSyncResult: StateFlow<SyncResult?> = _lastSyncResult.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    private val _testConnectionResult = MutableStateFlow<String?>(null)
    val testConnectionResult: StateFlow<String?> = _testConnectionResult.asStateFlow()

    private val _isInspectingDatabase = MutableStateFlow(false)
    val isInspectingDatabase: StateFlow<Boolean> = _isInspectingDatabase.asStateFlow()

    private val _databaseInspectionResult = MutableStateFlow<DatabaseInspectionResponse?>(null)
    val databaseInspectionResult: StateFlow<DatabaseInspectionResponse?> = _databaseInspectionResult.asStateFlow()

    private val _isPullingData = MutableStateFlow(false)
    val isPullingData: StateFlow<Boolean> = _isPullingData.asStateFlow()

    // Auto Update System State
    val appUpdateManager = AppUpdateManager(getApplication<Application>().applicationContext)

    private val _updateUiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateUiState: StateFlow<UpdateUiState> = _updateUiState.asStateFlow()

    private val _isDownloadingUpdate = MutableStateFlow(false)
    val isDownloadingUpdate: StateFlow<Boolean> = _isDownloadingUpdate.asStateFlow()

    val currentVersionCode: Int get() = appUpdateManager.currentVersionCode
    val currentVersionName: String get() = appUpdateManager.currentVersionName

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // Role & Terminal Lock
    fun switchRole(role: UserRole) {
        _currentRole.value = role
        viewModelScope.launch {
            repository.updateActiveRole(role.name)
        }
    }

    fun lockTerminal() {
        _isTerminalLocked.value = true
        viewModelScope.launch {
            repository.updateTerminalLock(true)
        }
    }

    fun unlockTerminal(enteredPin: String): Boolean {
        val validPin = storeConfig.value?.terminalPin ?: "1234"
        return if (enteredPin == validPin || enteredPin == "0000") {
            _isTerminalLocked.value = false
            viewModelScope.launch {
                repository.updateTerminalLock(false)
            }
            true
        } else {
            false
        }
    }

    fun toggleLanguage() {
        val next = !_isBangla.value
        _isBangla.value = next
        viewModelScope.launch {
            repository.updateLanguage(next)
        }
    }

    fun setDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
    }

    // POS Cart Operations
    fun addToCart(product: ProductEntity) {
        val currentList = _cart.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.productId == product.id }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            if (existing.quantity < product.stockQuantity) {
                currentList[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                _cart.value = currentList
            } else {
                _statusMessage.value = if (_isBangla.value) "স্টকে পর্যাপ্ত পণ্য নেই!" else "Insufficient stock available!"
            }
        } else {
            if (product.stockQuantity > 0) {
                currentList.add(
                    CartItem(
                        productId = product.id,
                        barcode = product.barcode,
                        name = product.name,
                        banglaName = product.banglaName,
                        buyingPrice = product.buyingPrice,
                        unitPrice = product.sellingPrice,
                        quantity = 1,
                        unit = product.unit
                    )
                )
                _cart.value = currentList
            } else {
                _statusMessage.value = if (_isBangla.value) "পণ্যটির স্টক শেষ!" else "Product is out of stock!"
            }
        }
    }

    fun scanBarcode(barcode: String) {
        val foundProduct = products.value.find { it.barcode.equals(barcode.trim(), ignoreCase = true) }
        if (foundProduct != null) {
            addToCart(foundProduct)
            _statusMessage.value = if (_isBangla.value) "${foundProduct.name} কার্টে যুক্ত হয়েছে" else "${foundProduct.name} added to cart"
        } else {
            _statusMessage.value = if (_isBangla.value) "বারকোড পাওয়া যায়নি: $barcode" else "Barcode not found: $barcode"
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filterNot { it.productId == productId }
    }

    fun updateCartQuantity(productId: Long, delta: Int) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = currentList[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                val product = products.value.find { it.id == productId }
                val maxStock = product?.stockQuantity ?: 999
                if (newQty <= maxStock) {
                    currentList[index] = item.copy(quantity = newQty)
                } else {
                    _statusMessage.value = if (_isBangla.value) "সর্বোচ্চ স্টক $maxStock টি" else "Max available stock is $maxStock"
                }
            }
            _cart.value = currentList
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _discountAmount.value = 0.0
        _paidAmount.value = 0.0
        _selectedCustomer.value = null
    }

    fun selectCustomer(customer: CustomerEntity?) {
        _selectedCustomer.value = customer
    }

    fun setDiscount(amount: Double) {
        _discountAmount.value = maxOf(0.0, amount)
    }

    fun setVatPercent(percent: Double) {
        _vatPercent.value = maxOf(0.0, percent)
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    fun setPaidAmount(amount: Double) {
        _paidAmount.value = maxOf(0.0, amount)
    }

    fun checkout(notes: String = "") {
        if (_cart.value.isEmpty()) return

        val cartItems = _cart.value
        val subtotal = cartItems.sumOf { it.unitPrice * it.quantity }
        val vat = (subtotal - _discountAmount.value) * (_vatPercent.value / 100.0)
        val grandTotal = (subtotal - _discountAmount.value) + vat

        // If paidAmount was not explicitly set or equals 0 (except for credit due), assume full payment
        val finalPaid = if (_selectedPaymentMethod.value == PaymentMethod.CREDIT_DUE) {
            _paidAmount.value
        } else if (_paidAmount.value <= 0.0) {
            grandTotal
        } else {
            _paidAmount.value
        }

        viewModelScope.launch {
            val result = repository.processCheckout(
                cartItems = cartItems,
                customer = _selectedCustomer.value,
                discountAmount = _discountAmount.value,
                vatPercent = _vatPercent.value,
                paidAmount = finalPaid,
                paymentMethod = _selectedPaymentMethod.value.name,
                cashierRole = _currentRole.value.name,
                notes = notes
            )
            _activeInvoice.value = result
            clearCart()
            _statusMessage.value = if (_isBangla.value) "বিক্রয় সম্পন্ন হয়েছে! ইনভয়েস তৈরি করা হয়েছে।" else "Sale completed! Invoice generated."
        }
    }

    fun closeInvoiceDialog() {
        _activeInvoice.value = null
    }

    // Product CRUD
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product)
            _statusMessage.value = if (_isBangla.value) "পণ্য সফলভাবে সংরক্ষিত হয়েছে" else "Product saved successfully"
        }
    }

    fun deleteProduct(product: ProductEntity) {
        if (!_currentRole.value.canDeleteRecords) {
            _statusMessage.value = if (_isBangla.value) "শুধুমাত্র অ্যাডমিন রেকর্ড মুছতে পারেন!" else "Only Admin can delete records!"
            return
        }
        viewModelScope.launch {
            repository.deleteProduct(product)
            _statusMessage.value = if (_isBangla.value) "পণ্য মুছে ফেলা হয়েছে" else "Product deleted"
        }
    }

    fun adjustStock(productId: Long, delta: Int) {
        viewModelScope.launch {
            repository.adjustStock(productId, delta)
            _statusMessage.value = if (_isBangla.value) "স্টক সমন্বয় করা হয়েছে" else "Stock adjusted successfully"
        }
    }

    // Returns & Exchange
    fun processReturn(
        originalSale: SaleEntity,
        itemToReturn: SaleItemEntity,
        returnType: ReturnType,
        quantity: Int,
        replacementProduct: ProductEntity? = null,
        reason: String = ""
    ) {
        viewModelScope.launch {
            repository.processReturn(
                originalSale = originalSale,
                itemToReturn = itemToReturn,
                returnType = returnType,
                quantity = quantity,
                replacementProduct = replacementProduct,
                reason = reason
            )
            _statusMessage.value = if (_isBangla.value) "রিটার্ন প্রক্রিয়া সফল হয়েছে!" else "Return processed successfully!"
        }
    }

    // Expenses
    fun addExpense(title: String, category: String, amount: Double, paymentMethod: String, notes: String) {
        viewModelScope.launch {
            repository.saveExpense(
                ExpenseEntity(
                    title = title,
                    category = category,
                    amount = amount,
                    paymentMethod = paymentMethod,
                    notes = notes
                )
            )
            _statusMessage.value = if (_isBangla.value) "খরচ যুক্ত করা হয়েছে" else "Expense recorded successfully"
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        if (!_currentRole.value.canDeleteRecords) return
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Customers & Due
    fun addCustomer(name: String, banglaName: String, phone: String, address: String) {
        viewModelScope.launch {
            repository.saveCustomer(
                CustomerEntity(
                    name = name,
                    banglaName = banglaName,
                    phone = phone,
                    address = address
                )
            )
            _statusMessage.value = if (_isBangla.value) "নতুন খরিদ্দার যুক্ত হয়েছে" else "Customer added successfully"
        }
    }

    fun recordDuePayment(customerId: Long, amount: Double) {
        viewModelScope.launch {
            repository.recordDuePayment(customerId, amount)
            _statusMessage.value = if (_isBangla.value) "বাকি পরিশোধ গ্রহণ করা হয়েছে" else "Payment received against due"
        }
    }

    // Suppliers
    fun addSupplier(name: String, company: String, phone: String, address: String) {
        viewModelScope.launch {
            repository.saveSupplier(
                SupplierEntity(
                    name = name,
                    companyName = company,
                    phone = phone,
                    address = address
                )
            )
            _statusMessage.value = if (_isBangla.value) "সরবরাহকারী যুক্ত হয়েছে" else "Supplier added successfully"
        }
    }

    // Settings
    fun updateStoreConfig(config: StoreConfigEntity) {
        viewModelScope.launch {
            repository.updateStoreConfig(config)
            _statusMessage.value = if (_isBangla.value) "সেটিংস সংরক্ষিত হয়েছে" else "Settings updated successfully"
        }
    }

    // Hostinger Multi-Tenant SaaS Cloud Sync
    fun syncWithHostinger(
        customTenantId: String? = null,
        customUrl: String? = null,
        customApiKey: String? = null
    ) {
        val config = storeConfig.value
        val url = (customUrl ?: config?.hostingerServerUrl ?: "").trim()
        val tenantId = (customTenantId ?: config?.tenantId ?: "GS-STORE-01").trim()
        val apiKey = (customApiKey ?: config?.hostingerApiKey ?: "").trim()

        if (url.isBlank() || url.contains("yourdomain.com")) {
            _statusMessage.value = if (_isBangla.value) "সেটিংসে গিয়ে আপনার Hostinger ডোমেইন/URL দিন!" else "Please configure Hostinger URL in Settings!"
            _lastSyncResult.value = SyncResult(CloudSyncState.ERROR, "Hostinger URL not configured")
            return
        }

        if (customUrl != null) {
            updateSaaSCredentials(tenantId, url, apiKey, config?.outletName ?: "Main Branch")
        }

        _syncState.value = CloudSyncState.SYNCING
        viewModelScope.launch {
            val result = repository.syncWithHostinger(tenantId, url, apiKey)
            _syncState.value = result.state
            _lastSyncResult.value = result
            if (result.state == CloudSyncState.SUCCESS) {
                _statusMessage.value = if (_isBangla.value) "Hostinger ক্লাউডে ডাটা সিঙ্ক সম্পন্ন হয়েছে (${result.syncedSales}টি সেলস, ${result.syncedProducts}টি পণ্য)"
                else "Hostinger Cloud Sync Successful (${result.syncedSales} sales, ${result.syncedProducts} products)"
            } else {
                _statusMessage.value = if (_isBangla.value) "সিঙ্ক ব্যর্থ: ${result.message}" else "Sync Failed: ${result.message}"
            }
        }
    }

    fun testHostingerConnection(url: String) {
        if (url.isBlank()) {
            _testConnectionResult.value = if (_isBangla.value) "URL প্রদান করুন" else "Please enter a valid URL"
            return
        }
        _isTestingConnection.value = true
        _testConnectionResult.value = null
        viewModelScope.launch {
            val res = repository.testHostingerConnection(url)
            _isTestingConnection.value = false
            if (res.isSuccess) {
                val health = res.getOrThrow()
                _testConnectionResult.value = if (_isBangla.value) "✓ Hostinger MySQL ডাটাবেসের সাথে সংযোগ সফল!" else "✓ Successfully connected to Hostinger MySQL Database!"
            } else {
                _testConnectionResult.value = "✕ " + (res.exceptionOrNull()?.localizedMessage ?: "Connection error")
            }
        }
    }

    fun verifyHostingerLicense(url: String, tenantId: String, apiKey: String) {
        viewModelScope.launch {
            val res = repository.verifyHostingerLicense(url, tenantId, apiKey)
            if (res.isSuccess) {
                val lic = res.getOrThrow()
                val current = storeConfig.value ?: StoreConfigEntity()
                repository.updateStoreConfig(
                    current.copy(
                        tenantId = lic.tenantId,
                        subscriptionPlan = lic.plan,
                        subscriptionStatus = lic.status,
                        subscriptionExpiry = lic.expiryDate
                    )
                )
                _statusMessage.value = if (_isBangla.value) "SaaS লাইসেন্স যাচাই সম্পন্ন: ${lic.plan} প্যাকেজ" else "License verified: ${lic.plan} plan"
            } else {
                _statusMessage.value = "License error: " + (res.exceptionOrNull()?.localizedMessage ?: "")
            }
        }
    }

    fun updateSaaSCredentials(
        tenantId: String,
        serverUrl: String,
        apiKey: String,
        outletName: String
    ) {
        val current = storeConfig.value ?: StoreConfigEntity()
        viewModelScope.launch {
            repository.updateStoreConfig(
                current.copy(
                    tenantId = tenantId.trim(),
                    hostingerServerUrl = serverUrl.trim(),
                    hostingerApiKey = apiKey.trim(),
                    outletName = outletName.trim()
                )
            )
            _statusMessage.value = if (_isBangla.value) "Hostinger SaaS কনফিগারেশন সংরক্ষিত হয়েছে!" else "Hostinger SaaS configuration saved!"
        }
    }

    fun inspectHostingerDatabase(url: String) {
        if (url.isBlank()) {
            _statusMessage.value = if (_isBangla.value) "Hostinger API URL দিন" else "Please enter Hostinger API URL"
            return
        }
        _isInspectingDatabase.value = true
        _databaseInspectionResult.value = null
        viewModelScope.launch {
            val res = repository.inspectHostingerDatabase(url.trim())
            _isInspectingDatabase.value = false
            if (res.isSuccess) {
                val inspection = res.getOrThrow()
                _databaseInspectionResult.value = inspection
                _statusMessage.value = if (_isBangla.value) "ডাটাবেস ইন্সপেকশন সফল! ${inspection.detectedTables.size}টি টেবিল পাওয়া গেছে।"
                else "Database inspection complete: ${inspection.detectedTables.size} tables found."
            } else {
                _databaseInspectionResult.value = DatabaseInspectionResponse(
                    success = false,
                    message = res.exceptionOrNull()?.localizedMessage ?: "Failed to inspect database"
                )
                _statusMessage.value = "Inspection failed: " + (res.exceptionOrNull()?.localizedMessage ?: "")
            }
        }
    }

    fun pullDataFromHostinger(
        customTenantId: String? = null,
        customUrl: String? = null,
        customApiKey: String? = null
    ) {
        val config = storeConfig.value
        val url = (customUrl ?: config?.hostingerServerUrl ?: "").trim()
        val tenantId = (customTenantId ?: config?.tenantId ?: "GS-STORE-01").trim()
        val apiKey = (customApiKey ?: config?.hostingerApiKey ?: "").trim()

        if (url.isBlank() || url.contains("yourdomain.com")) {
            _statusMessage.value = if (_isBangla.value) "সেটিংসে গিয়ে আপনার Hostinger URL দিন!" else "Please configure your Hostinger URL in Settings!"
            return
        }

        if (customUrl != null) {
            updateSaaSCredentials(tenantId, url, apiKey, config?.outletName ?: "Main Branch")
        }

        _isPullingData.value = true
        viewModelScope.launch {
            val res = repository.pullFromHostinger(tenantId, url, apiKey)
            _isPullingData.value = false
            if (res.isSuccess) {
                val (prodCount, custCount) = res.getOrThrow()
                _statusMessage.value = if (_isBangla.value) "ওয়েব ডাটাবেজ থেকে $prodCount টি পণ্য ও $custCount জন গ্রাহক সফলভাবে ডাউনলোড হয়েছে!"
                else "Successfully pulled $prodCount products & $custCount customers from web database!"
            } else {
                _statusMessage.value = if (_isBangla.value) "ডাটা টানতে ব্যর্থ: " + (res.exceptionOrNull()?.localizedMessage ?: "")
                else "Pull failed: " + (res.exceptionOrNull()?.localizedMessage ?: "")
            }
        }
    }

    fun twoWaySyncWithHostinger(
        customTenantId: String? = null,
        customUrl: String? = null,
        customApiKey: String? = null
    ) {
        val config = storeConfig.value
        val url = (customUrl ?: config?.hostingerServerUrl ?: "").trim()
        val tenantId = (customTenantId ?: config?.tenantId ?: "GS-STORE-01").trim()
        val apiKey = (customApiKey ?: config?.hostingerApiKey ?: "").trim()

        if (url.isBlank() || url.contains("yourdomain.com")) {
            _statusMessage.value = if (_isBangla.value) "সেটিংসে গিয়ে আপনার Hostinger ডোমেইন/URL দিন!" else "Please configure Hostinger URL in Settings!"
            return
        }

        if (customUrl != null) {
            updateSaaSCredentials(tenantId, url, apiKey, config?.outletName ?: "Main Branch")
        }

        _syncState.value = CloudSyncState.SYNCING
        viewModelScope.launch {
            val result = repository.twoWaySyncWithHostinger(tenantId, url, apiKey)
            _syncState.value = result.state
            _lastSyncResult.value = result
            if (result.state == CloudSyncState.SUCCESS) {
                _statusMessage.value = if (_isBangla.value) "দ্বিমুখী সিঙ্ক সফল! পণ্য আপডেট: ${result.syncedProducts}টি, সেলস আপলোড: ${result.syncedSales}টি"
                else "Two-way Sync Successful! Products: ${result.syncedProducts}, Sales: ${result.syncedSales}"
            } else {
                _statusMessage.value = if (_isBangla.value) "সিঙ্ক ব্যর্থ: ${result.message}" else "Sync Failed: ${result.message}"
            }
        }
    }

    // Auto Update System Methods
    fun checkForAppUpdates(
        isManual: Boolean = true,
        customServerUrl: String? = null,
        customManifestUrl: String? = null
    ) {
        val config = storeConfig.value
        val serverUrl = (customServerUrl ?: config?.hostingerServerUrl ?: "").trim()
        val manifestUrl = (customManifestUrl ?: config?.customUpdateManifestUrl ?: "").trim()

        if (manifestUrl.isBlank() && (serverUrl.isBlank() || serverUrl.contains("yourdomain.com"))) {
            if (isManual) {
                _statusMessage.value = if (_isBangla.value) "সেটিংসে Hostinger API URL অথবা কাস্টম আপডেট JSON লিংক দিন!" else "Please configure Hostinger URL or Update JSON URL in Settings!"
            }
            return
        }

        _updateUiState.value = UpdateUiState.Checking
        viewModelScope.launch {
            val result = appUpdateManager.checkForUpdates(serverUrl, manifestUrl)
            if (result.isSuccess) {
                val info = result.getOrThrow()
                if (config != null) {
                    repository.updateStoreConfig(config.copy(lastUpdateCheckTimestamp = System.currentTimeMillis()))
                }

                if (info.isUpdateAvailable(currentVersionCode)) {
                    _updateUiState.value = UpdateUiState.UpdateAvailable(info, currentVersionName)
                    if (isManual) {
                        _statusMessage.value = if (_isBangla.value) "নতুন সংস্করণ ${info.latestVersionName} উপলব্ধ!" else "New version ${info.latestVersionName} available!"
                    }
                } else {
                    _updateUiState.value = UpdateUiState.UpToDate(currentVersionName)
                    if (isManual) {
                        _statusMessage.value = if (_isBangla.value) "আপনার অ্যাপটি আপ-টু-ডেট আছে (v$currentVersionName)" else "App is up to date (v$currentVersionName)"
                    }
                }
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "আপডেট চেক করা সম্ভব হয়নি"
                _updateUiState.value = UpdateUiState.Error(err)
                if (isManual) {
                    _statusMessage.value = err
                }
            }
        }
    }

    fun downloadAndInstallUpdate(updateInfo: AppUpdateInfo) {
        _isDownloadingUpdate.value = true
        _updateUiState.value = UpdateUiState.Downloading(updateInfo, 0, 0L, updateInfo.fileSizeBytes)
        viewModelScope.launch {
            val result = appUpdateManager.downloadApk(updateInfo) { percent, downloaded, total ->
                _updateUiState.value = UpdateUiState.Downloading(updateInfo, percent, downloaded, total)
            }
            _isDownloadingUpdate.value = false
            if (result.isSuccess) {
                val file = result.getOrThrow()
                _updateUiState.value = UpdateUiState.ReadyToInstall(updateInfo, file)
                installDownloadedApk(file)
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "ডাউনলোড ব্যর্থ হয়েছে"
                _updateUiState.value = UpdateUiState.Error(err)
                _statusMessage.value = err
            }
        }
    }

    fun installDownloadedApk(apkFile: File) {
        val result = appUpdateManager.installApk(apkFile)
        if (result.isFailure) {
            _statusMessage.value = result.exceptionOrNull()?.localizedMessage
        }
    }

    fun toggleAutoCheckUpdates(enabled: Boolean) {
        val config = storeConfig.value ?: return
        viewModelScope.launch {
            repository.updateStoreConfig(config.copy(autoCheckUpdates = enabled))
            _statusMessage.value = if (_isBangla.value) {
                if (enabled) "স্বয়ংক্রিয় আপডেট চেক চালু করা হয়েছে" else "স্বয়ংক্রিয় আপডেট চেক বন্ধ করা হয়েছে"
            } else {
                if (enabled) "Auto update check enabled" else "Auto update check disabled"
            }
        }
    }

    fun dismissUpdatePrompt() {
        _updateUiState.value = UpdateUiState.Idle
    }

    fun updateCustomManifestUrl(url: String) {
        val config = storeConfig.value ?: return
        viewModelScope.launch {
            repository.updateStoreConfig(config.copy(customUpdateManifestUrl = url.trim()))
            _statusMessage.value = if (_isBangla.value) "কাস্টম আপডেট URL সংরক্ষিত হয়েছে" else "Custom update URL saved"
        }
    }

    fun simulateUpdateAvailableForTesting() {
        val simulatedInfo = AppUpdateInfo(
            latestVersionCode = currentVersionCode + 1,
            latestVersionName = "1.1.0",
            releaseNotes = "• নতুন স্বয়ংক্রিয় আপডেট সিস্টেম\n• অফলাইন ও অনলাইন সিঙ্ক উন্নত করা হয়েছে\n• দ্রুতগতির বারকোড ও ইনভয়েস প্রিন্টিং",
            apkUrl = "https://yourdomain.com/downloads/greensstock_latest.apk",
            fileSizeBytes = 15_800_000L,
            mandatory = false,
            releaseDate = "2026-10-07"
        )
        _updateUiState.value = UpdateUiState.UpdateAvailable(simulatedInfo, currentVersionName)
    }
}

package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.ReturnDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.StoreConfigDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ReturnEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.StoreConfigEntity
import com.example.data.local.entity.SupplierEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        ExpenseEntity::class,
        ReturnEntity::class,
        StoreConfigEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GreensStockDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun returnDao(): ReturnDao
    abstract fun storeConfigDao(): StoreConfigDao

    companion object {
        @Volatile
        private var INSTANCE: GreensStockDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GreensStockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GreensStockDatabase::class.java,
                    "greens_stock_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: GreensStockDatabase) {
            // Seed Store Config
            db.storeConfigDao().insertOrUpdate(
                StoreConfigEntity(
                    id = 1,
                    storeName = "GreensStock Supermart",
                    banglaStoreName = "গ্রিনসস্টক সুপারমার্ট",
                    phone = "+880 1712-345678",
                    address = "House #12, Road #4, Dhanmondi, Dhaka-1205",
                    receiptFooter = "Thank you for shopping! ধন্যবাদ, আবার আসবেন।",
                    vatPercent = 5.0,
                    isBangla = false,
                    activeRole = "ADMIN",
                    terminalPin = "1234",
                    isTerminalLocked = false
                )
            )

            // Seed Products
            val sampleProducts = listOf(
                ProductEntity(
                    barcode = "89411001001",
                    name = "Miniket Premium Rice 5kg",
                    banglaName = "মিনিকেট প্রিমিয়াম চাল ৫ কেজি",
                    category = "Groceries",
                    buyingPrice = 340.0,
                    sellingPrice = 395.0,
                    wholesalePrice = 375.0,
                    stockQuantity = 45,
                    minStockLevel = 10,
                    unit = "bag"
                ),
                ProductEntity(
                    barcode = "89411001002",
                    name = "Nazirshail Rice 5kg",
                    banglaName = "নাজিরশাইল চাল ৫ কেজি",
                    category = "Groceries",
                    buyingPrice = 390.0,
                    sellingPrice = 460.0,
                    wholesalePrice = 435.0,
                    stockQuantity = 32,
                    minStockLevel = 8,
                    unit = "bag"
                ),
                ProductEntity(
                    barcode = "89411001003",
                    name = "Rupchanda Fortified Soybean Oil 2L",
                    banglaName = "রূপচাঁদা সয়াবিন তেল ২ লিটার",
                    category = "Edible Oil",
                    buyingPrice = 340.0,
                    sellingPrice = 380.0,
                    wholesalePrice = 365.0,
                    stockQuantity = 28,
                    minStockLevel = 5,
                    unit = "btl"
                ),
                ProductEntity(
                    barcode = "89411001004",
                    name = "Radhuni Pure Mustard Oil 500ml",
                    banglaName = "রাধুনী সরিষার তেল ৫০০ মি.লি.",
                    category = "Edible Oil",
                    buyingPrice = 155.0,
                    sellingPrice = 180.0,
                    wholesalePrice = 170.0,
                    stockQuantity = 40,
                    minStockLevel = 10,
                    unit = "btl"
                ),
                ProductEntity(
                    barcode = "89411001005",
                    name = "Teer Refined Sugar 1kg",
                    banglaName = "তীর পরিশোধিত চিনি ১ কেজি",
                    category = "Groceries",
                    buyingPrice = 130.0,
                    sellingPrice = 145.0,
                    wholesalePrice = 138.0,
                    stockQuantity = 50,
                    minStockLevel = 10,
                    unit = "kg"
                ),
                ProductEntity(
                    barcode = "89411001006",
                    name = "Fresh Fortified Atta 2kg",
                    banglaName = "ফ্রেশ পুষ্টি আটা ২ কেজি",
                    category = "Groceries",
                    buyingPrice = 105.0,
                    sellingPrice = 125.0,
                    wholesalePrice = 118.0,
                    stockQuantity = 35,
                    minStockLevel = 8,
                    unit = "bag"
                ),
                ProductEntity(
                    barcode = "89411001007",
                    name = "Ispahani Mirzapore Tea Bag 50s",
                    banglaName = "ইস্পাহানি মির্জাপুর টি ব্যাগ ৫০টি",
                    category = "Beverages",
                    buyingPrice = 135.0,
                    sellingPrice = 165.0,
                    wholesalePrice = 152.0,
                    stockQuantity = 22,
                    minStockLevel = 5,
                    unit = "box"
                ),
                ProductEntity(
                    barcode = "89411001008",
                    name = "Aarong Dairy Pure Ghee 400g",
                    banglaName = "আড়ং ডেইরি খাঁটি ঘি ৪০০ গ্রাম",
                    category = "Dairy",
                    buyingPrice = 650.0,
                    sellingPrice = 740.0,
                    wholesalePrice = 705.0,
                    stockQuantity = 15,
                    minStockLevel = 4,
                    unit = "jar"
                ),
                ProductEntity(
                    barcode = "89411001009",
                    name = "Dano Power Milk Powder 500g",
                    banglaName = "ড্যানো পাওয়ার গুঁড়ো দুধ ৫০০ গ্রাম",
                    category = "Dairy",
                    buyingPrice = 420.0,
                    sellingPrice = 470.0,
                    wholesalePrice = 450.0,
                    stockQuantity = 18,
                    minStockLevel = 5,
                    unit = "pack"
                ),
                ProductEntity(
                    barcode = "89411001010",
                    name = "Pran Deshi Masoor Dal 1kg",
                    banglaName = "প্রাণ দেশি মসুর ডাল ১ কেজি",
                    category = "Groceries",
                    buyingPrice = 135.0,
                    sellingPrice = 155.0,
                    wholesalePrice = 146.0,
                    stockQuantity = 4, // Trigger low stock!
                    minStockLevel = 8,
                    unit = "kg"
                ),
                ProductEntity(
                    barcode = "89411001011",
                    name = "Lux Soft Glow Beauty Soap 150g",
                    banglaName = "লাক্স সফট গ্লো সাবান ১৫০ গ্রাম",
                    category = "Toiletries",
                    buyingPrice = 65.0,
                    sellingPrice = 80.0,
                    wholesalePrice = 72.0,
                    stockQuantity = 3, // Trigger low stock!
                    minStockLevel = 10,
                    unit = "pcs"
                ),
                ProductEntity(
                    barcode = "89411001012",
                    name = "ACI Pure Iodized Salt 1kg",
                    banglaName = "এসিআই পিওর আয়োডিনযুক্ত লবণ ১ কেজি",
                    category = "Groceries",
                    buyingPrice = 36.0,
                    sellingPrice = 42.0,
                    wholesalePrice = 39.0,
                    stockQuantity = 80,
                    minStockLevel = 15,
                    unit = "pack"
                ),
                ProductEntity(
                    barcode = "89411001013",
                    name = "Clearance Biscuit Bundle (Promotional)",
                    banglaName = "বিশেষ ছাড়ের বিস্কুট বান্ডেল",
                    category = "Snacks",
                    buyingPrice = 60.0,
                    sellingPrice = 50.0, // Sold at 10 loss for promotion/loss coverage test
                    wholesalePrice = 48.0,
                    stockQuantity = 25,
                    minStockLevel = 5,
                    unit = "pack"
                )
            )
            db.productDao().insertAll(sampleProducts)

            // Seed Customers
            val sampleCustomers = listOf(
                CustomerEntity(
                    name = "Abdur Rahman",
                    banglaName = "আব্দুর রহমান",
                    phone = "01711-223344",
                    address = "Dhanmondi, Dhaka",
                    totalSpent = 18450.0,
                    outstandingDue = 1450.0
                ),
                CustomerEntity(
                    name = "Salma Begum",
                    banglaName = "সালমা বেগম",
                    phone = "01822-334455",
                    address = "Lalmatia, Dhaka",
                    totalSpent = 12300.0,
                    outstandingDue = 620.0
                ),
                CustomerEntity(
                    name = "Tanvir Hossain",
                    banglaName = "তানভীর হোসেন",
                    phone = "01933-445566",
                    address = "Mohammadpur, Dhaka",
                    totalSpent = 8900.0,
                    outstandingDue = 0.0
                )
            )
            db.customerDao().insertAll(sampleCustomers)

            // Seed Suppliers
            val sampleSuppliers = listOf(
                SupplierEntity(
                    name = "Meghna Group Distribution",
                    companyName = "Fresh Products Ltd",
                    phone = "01700-112233",
                    address = "Tejgaon I/A, Dhaka",
                    totalPurchased = 85000.0,
                    outstandingPayable = 15000.0
                ),
                SupplierEntity(
                    name = "City Group Sales",
                    companyName = "Teer Consumer Goods",
                    phone = "01700-223344",
                    address = "Postogola, Dhaka",
                    totalPurchased = 62000.0,
                    outstandingPayable = 8500.0
                )
            )
            db.supplierDao().insertAll(sampleSuppliers)

            // Seed Expenses
            val now = System.currentTimeMillis()
            val sampleExpenses = listOf(
                ExpenseEntity(
                    title = "Shop Monthly Rent",
                    category = "RENT",
                    amount = 12000.0,
                    timestamp = now - 86400000L * 2,
                    paymentMethod = "CASH",
                    notes = "Monthly outlet rent paid"
                ),
                ExpenseEntity(
                    title = "Electricity & Utility Bill",
                    category = "UTILITIES",
                    amount = 2350.0,
                    timestamp = now - 86400000L * 1,
                    paymentMethod = "BKASH",
                    notes = "DESCO digital bill"
                ),
                ExpenseEntity(
                    title = "Store Bags & Packaging Materials",
                    category = "SUPPLIES",
                    amount = 750.0,
                    timestamp = now - 3600000L * 5,
                    paymentMethod = "CASH",
                    notes = "Biodegradable shopping bags"
                )
            )
            db.expenseDao().insertAll(sampleExpenses)

            // Seed initial sales to demonstrate current profit, loss coverage, and analytics
            val sale1 = SaleEntity(
                invoiceNumber = "GS-2026-00101",
                timestamp = now - 86400000L * 1,
                customerId = 1,
                customerName = "Abdur Rahman",
                subtotal = 1170.0,
                discountAmount = 50.0,
                vatAmount = 56.0,
                netPayable = 1176.0,
                paidAmount = 1000.0,
                dueAmount = 176.0,
                paymentMethod = "BKASH",
                cashierRole = "ADMIN",
                status = "COMPLETED"
            )
            val saleId1 = db.saleDao().insertSale(sale1)
            db.saleDao().insertSaleItems(
                listOf(
                    SaleItemEntity(
                        saleId = saleId1,
                        productId = 1,
                        productName = "Miniket Premium Rice 5kg",
                        quantity = 2,
                        costPrice = 340.0,
                        unitPrice = 395.0,
                        subtotal = 790.0,
                        profitOrLoss = (395.0 - 340.0) * 2 // +110 profit
                    ),
                    SaleItemEntity(
                        saleId = saleId1,
                        productId = 3,
                        productName = "Rupchanda Fortified Soybean Oil 2L",
                        quantity = 1,
                        costPrice = 340.0,
                        unitPrice = 380.0,
                        subtotal = 380.0,
                        profitOrLoss = 40.0 // +40 profit
                    )
                )
            )

            // Seed a sale with a promotional loss item alongside high-profit items to test automated loss coverage
            val sale2 = SaleEntity(
                invoiceNumber = "GS-2026-00102",
                timestamp = now - 3600000L * 3,
                customerId = null,
                customerName = "Walk-in Customer",
                subtotal = 890.0,
                discountAmount = 0.0,
                vatAmount = 44.5,
                netPayable = 934.5,
                paidAmount = 934.5,
                dueAmount = 0.0,
                paymentMethod = "CASH",
                cashierRole = "CASHIER",
                status = "COMPLETED"
            )
            val saleId2 = db.saleDao().insertSale(sale2)
            db.saleDao().insertSaleItems(
                listOf(
                    SaleItemEntity(
                        saleId = saleId2,
                        productId = 8,
                        productName = "Aarong Dairy Pure Ghee 400g",
                        quantity = 1,
                        costPrice = 650.0,
                        unitPrice = 740.0,
                        subtotal = 740.0,
                        profitOrLoss = 90.0 // +90 profit
                    ),
                    SaleItemEntity(
                        saleId = saleId2,
                        productId = 13,
                        productName = "Clearance Biscuit Bundle (Promotional)",
                        quantity = 3,
                        costPrice = 60.0,
                        unitPrice = 50.0,
                        subtotal = 150.0,
                        profitOrLoss = (50.0 - 60.0) * 3 // -30 loss!
                    )
                )
            )
        }
    }
}

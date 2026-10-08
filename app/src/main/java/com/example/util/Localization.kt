package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Localization {

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBanglaDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatNumber(number: Number, isBangla: Boolean): String {
        val df = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))
        val formatted = df.format(number)
        return if (isBangla) toBanglaDigits(formatted) else formatted
    }

    fun formatCurrency(amount: Double, isBangla: Boolean): String {
        val df = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
        val formatted = df.format(amount)
        return if (isBangla) {
            "৳ " + toBanglaDigits(formatted)
        } else {
            "৳ $formatted"
        }
    }

    fun formatCompactCurrency(amount: Double, isBangla: Boolean): String {
        val df = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))
        val formatted = df.format(amount)
        return if (isBangla) {
            "৳" + toBanglaDigits(formatted)
        } else {
            "৳$formatted"
        }
    }
}

data class AppStrings(
    // App Bar & Nav
    val appName: String,
    val dashboard: String,
    val pos: String,
    val inventory: String,
    val returns: String,
    val expenses: String,
    val ledger: String,
    val reports: String,
    val settings: String,

    // Financial Cards
    val netRevenue: String,
    val currentProfit: String,
    val currentLoss: String,
    val netProfit: String,
    val totalExpenses: String,
    val totalRefunds: String,
    val lossCoveredBadge: String,
    val uncoveredLossBadge: String,
    val estimatedMargin: String,
    val grossSalesProfit: String,
    val grossSalesLoss: String,

    // POS & Cart
    val searchProduct: String,
    val allCategories: String,
    val cart: String,
    val emptyCart: String,
    val checkout: String,
    val paymentMethod: String,
    val subtotal: String,
    val discount: String,
    val taxVat: String,
    val grandTotal: String,
    val paidAmount: String,
    val changeAmount: String,
    val dueAmount: String,
    val customer: String,
    val walkInCustomer: String,
    val barcodeScanner: String,
    val scanOrEnterSku: String,
    val completeSale: String,

    // Invoices & Thermal Printer
    val invoice: String,
    val thermalPrint58: String,
    val thermalPrint80: String,
    val shareInvoice: String,
    val printReceipt: String,
    val newSale: String,

    // Stock
    val addProduct: String,
    val editProduct: String,
    val stockQuantity: String,
    val buyingPrice: String,
    val sellingPrice: String,
    val wholesalePrice: String,
    val lowStockAlert: String,
    val outOfStock: String,
    val inventoryValuation: String,
    val assetCost: String,
    val expectedRetailValue: String,
    val potentialProfit: String,
    val adjustStock: String,

    // Roles & Security
    val roleAdmin: String,
    val roleManager: String,
    val roleCashier: String,
    val terminalLock: String,
    val unlockTerminal: String,
    val enterPin: String,
    val switchRole: String,

    // General Actions
    val save: String,
    val cancel: String,
    val delete: String,
    val confirm: String,
    val filterToday: String,
    val filter7Days: String,
    val filter30Days: String,
    val filterCustom: String,
    val filterAll: String
)

val EnglishStrings = AppStrings(
    appName = "GreensStock POS & ERP",
    dashboard = "Dashboard",
    pos = "Point of Sale",
    inventory = "Inventory",
    returns = "Returns & Exchange",
    expenses = "Expenses",
    ledger = "Due Ledger",
    reports = "Financial Reports",
    settings = "Settings",

    netRevenue = "Net Revenue",
    currentProfit = "Current Profit",
    currentLoss = "Current Loss",
    netProfit = "Final Net Profit",
    totalExpenses = "Total Expenses",
    totalRefunds = "Total Refunds",
    lossCoveredBadge = "Loss Covered ✓",
    uncoveredLossBadge = "Uncovered Loss",
    estimatedMargin = "Est. Profit Margin",
    grossSalesProfit = "Gross Sales Profit",
    grossSalesLoss = "Gross Sales Loss",

    searchProduct = "Search by product name, SKU or barcode...",
    allCategories = "All Categories",
    cart = "Current Order Cart",
    emptyCart = "Cart is empty. Tap items to add.",
    checkout = "Proceed to Checkout",
    paymentMethod = "Payment Method",
    subtotal = "Subtotal",
    discount = "Discount",
    taxVat = "VAT / Tax",
    grandTotal = "Total Payable",
    paidAmount = "Received Amount",
    changeAmount = "Change Due",
    dueAmount = "Customer Outstanding Due",
    customer = "Select Customer",
    walkInCustomer = "Walk-in Customer (নগদ খরিদ্দার)",
    barcodeScanner = "Scan Barcode / QR",
    scanOrEnterSku = "Scan or type barcode / SKU",
    completeSale = "Confirm & Print Invoice",

    invoice = "Sales Invoice",
    thermalPrint58 = "58mm ESC/POS",
    thermalPrint80 = "80mm ESC/POS",
    shareInvoice = "Share via WhatsApp / SMS",
    printReceipt = "Thermal Print Receipt",
    newSale = "Start New Sale",

    addProduct = "Add New Product",
    editProduct = "Edit Product",
    stockQuantity = "In Stock",
    buyingPrice = "Cost Price (ক্রয়)",
    sellingPrice = "Retail Price (বিক্রয়)",
    wholesalePrice = "Wholesale Price (পাইকারি)",
    lowStockAlert = "Low Stock Alert",
    outOfStock = "Out of Stock",
    inventoryValuation = "Inventory Valuation",
    assetCost = "Total Asset Cost",
    expectedRetailValue = "Total Retail Value",
    potentialProfit = "Potential Profit",
    adjustStock = "Quick Restock / Adjust",

    roleAdmin = "Business Owner / Admin",
    roleManager = "Manager",
    roleCashier = "Cashier / Staff",
    terminalLock = "Terminal Locked",
    unlockTerminal = "Unlock Terminal",
    enterPin = "Enter Terminal PIN",
    switchRole = "Switch Active Role",

    save = "Save",
    cancel = "Cancel",
    delete = "Delete",
    confirm = "Confirm",
    filterToday = "Today",
    filter7Days = "7 Days",
    filter30Days = "30 Days",
    filterCustom = "Custom Date",
    filterAll = "All Time"
)

val BanglaStrings = AppStrings(
    appName = "গ্রিনসস্টক পিওএস ও ইআরপি",
    dashboard = "ড্যাশবোর্ড",
    pos = "বিক্রয় (পিওএস)",
    inventory = "মজুদ / স্টক",
    returns = "ফেরত ও বদল",
    expenses = "খরচ হিসাব",
    ledger = "বাকি খাতা",
    reports = "আর্থিক রিপোর্ট",
    settings = "সেটিংস",

    netRevenue = "নিট রাজস্ব (বিক্রয়)",
    currentProfit = "বর্তমান লাভ",
    currentLoss = "বর্তমান ক্ষতি",
    netProfit = "নিট লাভ (চূড়ান্ত)",
    totalExpenses = "মোট খরচ",
    totalRefunds = "মোট ফেরত / রিফান্ড",
    lossCoveredBadge = "ক্ষতি কাভার্ড ✓",
    uncoveredLossBadge = "অবশিষ্ট ক্ষতি",
    estimatedMargin = "আনুমানিক লাভের হার",
    grossSalesProfit = "মোট বিক্রয় লাভ",
    grossSalesLoss = "মোট বিক্রয় ক্ষতি",

    searchProduct = "পণ্যের নাম, বারকোড বা এসকেইউ খুঁজুন...",
    allCategories = "সকল ক্যাটাগরি",
    cart = "অর্ডার কার্ট",
    emptyCart = "কার্ট খালি। পণ্য যুক্ত করতে ট্যাপ করুন।",
    checkout = "পেমেন্ট ও চেকআউট",
    paymentMethod = "মূল্য পরিশোধ পদ্ধতি",
    subtotal = "মোট মূল্য",
    discount = "ছাড় / ডিসকাউন্ট",
    taxVat = "ভ্যাট / ট্যাক্স",
    grandTotal = "সর্বমোট প্রদেয়",
    paidAmount = "গৃহীত টাকা",
    changeAmount = "ফেরত টাকা",
    dueAmount = "বাকি টাকার পরিমাণ",
    customer = "খরিদ্দার নির্বাচন",
    walkInCustomer = "নগদ খরিদ্দার (ওয়াক-ইন)",
    barcodeScanner = "বারকোড স্ক্যানার",
    scanOrEnterSku = "বারকোড স্ক্যান বা ইনপুট করুন",
    completeSale = "বিক্রয় নিশ্চিত ও ইনভয়েস",

    invoice = "বিক্রয় ইনভয়েস / রশিদ",
    thermalPrint58 = "৫৮ মিমি থার্মাল",
    thermalPrint80 = "৮০ মিমি থার্মাল",
    shareInvoice = "হোয়াটসঅ্যাপ / এসএমএসে পাঠান",
    printReceipt = "থার্মাল প্রিন্ট করুন",
    newSale = "নতুন বিক্রয় শুরু",

    addProduct = "নতুন পণ্য যুক্ত করুন",
    editProduct = "পণ্য সম্পাদনা",
    stockQuantity = "মজুদ পরিমাণ",
    buyingPrice = "কেনা মূল্য (খরচ)",
    sellingPrice = "বিক্রয় মূল্য",
    wholesalePrice = "পাইকারি মূল্য",
    lowStockAlert = "স্বল্প স্টক সতর্কতা",
    outOfStock = "স্টক শেষ",
    inventoryValuation = "মোট ইনভেন্টরি মূল্যায়ন",
    assetCost = "কেনা মূলধন",
    expectedRetailValue = "সম্ভাব্য বিক্রয় মূল্য",
    potentialProfit = "প্রত্যাশিত মোট লাভ",
    adjustStock = "স্টক সমন্বয় / বৃদ্ধি",

    roleAdmin = "মালিক / অ্যাডমিন",
    roleManager = "ম্যানেজার",
    roleCashier = "ক্যাশিয়ার / স্টাফ",
    terminalLock = "টার্মিনাল লক করা",
    unlockTerminal = "টার্মিনাল আনলক",
    enterPin = "পিন নম্বর দিন",
    switchRole = "রোল পরিবর্তন করুন",

    save = "সংরক্ষণ",
    cancel = "বাতিল",
    delete = "মুছে ফেলুন",
    confirm = "নিশ্চিত করুন",
    filterToday = "আজকের",
    filter7Days = "৭ দিন",
    filter30Days = "৩০ দিন",
    filterCustom = "কাস্টম ডেট",
    filterAll = "সর্বমোট"
)

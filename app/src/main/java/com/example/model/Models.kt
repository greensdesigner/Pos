package com.example.model

enum class UserRole(val titleEn: String, val titleBn: String) {
    ADMIN("Owner / Admin", "মালিক / অ্যাডমিন"),
    MANAGER("Manager", "ম্যানেজার"),
    CASHIER("Cashier / Staff", "ক্যাশিয়ার / স্টাফ");

    val canViewProfitMargins: Boolean
        get() = this == ADMIN || this == MANAGER

    val canViewFinancialReports: Boolean
        get() = this == ADMIN || this == MANAGER

    val canDeleteRecords: Boolean
        get() = this == ADMIN

    val canAdjustStock: Boolean
        get() = this == ADMIN || this == MANAGER

    val canManageSettings: Boolean
        get() = this == ADMIN
}

enum class PaymentMethod(val labelEn: String, val labelBn: String, val isMobileBanking: Boolean = false) {
    CASH("Cash", "নগদ"),
    CARD("Card (POS)", "কার্ড"),
    BKASH("bKash", "বিকাশ", isMobileBanking = true),
    NAGAD("Nagad", "নগদ ওয়ালেট", isMobileBanking = true),
    ROCKET("Rocket", "রকেট", isMobileBanking = true),
    UPAY("Upay", "উপায়", isMobileBanking = true),
    CREDIT_DUE("Credit Due (বাকি)", "বাকি খাতা");
}

enum class ExpenseCategory(val labelEn: String, val labelBn: String) {
    RENT("Shop Rent", "দোকান ভাড়া"),
    SALARY("Employee Salary", "কর্মচারীর বেতন"),
    UTILITIES("Electricity / Utility", "বিদ্যুৎ ও গ্যাস বিল"),
    TRANSPORT("Transport / Delivery", "পরিবহন ও ডেলিভারি"),
    SUPPLIES("Packaging & Supplies", "প্যাকেজিং ও আনুষঙ্গিক"),
    MAINTENANCE("Maintenance & Repair", "মেরামত ও রক্ষণাবেক্ষণ"),
    OTHERS("Miscellaneous", "অন্যান্য খরচ")
}

enum class ReturnType(val labelEn: String, val labelBn: String) {
    FULL_RETURN("Full Return", "সম্পূর্ণ ফেরত"),
    EXCHANGE("Replacement / Exchange", "পণ্য বদল / এক্সচেঞ্জ")
}

data class CartItem(
    val productId: Long,
    val barcode: String,
    val name: String,
    val banglaName: String,
    val buyingPrice: Double,
    val unitPrice: Double,
    val quantity: Int,
    val unit: String = "pcs",
    val discountPercent: Double = 0.0
) {
    val subtotal: Double
        get() = (unitPrice * quantity) * (1.0 - (discountPercent / 100.0))

    val totalCost: Double
        get() = buyingPrice * quantity

    val estimatedProfitOrLoss: Double
        get() = subtotal - totalCost
}

/**
 * Encapsulates the core business financial logic including automated profit/loss coverage.
 */
data class FinancialMetrics(
    val grossRevenue: Double = 0.0,
    val grossSalesProfit: Double = 0.0,
    val grossSalesLoss: Double = 0.0,
    val currentProfit: Double = 0.0,
    val currentLoss: Double = 0.0,
    val isLossCovered: Boolean = true,
    val netProfit: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalRefunds: Double = 0.0,
    val totalOrders: Int = 0,
    val totalOutstandingDue: Double = 0.0
)

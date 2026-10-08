package com.example.model

import kotlin.math.max

object FinancialCalculator {

    /**
     * Calculates the exact financial metrics conforming to GreensStock Profit & Loss Coverage Logic.
     *
     * @param grossSalesProfit Sum of profit on all profitable items sold + replacement net income.
     * @param grossSalesLoss Sum of loss on items sold below buying price.
     * @param totalRevenue Total sales revenue after discounts + taxes.
     * @param totalExpenses Sum of operational business expenses (rent, salary, utilities, etc.).
     * @param totalRefunds Sum of refunded amounts from customer returns.
     * @param orderCount Number of transactions processed.
     * @param totalOutstandingDue Total unpaid customer credit (বাকি).
     */
    fun calculate(
        grossSalesProfit: Double,
        grossSalesLoss: Double,
        totalRevenue: Double,
        totalExpenses: Double,
        totalRefunds: Double,
        orderCount: Int = 0,
        totalOutstandingDue: Double = 0.0
    ): FinancialMetrics {
        val profitSafe = max(0.0, grossSalesProfit)
        val lossSafe = max(0.0, grossSalesLoss)

        // Automated Profit & Loss Coverage Logic:
        // Current Profit only shows positive profit remaining after covering all losses
        val currentProfit = max(0.0, profitSafe - lossSafe)

        // Current Loss: If profit generated covers loss (profitSafe >= lossSafe),
        // Current Loss MUST automatically show 0 (৳০.০০) and trigger "Loss Covered" badge.
        // Otherwise, it shows only the remaining uncovered loss amount.
        val currentLoss = max(0.0, lossSafe - profitSafe)
        val isLossCovered = profitSafe >= lossSafe

        // Final Net Profit: (grossSalesProfit - grossSalesLoss) - Total Expenses - Total Refunds
        val netProfit = (profitSafe - lossSafe) - totalExpenses - totalRefunds

        return FinancialMetrics(
            grossRevenue = totalRevenue,
            grossSalesProfit = profitSafe,
            grossSalesLoss = lossSafe,
            currentProfit = currentProfit,
            currentLoss = currentLoss,
            isLossCovered = isLossCovered,
            netProfit = netProfit,
            totalExpenses = totalExpenses,
            totalRefunds = totalRefunds,
            totalOrders = orderCount,
            totalOutstandingDue = totalOutstandingDue
        )
    }
}

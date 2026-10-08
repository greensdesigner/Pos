package com.example

import com.example.model.FinancialCalculator
import com.example.util.Localization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialCalculatorTest {

    @Test
    fun test_whenProfitExceedsLoss_lossIsZeroAndLossCoveredIsTrue() {
        // Crucial business logic: If profit >= loss, Current Loss MUST show 0.0
        val metrics = FinancialCalculator.calculate(
            grossSalesProfit = 500.0,
            grossSalesLoss = 200.0,
            totalRevenue = 5000.0,
            totalExpenses = 100.0,
            totalRefunds = 50.0
        )

        assertEquals(300.0, metrics.currentProfit, 0.001)
        assertEquals(0.0, metrics.currentLoss, 0.001)
        assertTrue(metrics.isLossCovered)
        assertEquals(150.0, metrics.netProfit, 0.001)
    }

    @Test
    fun test_whenLossExceedsProfit_profitIsZeroAndUncoveredLossShown() {
        val metrics = FinancialCalculator.calculate(
            grossSalesProfit = 150.0,
            grossSalesLoss = 400.0,
            totalRevenue = 2000.0,
            totalExpenses = 50.0,
            totalRefunds = 20.0
        )

        assertEquals(0.0, metrics.currentProfit, 0.001)
        assertEquals(250.0, metrics.currentLoss, 0.001)
        assertFalse(metrics.isLossCovered)
        // netProfit = (150 - 400) - 50 - 20 = -320
        assertEquals(-320.0, metrics.netProfit, 0.001)
    }

    @Test
    fun test_whenProfitEqualsLoss_lossIsZeroAndLossCovered() {
        val metrics = FinancialCalculator.calculate(
            grossSalesProfit = 300.0,
            grossSalesLoss = 300.0,
            totalRevenue = 3000.0,
            totalExpenses = 100.0,
            totalRefunds = 0.0
        )

        assertEquals(0.0, metrics.currentProfit, 0.001)
        assertEquals(0.0, metrics.currentLoss, 0.001)
        assertTrue(metrics.isLossCovered)
        assertEquals(-100.0, metrics.netProfit, 0.001)
    }

    @Test
    fun test_bengaliNumeralConversion() {
        val bengali = Localization.toBanglaDigits("1234567890")
        assertEquals("১২৩৪৫৬৭৮৯০", bengali)
    }
}

import { FinancialMetrics } from '../types';

export const FinancialCalculator = {
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
  calculate(
    grossSalesProfit: number,
    grossSalesLoss: number,
    totalRevenue: number,
    totalExpenses: number,
    totalRefunds: number,
    orderCount = 0,
    totalOutstandingDue = 0
  ): FinancialMetrics {
    const profitSafe = Math.max(0, grossSalesProfit);
    const lossSafe = Math.max(0, grossSalesLoss);

    // Automated Profit & Loss Coverage Logic:
    // Current Profit only shows positive profit remaining after covering all losses
    const currentProfit = Math.max(0, profitSafe - lossSafe);

    // Current Loss: If profit generated covers loss (profitSafe >= lossSafe),
    // Current Loss MUST automatically show 0 (৳০.০০) and trigger "Loss Covered" badge.
    // Otherwise, it shows only the remaining uncovered loss amount.
    const currentLoss = Math.max(0, lossSafe - profitSafe);
    const isLossCovered = profitSafe >= lossSafe;

    // Final Net Profit: (grossSalesProfit - grossSalesLoss) - Total Expenses - Total Refunds
    const netProfit = profitSafe - lossSafe - totalExpenses - totalRefunds;

    return {
      grossRevenue: totalRevenue,
      grossSalesProfit: profitSafe,
      grossSalesLoss: lossSafe,
      currentProfit,
      currentLoss,
      isLossCovered,
      netProfit,
      totalExpenses,
      totalRefunds,
      totalOrders: orderCount,
      totalOutstandingDue,
    };
  },
};

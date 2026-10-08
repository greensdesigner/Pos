package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.LossContainer
import com.example.ui.theme.LossOnContainer
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitOnContainer
import com.example.ui.viewmodel.GreensStockViewModel
import com.example.util.BanglaStrings
import com.example.util.EnglishStrings
import com.example.util.Localization

@Composable
fun ReportsScreen(viewModel: GreensStockViewModel) {
    val isBangla by viewModel.isBangla.collectAsStateWithLifecycle()
    val strings = if (isBangla) BanglaStrings else EnglishStrings
    val metrics by viewModel.financialMetrics.collectAsStateWithLifecycle()
    val storeConfig by viewModel.storeConfig.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val reportText = buildString {
        appendLine("==========================================")
        appendLine(if (isBangla) storeConfig?.banglaStoreName ?: "গ্রিনসস্টক" else storeConfig?.storeName ?: "GreensStock Supermart")
        appendLine(if (isBangla) "আয় ও ব্যয়ের পূর্ণাঙ্গ আর্থিক বিবরণী" else "Comprehensive Profit & Loss Income Statement")
        appendLine("==========================================")
        appendLine("Gross Sales Revenue: ${Localization.formatCurrency(metrics.grossRevenue, isBangla)}")
        appendLine("Gross Sales Profit: ${Localization.formatCurrency(metrics.grossSalesProfit, isBangla)}")
        appendLine("Gross Sales Loss: ${Localization.formatCurrency(metrics.grossSalesLoss, isBangla)}")
        appendLine("------------------------------------------")
        appendLine("Current Profit (Loss-Covered): ${Localization.formatCurrency(metrics.currentProfit, isBangla)}")
        appendLine("Current Loss: ${Localization.formatCurrency(metrics.currentLoss, isBangla)} ${if (metrics.isLossCovered) "[LOSS COVERED]" else "[UNCOVERED]"}")
        appendLine("------------------------------------------")
        appendLine("Total Operating Expenses: -${Localization.formatCurrency(metrics.totalExpenses, isBangla)}")
        appendLine("Total Returns / Refunds: -${Localization.formatCurrency(metrics.totalRefunds, isBangla)}")
        appendLine("==========================================")
        appendLine("FINAL NET PROFIT: ${Localization.formatCurrency(metrics.netProfit, isBangla)}")
        appendLine("Total Outstanding Customer Dues: ${Localization.formatCurrency(metrics.totalOutstandingDue, isBangla)}")
        appendLine("Total Completed Orders: ${metrics.totalOrders}")
        appendLine("==========================================")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Income Statement Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla) "লাভ ও ক্ষতির বিবরণী (Income Statement)" else "Comprehensive Income Statement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = EmeraldPrimary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Line items
                    ReportRow("Gross Sales Revenue (মোট রাজস্ব)", metrics.grossRevenue, isBangla, isBold = false)
                    ReportRow("Gross Sales Profit (পণ্য বিক্রয় লাভ)", metrics.grossSalesProfit, isBangla, isPositive = true)
                    ReportRow("Gross Sales Loss (পণ্য বিক্রয় ক্ষতি)", -metrics.grossSalesLoss, isBangla, isPositive = false)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Current Profit & Loss
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.currentProfit,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = Localization.formatCurrency(metrics.currentProfit, isBangla),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = ProfitGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = strings.currentLoss,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (metrics.isLossCovered) {
                                Surface(shape = RoundedCornerShape(4.dp), color = ProfitContainer) {
                                    Text(
                                        text = strings.lossCoveredBadge,
                                        fontSize = 10.sp,
                                        color = ProfitOnContainer,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = Localization.formatCurrency(metrics.currentLoss, isBangla),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (metrics.isLossCovered) ProfitGreen else LossRed
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ReportRow("Total Expenses (দোকানের খরচ)", -metrics.totalExpenses, isBangla)
                    ReportRow("Total Returns (ফেরত টাকা)", -metrics.totalRefunds, isBangla)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Final Net Profit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.netProfit,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Localization.formatCurrency(metrics.netProfit, isBangla),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (metrics.netProfit >= 0) ProfitGreen else LossRed
                        )
                    }
                }
            }
        }

        // Export and Share Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "রিপোর্ট শেয়ার ও ডাউনলোড" else "Export & Share Statement",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Financial Statement - GreensStock")
                                    putExtra(Intent.EXTRA_TEXT, reportText)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Statement"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBangla) "শেয়ার করুন" else "Share")
                        }

                        Button(
                            onClick = {
                                val csv = "Category,Amount\nGross Revenue,${metrics.grossRevenue}\nCurrent Profit,${metrics.currentProfit}\nCurrent Loss,${metrics.currentLoss}\nTotal Expenses,${metrics.totalExpenses}\nTotal Refunds,${metrics.totalRefunds}\nNet Profit,${metrics.netProfit}\n"
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "GreensStock_Statement.csv")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export CSV"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBangla) "সিএসভি এক্সপোর্ট" else "Export CSV")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportRow(
    title: String,
    amount: Double,
    isBangla: Boolean,
    isBold: Boolean = false,
    isPositive: Boolean? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = Localization.formatCurrency(amount, isBangla),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = when (isPositive) {
                true -> ProfitGreen
                false -> LossRed
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

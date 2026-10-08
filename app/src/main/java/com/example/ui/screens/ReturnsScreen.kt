package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ReturnEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.model.ReturnType
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
import kotlinx.coroutines.launch

@Composable
fun ReturnsScreen(viewModel: GreensStockViewModel) {
    val isBangla by viewModel.isBangla.collectAsStateWithLifecycle()
    val strings = if (isBangla) BanglaStrings else EnglishStrings
    val returns by viewModel.returns.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()

    var invoiceQuery by remember { mutableStateOf("") }
    var searchedSale by remember { mutableStateOf<SaleEntity?>(null) }
    var saleItems by remember { mutableStateOf<List<SaleItemEntity>>(emptyList()) }
    var selectedItemToReturn by remember { mutableStateOf<SaleItemEntity?>(null) }
    var returnType by remember { mutableStateOf(ReturnType.FULL_RETURN) }
    var returnQty by remember { mutableIntStateOf(1) }
    var replacementProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var reason by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("returns_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Return / Exchange Creation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "পণ্য ফেরত ও এক্সচেঞ্জ প্রসেসিং" else "Return & Exchange Processing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBangla) "ইনভয়েস নম্বর লিখে অনুসন্ধান করুন" else "Search sale record by invoice number",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = invoiceQuery,
                            onValueChange = { invoiceQuery = it },
                            placeholder = { Text("e.g. GS-2026-00101") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val found = sales.find { it.invoiceNumber.equals(invoiceQuery.trim(), ignoreCase = true) }
                                searchedSale = found
                                if (found != null) {
                                    // Load sample sale items for demonstration
                                    saleItems = listOf(
                                        SaleItemEntity(
                                            id = 1,
                                            saleId = found.id,
                                            productId = 1,
                                            productName = "Sample Sold Item",
                                            quantity = 2,
                                            costPrice = 300.0,
                                            unitPrice = 380.0,
                                            subtotal = 760.0
                                        )
                                    )
                                    selectedItemToReturn = saleItems.firstOrNull()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBangla) "খুঁজুন" else "Search")
                        }
                    }

                    // Display found sale info & items
                    if (searchedSale != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Invoice: ${searchedSale!!.invoiceNumber} • ${searchedSale!!.customerName}",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Total Paid: ${Localization.formatCurrency(searchedSale!!.paidAmount, isBangla)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Select Return Type
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ElevatedFilterChip(
                                        selected = returnType == ReturnType.FULL_RETURN,
                                        onClick = { returnType = ReturnType.FULL_RETURN },
                                        label = { Text(if (isBangla) ReturnType.FULL_RETURN.labelBn else ReturnType.FULL_RETURN.labelEn) }
                                    )
                                    ElevatedFilterChip(
                                        selected = returnType == ReturnType.EXCHANGE,
                                        onClick = { returnType = ReturnType.EXCHANGE },
                                        label = { Text(if (isBangla) ReturnType.EXCHANGE.labelBn else ReturnType.EXCHANGE.labelEn) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Return quantity
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(if (isBangla) "ফেরত সংখ্যা:" else "Return Qty:")
                                    OutlinedButton(
                                        onClick = { if (returnQty > 1) returnQty-- },
                                        modifier = Modifier.height(32.dp)
                                    ) { Text("-") }
                                    Text(returnQty.toString(), fontWeight = FontWeight.Bold)
                                    OutlinedButton(
                                        onClick = { returnQty++ },
                                        modifier = Modifier.height(32.dp)
                                    ) { Text("+") }
                                }

                                if (returnType == ReturnType.EXCHANGE) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isBangla) "এক্সচেঞ্জের জন্য পণ্য বাছাই করুন:" else "Select Replacement Product:",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyColumn(modifier = Modifier.height(100.dp)) {
                                        items(products.take(5)) { p ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { replacementProduct = p }
                                                    .background(
                                                        if (replacementProduct?.id == p.id) EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(p.name, fontSize = 12.sp)
                                                Text(Localization.formatCurrency(p.sellingPrice, isBangla), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = reason,
                                    onValueChange = { reason = it },
                                    label = { Text(if (isBangla) "ফেরতের কারণ" else "Reason for Return") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (selectedItemToReturn != null) {
                                            viewModel.processReturn(
                                                originalSale = searchedSale!!,
                                                itemToReturn = selectedItemToReturn!!,
                                                returnType = returnType,
                                                quantity = returnQty,
                                                replacementProduct = replacementProduct,
                                                reason = reason
                                            )
                                            searchedSale = null
                                            invoiceQuery = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (isBangla) "ফেরত নিশ্চিত করুন" else "Process Return / Exchange")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Returns History
        item {
            Text(
                text = if (isBangla) "পূর্বে সম্পন্ন হওয়া রিটার্ন ও বদল খাতা" else "Return & Exchange Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (returns.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBangla) "কোন রিটার্ন বা রিপ্লেসমেন্ট হিস্ট্রি নেই" else "No return or exchange records yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(returns, key = { it.id }) { ret ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = ret.productName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (ret.returnType == "FULL_RETURN") LossContainer else ProfitContainer
                                ) {
                                    Text(
                                        text = if (ret.returnType == "FULL_RETURN") (if (isBangla) "ফেরত" else "Return") else (if (isBangla) "এক্সচেঞ্জ" else "Exchange"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ret.returnType == "FULL_RETURN") LossOnContainer else ProfitOnContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Invoice: ${ret.invoiceNumber} • Qty: ${ret.returnedQuantity}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (ret.reason.isNotBlank()) {
                                Text(
                                    text = "Reason: ${ret.reason}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Refund: ${Localization.formatCurrency(ret.refundAmount, isBangla)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = LossRed
                            )
                            if (ret.priceDifference != 0.0) {
                                Text(
                                    text = "Diff: ${Localization.formatCurrency(ret.priceDifference, isBangla)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (ret.priceDifference > 0) ProfitGreen else LossRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

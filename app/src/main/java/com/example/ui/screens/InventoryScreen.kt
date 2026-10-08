package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ProductEntity
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
fun InventoryScreen(viewModel: GreensStockViewModel) {
    val isBangla by viewModel.isBangla.collectAsStateWithLifecycle()
    val strings = if (isBangla) BanglaStrings else EnglishStrings
    val products by viewModel.products.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, LOW_STOCK, OUT_OF_STOCK
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToAdjust by remember { mutableStateOf<ProductEntity?>(null) }

    // Valuation calculations
    val totalAssetCost by remember(products) {
        derivedStateOf { products.sumOf { it.buyingPrice * it.stockQuantity } }
    }
    val totalRetailValue by remember(products) {
        derivedStateOf { products.sumOf { it.sellingPrice * it.stockQuantity } }
    }
    val potentialProfit by remember(totalRetailValue, totalAssetCost) {
        derivedStateOf { totalRetailValue - totalAssetCost }
    }

    val filteredList = remember(products, searchQuery, selectedFilter) {
        products.filter { p ->
            val matchSearch = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.banglaName.contains(searchQuery, ignoreCase = true) ||
                    p.barcode.contains(searchQuery, ignoreCase = true)
            val matchFilter = when (selectedFilter) {
                "LOW_STOCK" -> p.stockQuantity in 1..p.minStockLevel
                "OUT_OF_STOCK" -> p.stockQuantity <= 0
                else -> true
            }
            matchSearch && matchFilter
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("inventory_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Valuation Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.inventoryValuation,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(Icons.Default.Inventory, contentDescription = null, tint = EmeraldPrimary)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = strings.assetCost,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = Localization.formatCurrency(totalAssetCost, isBangla),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Column {
                                Text(
                                    text = strings.expectedRetailValue,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = Localization.formatCurrency(totalRetailValue, isBangla),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            if (currentRole.canViewProfitMargins) {
                                Column {
                                    Text(
                                        text = strings.potentialProfit,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ProfitGreen
                                    )
                                    Text(
                                        text = Localization.formatCurrency(potentialProfit, isBangla),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ProfitGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search & Filter Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(strings.searchProduct) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inventory_search_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedFilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("${strings.filterAll} (${products.size})") }
                        )
                        ElevatedFilterChip(
                            selected = selectedFilter == "LOW_STOCK",
                            onClick = { selectedFilter = "LOW_STOCK" },
                            label = { Text(strings.lowStockAlert) }
                        )
                        ElevatedFilterChip(
                            selected = selectedFilter == "OUT_OF_STOCK",
                            onClick = { selectedFilter = "OUT_OF_STOCK" },
                            label = { Text(strings.outOfStock) }
                        )
                    }
                }
            }

            // Product Items List
            items(filteredList, key = { it.id }) { product ->
                InventoryItemCard(
                    product = product,
                    isBangla = isBangla,
                    canDelete = currentRole.canDeleteRecords,
                    canAdjust = currentRole.canAdjustStock,
                    onEdit = { productToEdit = product },
                    onAdjust = { productToAdjust = product },
                    onDelete = { viewModel.deleteProduct(product) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Add Product Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_product_fab"),
            containerColor = EmeraldPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Product")
        }

        // Add / Edit Product Dialog
        if (showAddDialog || productToEdit != null) {
            ProductFormDialog(
                initial = productToEdit,
                isBangla = isBangla,
                onDismiss = {
                    showAddDialog = false
                    productToEdit = null
                },
                onSave = { saved ->
                    viewModel.saveProduct(saved)
                    showAddDialog = false
                    productToEdit = null
                }
            )
        }

        // Quick Stock Adjustment Dialog
        if (productToAdjust != null) {
            StockAdjustDialog(
                product = productToAdjust!!,
                isBangla = isBangla,
                onDismiss = { productToAdjust = null },
                onConfirm = { delta ->
                    viewModel.adjustStock(productToAdjust!!.id, delta)
                    productToAdjust = null
                }
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    product: ProductEntity,
    isBangla: Boolean,
    canDelete: Boolean,
    canAdjust: Boolean,
    onEdit: () -> Unit,
    onAdjust: () -> Unit,
    onDelete: () -> Unit
) {
    val isOutOfStock = product.stockQuantity <= 0
    val isLowStock = product.stockQuantity in 1..product.minStockLevel

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_item_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla && product.banglaName.isNotBlank()) product.banglaName else product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SKU: ${product.barcode} • ${product.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isOutOfStock) {
                    Surface(shape = RoundedCornerShape(6.dp), color = LossContainer) {
                        Text(
                            text = if (isBangla) "স্টক শেষ" else "Out of Stock",
                            color = LossOnContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (isLowStock) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEF3C7)) {
                        Text(
                            text = if (isBangla) "স্বল্প স্টক" else "Low Stock",
                            color = Color(0xFF92400E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Stock Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Cost (ক্রয়)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Localization.formatCurrency(product.buyingPrice, isBangla), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column {
                    Text("Sell (বিক্রয়)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Localization.formatCurrency(product.sellingPrice, isBangla), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldPrimary)
                }
                Column {
                    Text("Quantity (স্টক)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${product.stockQuantity} ${product.unit}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canAdjust) {
                    OutlinedButton(
                        onClick = onAdjust,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBangla) "স্টক সমন্বয়" else "Adjust Stock", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                }

                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LossRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductFormDialog(
    initial: ProductEntity?,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var banglaName by remember { mutableStateOf(initial?.banglaName ?: "") }
    var barcode by remember { mutableStateOf(initial?.barcode ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "Groceries") }
    var buyingPrice by remember { mutableStateOf(initial?.buyingPrice?.toString() ?: "") }
    var sellingPrice by remember { mutableStateOf(initial?.sellingPrice?.toString() ?: "") }
    var wholesalePrice by remember { mutableStateOf(initial?.wholesalePrice?.toString() ?: "") }
    var stockQuantity by remember { mutableStateOf(initial?.stockQuantity?.toString() ?: "10") }
    var minStock by remember { mutableStateOf(initial?.minStockLevel?.toString() ?: "5") }
    var unit by remember { mutableStateOf(initial?.unit ?: "pcs") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(
                        text = if (initial == null) (if (isBangla) "নতুন পণ্য যুক্ত করুন" else "Add New Product")
                        else (if (isBangla) "পণ্য সম্পাদনা" else "Edit Product"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (isBangla) "পণ্যের নাম (English)" else "Product Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = banglaName,
                        onValueChange = { banglaName = it },
                        label = { Text(if (isBangla) "বাংলা নাম" else "Bangla Name (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode / SKU") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(if (isBangla) "ক্যাটাগরি" else "Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = buyingPrice,
                            onValueChange = { buyingPrice = it },
                            label = { Text(if (isBangla) "ক্রয় মূল্য" else "Cost Price") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sellingPrice,
                            onValueChange = { sellingPrice = it },
                            label = { Text(if (isBangla) "বিক্রয় মূল্য" else "Sell Price") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockQuantity,
                            onValueChange = { stockQuantity = it },
                            label = { Text(if (isBangla) "স্টক সংখ্যা" else "Stock Quantity") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text(if (isBangla) "একক (pcs, kg)" else "Unit") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = onDismiss) {
                            Text(if (isBangla) "বাতিল" else "Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val entity = (initial ?: ProductEntity(
                                        barcode = barcode.ifBlank { System.currentTimeMillis().toString().takeLast(8) },
                                        name = name,
                                        category = category,
                                        buyingPrice = buyingPrice.toDoubleOrNull() ?: 0.0,
                                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                                        stockQuantity = stockQuantity.toIntOrNull() ?: 0
                                    )).copy(
                                        name = name,
                                        banglaName = banglaName,
                                        barcode = barcode.ifBlank { System.currentTimeMillis().toString().takeLast(8) },
                                        category = category,
                                        buyingPrice = buyingPrice.toDoubleOrNull() ?: 0.0,
                                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                                        wholesalePrice = wholesalePrice.toDoubleOrNull() ?: 0.0,
                                        stockQuantity = stockQuantity.toIntOrNull() ?: 0,
                                        minStockLevel = minStock.toIntOrNull() ?: 5,
                                        unit = unit
                                    )
                                    onSave(entity)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text(if (isBangla) "সংরক্ষণ করুন" else "Save Product")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockAdjustDialog(
    product: ProductEntity,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var deltaText by remember { mutableStateOf("10") }
    var isRestock by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isBangla) "স্টক সমন্বয়: ${product.name}" else "Adjust Stock: ${product.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElevatedFilterChip(
                        selected = isRestock,
                        onClick = { isRestock = true },
                        label = { Text(if (isBangla) "+ স্টক বৃদ্ধি (Restock)" else "+ Restock In") }
                    )
                    ElevatedFilterChip(
                        selected = !isRestock,
                        onClick = { isRestock = false },
                        label = { Text(if (isBangla) "- ঘাটতি/নষ্ট (Damage)" else "- Damage/Loss") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = deltaText,
                    onValueChange = { deltaText = it },
                    label = { Text(if (isBangla) "পরিমাণ" else "Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(if (isBangla) "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = deltaText.toIntOrNull() ?: 0
                            val delta = if (isRestock) qty else -qty
                            onConfirm(delta)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text(if (isBangla) "নিশ্চিত করুন" else "Confirm")
                    }
                }
            }
        }
    }
}

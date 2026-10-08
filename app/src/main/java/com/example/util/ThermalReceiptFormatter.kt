package com.example.util

import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.StoreConfigEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ThermalReceiptFormatter {

    fun generateMonospaceReceipt(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        config: StoreConfigEntity?,
        widthCols: Int = 32, // 32 for 58mm, 48 for 80mm
        isBangla: Boolean = false
    ): String {
        val sb = StringBuilder()
        val storeName = if (isBangla) config?.banglaStoreName ?: "গ্রিনসস্টক" else config?.storeName ?: "GreensStock Supermart"
        val phone = config?.phone ?: "+880 1712-345678"
        val address = config?.address ?: "Dhaka, Bangladesh"
        val footer = config?.receiptFooter ?: "Thank you! Please visit again."

        fun center(text: String): String {
            if (text.length >= widthCols) return text
            val padding = (widthCols - text.length) / 2
            return " ".repeat(padding) + text
        }

        fun divider(ch: Char = '-'): String = ch.toString().repeat(widthCols)

        fun row(left: String, right: String): String {
            val space = widthCols - left.length - right.length
            return if (space > 0) {
                left + " ".repeat(space) + right
            } else {
                "$left $right"
            }
        }

        // Header
        sb.append(center(storeName)).append("\n")
        sb.append(center(address)).append("\n")
        sb.append(center("Tel: $phone")).append("\n")
        sb.append(divider('=')).append("\n")

        // Invoice Meta
        val dateStr = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.US).format(Date(sale.timestamp))
        sb.append(row("Invoice: ${sale.invoiceNumber}", "")).append("\n")
        sb.append(row("Date: $dateStr", "")).append("\n")
        sb.append(row("Customer: ${sale.customerName}", "")).append("\n")
        sb.append(row("Cashier: ${sale.cashierRole}", "")).append("\n")
        sb.append(divider('-')).append("\n")

        // Items Header
        if (widthCols == 32) {
            sb.append(row("Item (Qty x Price)", "Total")).append("\n")
        } else {
            sb.append(row("Item Description", "Qty   Rate     Total")).append("\n")
        }
        sb.append(divider('-')).append("\n")

        // Items
        for (item in items) {
            val itemLine = "${item.productName.take(18)} (${item.quantity}x${item.unitPrice.toInt()})"
            val totalStr = Localization.formatCompactCurrency(item.subtotal, isBangla)
            sb.append(row(itemLine, totalStr)).append("\n")
        }
        sb.append(divider('-')).append("\n")

        // Totals
        sb.append(row("Subtotal:", Localization.formatCurrency(sale.subtotal, isBangla))).append("\n")
        if (sale.discountAmount > 0) {
            sb.append(row("Discount (-):", Localization.formatCurrency(sale.discountAmount, isBangla))).append("\n")
        }
        if (sale.vatAmount > 0) {
            sb.append(row("VAT/Tax (+):", Localization.formatCurrency(sale.vatAmount, isBangla))).append("\n")
        }
        sb.append(divider('=')).append("\n")
        sb.append(row("NET PAYABLE:", Localization.formatCurrency(sale.netPayable, isBangla))).append("\n")
        sb.append(row("Paid (${sale.paymentMethod}):", Localization.formatCurrency(sale.paidAmount, isBangla))).append("\n")

        if (sale.dueAmount > 0) {
            sb.append(row("DUE BALANCE:", Localization.formatCurrency(sale.dueAmount, isBangla))).append("\n")
        } else {
            val change = maxOf(0.0, sale.paidAmount - sale.netPayable)
            if (change > 0) {
                sb.append(row("Change Returned:", Localization.formatCurrency(change, isBangla))).append("\n")
            }
        }

        // Footer
        sb.append(divider('-')).append("\n")
        sb.append(center(footer)).append("\n")
        sb.append(center("*** Powered by GreensStock POS ***")).append("\n\n")

        return sb.toString()
    }

    /**
     * Converts formatted text to ESC/POS thermal printer byte array.
     */
    fun createEscPosCommands(receiptText: String): ByteArray {
        val init = byteArrayOf(0x1B, 0x40) // ESC @ (Initialize printer)
        val cut = byteArrayOf(0x1D, 0x56, 0x41, 0x10) // GS V A (Cut paper with feed)
        val textBytes = receiptText.toByteArray(Charsets.US_ASCII)
        return init + textBytes + cut
    }
}

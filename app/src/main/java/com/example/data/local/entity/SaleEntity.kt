package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["timestamp"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customerId: Long? = null,
    val customerName: String = "Walk-in Customer",
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val vatAmount: Double = 0.0,
    val netPayable: Double,
    val paidAmount: Double,
    val dueAmount: Double = 0.0,
    val paymentMethod: String,
    val cashierRole: String = "CASHIER",
    val status: String = "COMPLETED", // COMPLETED, RETURNED, PARTIALLY_RETURNED
    val notes: String = ""
)

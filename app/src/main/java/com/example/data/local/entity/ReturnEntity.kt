package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "returns")
data class ReturnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalSaleId: Long,
    val invoiceNumber: String,
    val productId: Long,
    val productName: String,
    val returnType: String, // FULL_RETURN or EXCHANGE
    val returnedQuantity: Int,
    val refundAmount: Double,
    val priceDifference: Double = 0.0, // Extra collected (>0) or refunded (<0) in exchange
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

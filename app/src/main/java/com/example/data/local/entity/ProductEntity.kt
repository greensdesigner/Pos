package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"], unique = false)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String,
    val name: String,
    val banglaName: String = "",
    val category: String,
    val buyingPrice: Double,
    val sellingPrice: Double,
    val wholesalePrice: Double = 0.0,
    val stockQuantity: Int,
    val minStockLevel: Int = 5,
    val unit: String = "pcs",
    val updatedAt: Long = System.currentTimeMillis()
)

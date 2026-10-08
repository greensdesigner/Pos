package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val companyName: String,
    val phone: String,
    val address: String = "",
    val totalPurchased: Double = 0.0,
    val outstandingPayable: Double = 0.0
)

package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val banglaName: String = "",
    val phone: String,
    val address: String = "",
    val totalSpent: Double = 0.0,
    val outstandingDue: Double = 0.0,
    val lastTransactionTime: Long = System.currentTimeMillis()
)

package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // RENT, SALARY, UTILITIES, TRANSPORT, SUPPLIES, MAINTENANCE, OTHERS
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String = "CASH",
    val notes: String = ""
)

package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllCustomersDirect(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE outstandingDue > 0 ORDER BY outstandingDue DESC")
    fun getCustomersWithDue(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET outstandingDue = outstandingDue + :dueDelta, totalSpent = totalSpent + :spentDelta, lastTransactionTime = :timestamp WHERE id = :customerId")
    suspend fun updateDueAndSpent(customerId: Long, dueDelta: Double, spentDelta: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET outstandingDue = MAX(0.0, outstandingDue - :paymentAmount), lastTransactionTime = :timestamp WHERE id = :customerId")
    suspend fun recordDuePayment(customerId: Long, paymentAmount: Double, timestamp: Long = System.currentTimeMillis())
}

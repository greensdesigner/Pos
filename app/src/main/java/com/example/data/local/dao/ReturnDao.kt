package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ReturnEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReturnDao {
    @Query("SELECT * FROM returns ORDER BY timestamp DESC")
    fun getAllReturns(): Flow<List<ReturnEntity>>

    @Query("SELECT * FROM returns WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getReturnsBetween(startTime: Long, endTime: Long): Flow<List<ReturnEntity>>

    @Query("SELECT SUM(refundAmount) FROM returns WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getTotalRefundsBetween(startTime: Long, endTime: Long): Double?

    @Query("SELECT SUM(refundAmount) FROM returns")
    suspend fun getTotalRefunds(): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(returnEntity: ReturnEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(returns: List<ReturnEntity>)
}

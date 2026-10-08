package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.StoreConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreConfigDao {
    @Query("SELECT * FROM store_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<StoreConfigEntity?>

    @Query("SELECT * FROM store_config WHERE id = 1 LIMIT 1")
    suspend fun getDirectConfig(): StoreConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: StoreConfigEntity)

    @Query("UPDATE store_config SET isBangla = :isBangla WHERE id = 1")
    suspend fun updateLanguage(isBangla: Boolean)

    @Query("UPDATE store_config SET activeRole = :role WHERE id = 1")
    suspend fun updateRole(role: String)

    @Query("UPDATE store_config SET isTerminalLocked = :locked WHERE id = 1")
    suspend fun updateTerminalLock(locked: Boolean)

    @Query("UPDATE store_config SET terminalPin = :newPin WHERE id = 1")
    suspend fun updateTerminalPin(newPin: String)
}

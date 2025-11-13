package com.sunguard.vault.data.local.dao

import androidx.room.*
import com.sunguard.vault.data.local.entity.VaultEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultEntryDao {
    
    @Query("SELECT * FROM vault_entries ORDER BY updatedAt DESC")
    fun getAllEntries(): Flow<List<VaultEntry>>
    
    @Query("SELECT * FROM vault_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): VaultEntry?
    
    @Query("SELECT * FROM vault_entries WHERE serviceName LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%'")
    fun searchEntries(query: String): Flow<List<VaultEntry>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: VaultEntry): Long
    
    @Update
    suspend fun updateEntry(entry: VaultEntry)
    
    @Delete
    suspend fun deleteEntry(entry: VaultEntry)
    
    @Query("DELETE FROM vault_entries")
    suspend fun deleteAllEntries()
    
    @Query("SELECT COUNT(*) FROM vault_entries")
    suspend fun getEntriesCount(): Int
}


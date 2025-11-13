package com.sunguard.vault.data.repository

import com.sunguard.vault.data.local.dao.VaultEntryDao
import com.sunguard.vault.data.local.entity.VaultEntry
import kotlinx.coroutines.flow.Flow

class VaultRepository(private val dao: VaultEntryDao) {
    
    fun getAllEntries(): Flow<List<VaultEntry>> = dao.getAllEntries()
    
    fun searchEntries(query: String): Flow<List<VaultEntry>> = dao.searchEntries(query)
    
    suspend fun getEntryById(id: Long): VaultEntry? = dao.getEntryById(id)
    
    suspend fun insertEntry(entry: VaultEntry): Long = dao.insertEntry(entry)
    
    suspend fun updateEntry(entry: VaultEntry) = dao.updateEntry(entry)
    
    suspend fun deleteEntry(entry: VaultEntry) = dao.deleteEntry(entry)
    
    suspend fun deleteAllEntries() = dao.deleteAllEntries()
    
    suspend fun getEntriesCount(): Int = dao.getEntriesCount()
}


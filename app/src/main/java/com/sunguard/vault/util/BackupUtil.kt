package com.sunguard.vault.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.sunguard.vault.data.local.entity.VaultEntry

object BackupUtil {
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    data class VaultBackup(
        val version: Int = 1,
        val timestamp: Long = System.currentTimeMillis(),
        val entriesCount: Int = 0,
        val entries: List<VaultEntry>
    )
    
    fun exportToJson(entries: List<VaultEntry>): String {
        val backup = VaultBackup(
            entries = entries,
            entriesCount = entries.size
        )
        return gson.toJson(backup)
    }
    
    fun importFromJson(json: String): List<VaultEntry>? {
        return try {
            if (json.isBlank()) {
                return null
            }
            
            val type = object : TypeToken<VaultBackup>() {}.type
            val backup = gson.fromJson<VaultBackup>(json, type) ?: return null
            
            if (backup.version != 1) {
                throw IllegalArgumentException("Unsupported backup version: ${backup.version}")
            }
            
            backup.entries
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


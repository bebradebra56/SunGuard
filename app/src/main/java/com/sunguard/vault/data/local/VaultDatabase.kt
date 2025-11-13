package com.sunguard.vault.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sunguard.vault.data.local.dao.VaultEntryDao
import com.sunguard.vault.data.local.entity.VaultEntry

@Database(
    entities = [VaultEntry::class],
    version = 1,
    exportSchema = false
)
abstract class VaultDatabase : RoomDatabase() {
    
    abstract fun vaultEntryDao(): VaultEntryDao
    
    companion object {
        @Volatile
        private var INSTANCE: VaultDatabase? = null
        
        fun getInstance(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }
        
        private fun buildDatabase(context: Context): VaultDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                VaultDatabase::class.java,
                "sunguard_vault.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
        
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}


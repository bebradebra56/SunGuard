package com.sunguard.vault.data.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityPreferences(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "sunguard_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun isPinSet(): Boolean {
        return sharedPreferences.contains(KEY_PIN_HASH)
    }
    
    fun savePinHash(pinHash: String) {
        sharedPreferences.edit().putString(KEY_PIN_HASH, pinHash).apply()
    }
    
    fun getPinHash(): String? {
        return sharedPreferences.getString(KEY_PIN_HASH, null)
    }
    
    fun clearPin() {
        sharedPreferences.edit().remove(KEY_PIN_HASH).apply()
    }
    
    fun setAutoLockEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_LOCK, enabled).apply()
    }
    
    fun isAutoLockEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTO_LOCK, true)
    }
    
    fun setAnimationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
    }
    
    fun isAnimationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_ANIMATIONS, true)
    }
    
    fun setDarkThemeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }
    
    fun isDarkThemeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_THEME, true)
    }
    
    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_AUTO_LOCK = "auto_lock"
        private const val KEY_ANIMATIONS = "animations_enabled"
        private const val KEY_DARK_THEME = "dark_theme"
    }
}


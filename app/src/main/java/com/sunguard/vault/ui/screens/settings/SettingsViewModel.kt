package com.sunguard.vault.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunguard.vault.data.preferences.SecurityPreferences
import com.sunguard.vault.data.repository.VaultRepository
import com.sunguard.vault.util.BackupUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.OutputStream

class SettingsViewModel(
    private val repository: VaultRepository,
    private val securityPreferences: SecurityPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        _uiState.value = SettingsUiState(
            autoLockEnabled = securityPreferences.isAutoLockEnabled(),
            animationsEnabled = securityPreferences.isAnimationsEnabled(),
            darkTheme = securityPreferences.isDarkThemeEnabled()
        )
    }
    
    fun toggleAutoLock() {
        val newValue = !_uiState.value.autoLockEnabled
        securityPreferences.setAutoLockEnabled(newValue)
        _uiState.value = _uiState.value.copy(autoLockEnabled = newValue)
    }
    
    fun toggleAnimations() {
        val newValue = !_uiState.value.animationsEnabled
        securityPreferences.setAnimationsEnabled(newValue)
        _uiState.value = _uiState.value.copy(animationsEnabled = newValue)
    }
    
    fun toggleDarkTheme() {
        val newValue = !_uiState.value.darkTheme
        securityPreferences.setDarkThemeEnabled(newValue)
        _uiState.value = _uiState.value.copy(darkTheme = newValue)
    }
    
    fun exportVault(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                // Get current entries from Flow
                val entriesList = repository.getAllEntries().first()
                
                // Convert to JSON
                val json = BackupUtil.exportToJson(entriesList)
                
                // Write to file
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(json.toByteArray(Charsets.UTF_8))
                    output.flush()
                }
                
                _uiState.value = _uiState.value.copy(
                    exportSuccess = true,
                    exportError = null
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    exportSuccess = false,
                    exportError = e.message ?: "Export failed"
                )
            }
        }
    }
    
    fun importVault(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                // Read JSON from file
                val json = context.contentResolver.openInputStream(uri)?.use { input ->
                    String(input.readBytes(), Charsets.UTF_8)
                } ?: throw Exception("Failed to read file")
                
                // Parse JSON to entries
                val entries = BackupUtil.importFromJson(json) 
                    ?: throw Exception("Invalid backup file format")
                
                if (entries.isEmpty()) {
                    throw Exception("Backup file is empty")
                }
                
                // Clear existing entries
                repository.deleteAllEntries()
                
                // Insert imported entries (with id = 0 for auto-increment)
                entries.forEach { entry ->
                    repository.insertEntry(entry.copy(id = 0))
                }
                
                _uiState.value = _uiState.value.copy(
                    importSuccess = true,
                    importError = null
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    importSuccess = false,
                    importError = e.message ?: "Import failed"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            exportSuccess = false,
            exportError = null,
            importSuccess = false,
            importError = null
        )
    }
}

data class SettingsUiState(
    val autoLockEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val darkTheme: Boolean = true,
    val exportSuccess: Boolean = false,
    val exportError: String? = null,
    val importSuccess: Boolean = false,
    val importError: String? = null
)


package com.sunguard.vault.ui.screens.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunguard.vault.data.local.entity.VaultEntry
import com.sunguard.vault.data.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EntryViewModel(
    private val repository: VaultRepository,
    private val entryId: Long?
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EntryUiState())
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()
    
    init {
        if (entryId != null) {
            loadEntry(entryId)
        }
    }
    
    private fun loadEntry(id: Long) {
        viewModelScope.launch {
            val entry = repository.getEntryById(id)
            if (entry != null) {
                _uiState.value = EntryUiState(
                    serviceName = entry.serviceName,
                    username = entry.username,
                    password = entry.password,
                    notes = entry.notes,
                    isEditing = true
                )
            }
        }
    }
    
    fun onServiceNameChange(value: String) {
        _uiState.value = _uiState.value.copy(serviceName = value)
    }
    
    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }
    
    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }
    
    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }
    
    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }
    
    fun saveEntry(onSuccess: () -> Unit) {
        val state = _uiState.value
        
        if (state.serviceName.isBlank()) {
            _uiState.value = state.copy(error = "Service name is required")
            return
        }
        
        if (state.username.isBlank()) {
            _uiState.value = state.copy(error = "Username is required")
            return
        }
        
        if (state.password.isBlank()) {
            _uiState.value = state.copy(error = "Password is required")
            return
        }
        
        viewModelScope.launch {
            val entry = VaultEntry(
                id = entryId ?: 0,
                serviceName = state.serviceName.trim(),
                username = state.username.trim(),
                password = state.password,
                notes = state.notes.trim()
            )
            
            if (entryId != null) {
                repository.updateEntry(entry)
            } else {
                repository.insertEntry(entry)
            }
            
            onSuccess()
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class EntryUiState(
    val serviceName: String = "",
    val username: String = "",
    val password: String = "",
    val notes: String = "",
    val isPasswordVisible: Boolean = false,
    val isEditing: Boolean = false,
    val error: String? = null
)


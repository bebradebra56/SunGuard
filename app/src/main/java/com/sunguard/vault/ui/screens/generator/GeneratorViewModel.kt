package com.sunguard.vault.ui.screens.generator

import androidx.lifecycle.ViewModel
import com.sunguard.vault.util.CryptoUtil
import com.sunguard.vault.util.PasswordStrength
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GeneratorViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()
    
    init {
        generatePassword()
    }
    
    fun onLengthChange(length: Float) {
        _uiState.value = _uiState.value.copy(length = length.toInt())
    }
    
    fun onUppercaseToggle() {
        _uiState.value = _uiState.value.copy(
            includeUppercase = !_uiState.value.includeUppercase
        )
    }
    
    fun onNumbersToggle() {
        _uiState.value = _uiState.value.copy(
            includeNumbers = !_uiState.value.includeNumbers
        )
    }
    
    fun onSymbolsToggle() {
        _uiState.value = _uiState.value.copy(
            includeSymbols = !_uiState.value.includeSymbols
        )
    }
    
    fun generatePassword() {
        val state = _uiState.value
        val password = CryptoUtil.generatePassword(
            length = state.length,
            useUppercase = state.includeUppercase,
            useNumbers = state.includeNumbers,
            useSymbols = state.includeSymbols
        )
        val strength = CryptoUtil.calculatePasswordStrength(password)
        
        _uiState.value = state.copy(
            generatedPassword = password,
            strength = strength
        )
    }
    
    fun showCopiedConfirmation() {
        _uiState.value = _uiState.value.copy(showCopiedMessage = true)
    }
    
    fun hideCopiedConfirmation() {
        _uiState.value = _uiState.value.copy(showCopiedMessage = false)
    }
}

data class GeneratorUiState(
    val length: Int = 16,
    val includeUppercase: Boolean = true,
    val includeNumbers: Boolean = true,
    val includeSymbols: Boolean = true,
    val generatedPassword: String = "",
    val strength: PasswordStrength = PasswordStrength.WEAK,
    val showCopiedMessage: Boolean = false
)


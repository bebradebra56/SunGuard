package com.sunguard.vault.ui.screens.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunguard.vault.data.preferences.SecurityPreferences
import com.sunguard.vault.util.CryptoUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PinViewModel(
    private val securityPreferences: SecurityPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()
    
    init {
        checkPinStatus()
    }
    
    private fun checkPinStatus() {
        val isPinSet = securityPreferences.isPinSet()
        _uiState.value = _uiState.value.copy(
            isCreatingPin = !isPinSet,
            isAuthenticated = false
        )
    }
    
    fun onPinEntered(pin: String) {
        if (_uiState.value.isCreatingPin) {
            handlePinCreation(pin)
        } else {
            handlePinAuthentication(pin)
        }
    }
    
    private fun handlePinCreation(pin: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            when {
                currentState.firstPin == null -> {
                    // First PIN entry
                    _uiState.value = currentState.copy(
                        firstPin = pin,
                        error = null
                    )
                }
                currentState.firstPin == pin -> {
                    // PINs match, save it
                    val hash = CryptoUtil.hashPin(pin)
                    securityPreferences.savePinHash(hash)
                    _uiState.value = currentState.copy(
                        isAuthenticated = true,
                        error = null
                    )
                }
                else -> {
                    // PINs don't match
                    _uiState.value = currentState.copy(
                        firstPin = null,
                        error = "PINs do not match"
                    )
                }
            }
        }
    }
    
    private fun handlePinAuthentication(pin: String) {
        viewModelScope.launch {
            val savedHash = securityPreferences.getPinHash()
            
            if (savedHash != null && CryptoUtil.verifyPin(pin, savedHash)) {
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = true,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Wrong PIN. Try again.",
                    shakeError = true
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, shakeError = false)
    }
    
    fun resetPin() {
        securityPreferences.clearPin()
        _uiState.value = PinUiState(isCreatingPin = true)
    }
}

data class PinUiState(
    val isCreatingPin: Boolean = false,
    val isAuthenticated: Boolean = false,
    val firstPin: String? = null,
    val error: String? = null,
    val shakeError: Boolean = false
)


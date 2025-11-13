package com.sunguard.vault.util

import java.security.MessageDigest
import java.security.SecureRandom

object CryptoUtil {
    
    fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
    
    fun verifyPin(pin: String, hash: String): Boolean {
        return hashPin(pin) == hash
    }
    
    fun generatePassword(
        length: Int = 16,
        useUppercase: Boolean = true,
        useNumbers: Boolean = true,
        useSymbols: Boolean = true
    ): String {
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        
        var chars = lowercase
        if (useUppercase) chars += uppercase
        if (useNumbers) chars += numbers
        if (useSymbols) chars += symbols
        
        val random = SecureRandom()
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
    
    fun calculatePasswordStrength(password: String): PasswordStrength {
        var score = 0
        
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        
        return when {
            score <= 2 -> PasswordStrength.WEAK
            score <= 4 -> PasswordStrength.FAIR
            score <= 5 -> PasswordStrength.GOOD
            else -> PasswordStrength.STRONG
        }
    }
}

enum class PasswordStrength {
    WEAK, FAIR, GOOD, STRONG
}


package com.sunguard.vault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sunguard.vault.data.local.VaultDatabase
import com.sunguard.vault.data.preferences.SecurityPreferences
import com.sunguard.vault.navigation.NavGraph
import com.sunguard.vault.ui.theme.SunGuardTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var securityPreferences: SecurityPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        enableEdgeToEdge()
        
        // Initialize security preferences
        securityPreferences = SecurityPreferences(this)
        
        setContent {
            val darkTheme by remember {
                mutableStateOf(securityPreferences.isDarkThemeEnabled())
            }
            
            var database by remember { mutableStateOf<VaultDatabase?>(null) }
            
            SunGuardTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Initialize database
                    LaunchedEffect(Unit) {
                        database = VaultDatabase.getInstance(
                            context = applicationContext
                        )
                    }
                    
                    if (database != null) {
                        val navController = rememberNavController()
                        NavGraph(
                            navController = navController,
                            database = database!!,
                            securityPreferences = securityPreferences
                        )
                    }
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Auto-lock feature can be implemented here
        if (securityPreferences.isAutoLockEnabled()) {
            // Close database on app pause for security
            // It will be reopened when user authenticates again
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        VaultDatabase.closeDatabase()
    }
}


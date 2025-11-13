package com.sunguard.vault.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sunguard.vault.ui.components.EgyptianCard
import com.sunguard.vault.ui.components.GoldenDivider
import com.sunguard.vault.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onChangePinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportVault(context, it) }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportDialog = true
        }
    }
    
    LaunchedEffect(uiState.exportSuccess, uiState.importSuccess) {
        if (uiState.exportSuccess || uiState.importSuccess) {
            delay(3000)
            viewModel.clearMessages()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    color = Gold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Gold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Background sarcophagus glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Gold.copy(alpha = 0.05f),
                                Color.Transparent,
                                Gold.copy(alpha = 0.05f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Security Section
                SettingsSection(title = "Security") {
                    SettingItem(
                        icon = Icons.Default.Lock,
                        title = "Change PIN",
                        subtitle = "Update your vault PIN",
                        onClick = onChangePinClick
                    )

                }
                
                // Backup & Restore Section
                SettingsSection(title = "Backup & Restore") {
                    SettingItem(
                        icon = Icons.Default.Upload,
                        title = "Export Vault",
                        subtitle = "Save your vault to a file",
                        onClick = {
                            val fileName = "sunguard_backup_${System.currentTimeMillis()}.json"
                            exportLauncher.launch(fileName)
                        }
                    )
                    
                    GoldenDivider()
                    
                    SettingItem(
                        icon = Icons.Default.Download,
                        title = "Import Vault",
                        subtitle = "Restore from backup file",
                        onClick = {
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    )
                }

                
                // About Section
                SettingsSection(title = "About") {
                    SettingItem(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = "1.0.0",
                        onClick = { }
                    )
                    
                    GoldenDivider()

                    SettingItem(
                        icon = Icons.Default.Info,
                        title = "Privacy Policy",
                        subtitle = "Tap to read",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sunguarrd.com/privacy-policy.html"))
                            context.startActivity(intent)
                        }
                    )

                    GoldenDivider()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Vault of Secrets Protected by Anubis",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Success/Error messages
            if (uiState.exportSuccess || uiState.importSuccess || 
                uiState.exportError != null || uiState.importError != null) {
                val isSuccess = uiState.exportSuccess || uiState.importSuccess
                val message = when {
                    uiState.exportSuccess -> "Vault exported successfully"
                    uiState.importSuccess -> "Vault imported successfully"
                    uiState.exportError != null -> uiState.exportError
                    else -> uiState.importError
                }
                
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSuccess) Gold else ErrorRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isSuccess) BlackObsidian else Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            message ?: "",
                            color = if (isSuccess) BlackObsidian else Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
    
    // Import confirmation dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { 
                showImportDialog = false
                pendingImportUri = null
            },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Gold
                )
            },
            title = { 
                Text(
                    "Import Vault?",
                    color = Gold
                ) 
            },
            text = { 
                Text(
                    "This will replace ALL existing vault entries with the imported data. This action cannot be undone.\n\nAre you sure you want to continue?",
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingImportUri?.let { uri ->
                            viewModel.importVault(context, uri)
                        }
                        showImportDialog = false
                        pendingImportUri = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Gold
                    )
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showImportDialog = false
                        pendingImportUri = null
                    }
                ) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Gold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        EgyptianCard {
            content()
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Gold.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun SettingSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Gold,
                checkedTrackColor = Gold.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}


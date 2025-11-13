package com.sunguard.vault.ui.screens.entry

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sunguard.vault.ui.components.EgyptianTextField
import com.sunguard.vault.ui.components.GoldenButton
import com.sunguard.vault.ui.components.TurquoiseButton
import com.sunguard.vault.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryFormScreen(
    viewModel: EntryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToGenerator: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showSaveAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            delay(3000)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditing) "Edit Entry" else "Add Entry",
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background papyrus effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                SandyLight.copy(alpha = 0.05f)
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
                // Service Name
                EgyptianTextField(
                    value = uiState.serviceName,
                    onValueChange = viewModel::onServiceNameChange,
                    label = "Service Name",
                    placeholder = "e.g., Google, Facebook",
                    leadingIcon = Icons.Default.Business,
                    isError = uiState.error == "Service name is required"
                )
                
                // Username
                EgyptianTextField(
                    value = uiState.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = "Username / Email",
                    placeholder = "your@email.com",
                    leadingIcon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    isError = uiState.error == "Username is required"
                )
                
                // Password
                EgyptianTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Password",
                    placeholder = "Enter password",
                    leadingIcon = Icons.Default.Lock,
                    trailingIcon = {
                        IconButton(onClick = viewModel::togglePasswordVisibility) {
                            Icon(
                                imageVector = if (uiState.isPasswordVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (uiState.isPasswordVisible) {
                                    "Hide password"
                                } else {
                                    "Show password"
                                },
                                tint = Gold
                            )
                        }
                    },
                    visualTransformation = if (uiState.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    isError = uiState.error == "Password is required"
                )
                
                // Generate Password Button
                TurquoiseButton(
                    text = "Generate Password",
                    onClick = { onNavigateToGenerator(uiState.password) },
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Notes
                EgyptianTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = "Notes (Optional)",
                    placeholder = "Additional information...",
                    leadingIcon = Icons.Default.Note,
                    singleLine = false,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Error message
                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = ErrorRed.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = ErrorRed
                            )
                            Text(
                                text = uiState.error ?: "",
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Save Button
                GoldenButton(
                    text = if (uiState.isEditing) "Update Entry" else "Save Entry",
                    onClick = {
                        viewModel.saveEntry {
                            showSaveAnimation = true
                        }
                    },
                    icon = Icons.Default.Save,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Save animation
            AnimatedVisibility(
                visible = showSaveAnimation,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                SaveSuccessAnimation(
                    onAnimationComplete = {
                        showSaveAnimation = false
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

@Composable
fun SaveSuccessAnimation(onAnimationComplete: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "save")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    LaunchedEffect(Unit) {
        delay(1500)
        onAnimationComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackObsidian.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Gold
            )
            
            Text(
                text = "Saved!",
                style = MaterialTheme.typography.headlineMedium,
                color = Gold
            )
            
            Text(
                text = "Blessed by Anubis",
                style = MaterialTheme.typography.bodyLarge,
                color = SandyLight.copy(alpha = 0.7f)
            )
        }
    }
}


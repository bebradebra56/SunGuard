package com.sunguard.vault.ui.screens.generator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.selection.SelectionContainer
import com.sunguard.vault.ui.components.GoldenButton
import com.sunguard.vault.ui.components.GoldenDivider
import com.sunguard.vault.ui.components.StrengthMeter
import com.sunguard.vault.ui.theme.*
import com.sunguard.vault.util.PasswordStrength
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    viewModel: GeneratorViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onPasswordSelected: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    LaunchedEffect(uiState.showCopiedMessage) {
        if (uiState.showCopiedMessage) {
            delay(2000)
            viewModel.hideCopiedConfirmation()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onNavigateBack != null) {
            TopAppBar(
                title = {
                    Text(
                        text = "Password Generator",
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
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Animated sun disk background
            AnimatedSunDisk()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (onNavigateBack == null) {
                    // Title for standalone screen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Password Generator",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Gold
                        )
                    }
                }
                
                // Generated password display
                GeneratedPasswordCard(
                    password = uiState.generatedPassword,
                    onCopy = {
                        copyToClipboard(context, uiState.generatedPassword)
                        viewModel.showCopiedConfirmation()
                    }
                )
                
                // Strength meter
                StrengthIndicator(strength = uiState.strength)
                
                GoldenDivider()
                
                // Length slider
                LengthSlider(
                    length = uiState.length,
                    onLengthChange = viewModel::onLengthChange
                )
                
                // Options
                OptionsSection(
                    includeUppercase = uiState.includeUppercase,
                    includeNumbers = uiState.includeNumbers,
                    includeSymbols = uiState.includeSymbols,
                    onUppercaseToggle = viewModel::onUppercaseToggle,
                    onNumbersToggle = viewModel::onNumbersToggle,
                    onSymbolsToggle = viewModel::onSymbolsToggle
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Generate button
                GoldenButton(
                    text = "Generate New",
                    onClick = viewModel::generatePassword,
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Use password button (if callback provided)
                if (onPasswordSelected != null) {
                    Button(
                        onClick = { onPasswordSelected(uiState.generatedPassword) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Turquoise,
                            contentColor = BlackObsidian
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Use This Password",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Copied confirmation
            if (uiState.showCopiedMessage) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Gold
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BlackObsidian
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Copied to clipboard!",
                            color = BlackObsidian,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedSunDisk() {
    val infiniteTransition = rememberInfiniteTransition(label = "sun")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Gold.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    radius = 800f
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
                .rotate(rotation)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            Gold.copy(alpha = 0.05f),
                            Color.Transparent,
                            Gold.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
    }
}

@Composable
fun GeneratedPasswordCard(
    password: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Generated Password",
                style = MaterialTheme.typography.labelLarge,
                color = Gold
            )
            
            SelectionContainer {
                Text(
                    text = password,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                )
            }
            
            Button(
                onClick = onCopy,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Turquoise,
                    contentColor = BlackObsidian
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Password")
            }
        }
    }
}

@Composable
fun StrengthIndicator(strength: PasswordStrength) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Password Strength",
                style = MaterialTheme.typography.labelLarge,
                color = Gold
            )
            Text(
                text = when (strength) {
                    PasswordStrength.WEAK -> "Weak"
                    PasswordStrength.FAIR -> "Fair"
                    PasswordStrength.GOOD -> "Good"
                    PasswordStrength.STRONG -> "Strong"
                },
                style = MaterialTheme.typography.labelLarge,
                color = when (strength) {
                    PasswordStrength.WEAK -> ErrorRed
                    PasswordStrength.FAIR -> WarningOrange
                    PasswordStrength.GOOD -> Turquoise
                    PasswordStrength.STRONG -> Gold
                }
            )
        }
        
        StrengthMeter(
            strength = when (strength) {
                PasswordStrength.WEAK -> 1
                PasswordStrength.FAIR -> 2
                PasswordStrength.GOOD -> 3
                PasswordStrength.STRONG -> 4
            }
        )
    }
}

@Composable
fun LengthSlider(
    length: Int,
    onLengthChange: (Float) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Length",
                style = MaterialTheme.typography.titleMedium,
                color = Gold
            )
            Text(
                text = length.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Slider(
            value = length.toFloat(),
            onValueChange = onLengthChange,
            valueRange = 4f..32f,
            steps = 27,
            colors = SliderDefaults.colors(
                thumbColor = Gold,
                activeTrackColor = Gold,
                inactiveTrackColor = Gold.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun OptionsSection(
    includeUppercase: Boolean,
    includeNumbers: Boolean,
    includeSymbols: Boolean,
    onUppercaseToggle: () -> Unit,
    onNumbersToggle: () -> Unit,
    onSymbolsToggle: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Options",
            style = MaterialTheme.typography.titleMedium,
            color = Gold
        )
        
        OptionItem(
            label = "A–Z",
            description = "Uppercase letters",
            checked = includeUppercase,
            onToggle = onUppercaseToggle
        )
        
        OptionItem(
            label = "0–9",
            description = "Numbers",
            checked = includeNumbers,
            onToggle = onNumbersToggle
        )
        
        OptionItem(
            label = "!@#",
            description = "Symbols",
            checked = includeSymbols,
            onToggle = onSymbolsToggle
        )
    }
}

@Composable
fun OptionItem(
    label: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) {
                Gold.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (checked) {
            androidx.compose.foundation.BorderStroke(2.dp, Gold)
        } else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (checked) Gold else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
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
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("password", text)
    clipboard.setPrimaryClip(clip)
}


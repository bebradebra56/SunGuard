package com.sunguard.vault.ui.screens.pin

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
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
import androidx.core.content.ContextCompat
import com.sunguard.vault.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PinScreen(
    viewModel: PinViewModel,
    onAuthenticated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var enteredPin by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            delay(300)
            onAuthenticated()
        }
    }
    
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            // Vibrate on error
            val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200)
            }
            
            delay(2000)
            viewModel.clearError()
            enteredPin = ""
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BlackObsidian,
                        ObsidianLight
                    )
                )
            )
    ) {
        // Animated background elements
        AnimatedBackgroundElements()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Eye of Horus icon
            EyeOfHorus(hasError = uiState.shakeError)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Title
            Text(
                text = when {
                    uiState.isCreatingPin && uiState.firstPin == null -> "Create your PIN"
                    uiState.isCreatingPin && uiState.firstPin != null -> "Confirm your PIN"
                    else -> "Enter your PIN"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = Gold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Vault of Secrets Protected by Anubis",
                style = MaterialTheme.typography.bodyMedium,
                color = SandyLight.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // PIN dots display
            PinDotsDisplay(pinLength = enteredPin.length)
            
            // Error message
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = uiState.error ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ErrorRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // PIN pad
            PinPad(
                onNumberClick = { number ->
                    if (enteredPin.length < 4) {
                        enteredPin += number
                        if (enteredPin.length == 4) {
                            viewModel.onPinEntered(enteredPin)
                            enteredPin = ""
                        }
                    }
                },
                onDeleteClick = {
                    if (enteredPin.isNotEmpty()) {
                        enteredPin = enteredPin.dropLast(1)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Forgot PIN button (only when not creating)
            if (!uiState.isCreatingPin) {
                TextButton(
                    onClick = { viewModel.resetPin() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Turquoise
                    )
                ) {
                    Text("Forgot PIN?")
                }
            }
        }
    }
}

@Composable
fun EyeOfHorus(hasError: Boolean) {
    val shake by remember { mutableStateOf(Animatable(0f)) }
    
    LaunchedEffect(hasError) {
        if (hasError) {
            repeat(3) {
                shake.animateTo(
                    targetValue = 15f,
                    animationSpec = tween(50)
                )
                shake.animateTo(
                    targetValue = -15f,
                    animationSpec = tween(50)
                )
            }
            shake.animateTo(0f)
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = Modifier
            .size(120.dp)
            .rotate(shake.value)
            .background(
                Gold.copy(alpha = glowAlpha),
                CircleShape
            )
            .border(3.dp, Gold, CircleShape)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = "Eye of Horus",
            modifier = Modifier.size(64.dp),
            tint = if (hasError) ErrorRed else BlackObsidian
        )
    }
}

@Composable
fun PinDotsDisplay(pinLength: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        repeat(4) { index ->
            PinDot(isFilled = index < pinLength)
        }
    }
}

@Composable
fun PinDot(isFilled: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dot_scale"
    )
    
    Box(
        modifier = Modifier
            .size(16.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isFilled) Gold else Gold.copy(alpha = 0.3f)
            )
    )
}

@Composable
fun PinPad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rows 1-3
        for (row in 0..2) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (col in 1..3) {
                    val number = (row * 3 + col).toString()
                    PinButton(
                        text = number,
                        onClick = { onNumberClick(number) }
                    )
                }
            }
        }
        
        // Row 4: empty, 0, delete
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.size(80.dp))
            
            PinButton(
                text = "0",
                onClick = { onNumberClick("0") }
            )
            
            PinButton(
                text = "⌫",
                onClick = onDeleteClick,
                isDelete = true
            )
        }
    }
}

@Composable
fun PinButton(
    text: String,
    onClick: () -> Unit,
    isDelete: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        if (isDelete) Turquoise else Gold.copy(alpha = 0.9f),
                        if (isDelete) TurquoiseDark else GoldDark
                    )
                )
            )
            .border(2.dp, Gold.copy(alpha = 0.5f), CircleShape)
            .clickable(
                onClick = {
                    isPressed = true
                    onClick()
                },
                onClickLabel = text
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            color = BlackObsidian,
            textAlign = TextAlign.Center
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
fun AnimatedBackgroundElements() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    // Torch flicker effect
    val torchAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "torch"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Gold.copy(alpha = torchAlpha),
                        Color.Transparent
                    ),
                    center = androidx.compose.ui.geometry.Offset(100f, 100f),
                    radius = 500f
                )
            )
    )
}


package com.sunguard.vault.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sunguard.vault.navigation.Screen
import com.sunguard.vault.ui.theme.*

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem(
                route = Screen.Vault.route,
                icon = Icons.Default.Lock,
                label = "Vault"
            ),
            BottomNavItem(
                route = Screen.Generator.route,
                icon = Icons.Default.AutoAwesome,
                label = "Generator"
            ),
            BottomNavItem(
                route = Screen.Settings.route,
                icon = Icons.Default.Settings,
                label = "Settings"
            )
        )
        
        items.forEach { item ->
            val selected = currentRoute == item.route
            
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Vault.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    BottomNavIcon(
                        icon = item.icon,
                        selected = selected
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BlackObsidian,
                    selectedTextColor = Gold,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = Gold
                )
            )
        }
    }
}

@Composable
fun BottomNavIcon(
    icon: ImageVector,
    selected: Boolean
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) BlackObsidian else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_color"
    )
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconColor,
        modifier = Modifier.size(24.dp)
    )
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)


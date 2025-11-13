package com.sunguard.vault.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.sunguard.vault.data.local.VaultDatabase
import com.sunguard.vault.data.local.entity.VaultEntry
import com.sunguard.vault.data.preferences.SecurityPreferences
import com.sunguard.vault.data.repository.VaultRepository
import com.sunguard.vault.ui.navigation.BottomNavigationBar
import com.sunguard.vault.ui.screens.entry.EntryFormScreen
import com.sunguard.vault.ui.screens.entry.EntryViewModel
import com.sunguard.vault.ui.screens.generator.GeneratorScreen
import com.sunguard.vault.ui.screens.generator.GeneratorViewModel
import com.sunguard.vault.ui.screens.pin.PinScreen
import com.sunguard.vault.ui.screens.pin.PinViewModel
import com.sunguard.vault.ui.screens.settings.SettingsScreen
import com.sunguard.vault.ui.screens.settings.SettingsViewModel
import com.sunguard.vault.ui.screens.vault.VaultListScreen
import com.sunguard.vault.ui.screens.vault.VaultViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Pin : Screen("pin")
    object Vault : Screen("vault")
    object AddEntry : Screen("add_entry")
    object DetailEntry : Screen("detail_entry/{entryId}") {
        fun createRoute(entryId: Long) = "detail_entry/$entryId"
    }
    object EditEntry : Screen("edit_entry/{entryId}") {
        fun createRoute(entryId: Long) = "edit_entry/$entryId"
    }
    object Generator : Screen("generator")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    database: VaultDatabase,
    securityPreferences: SecurityPreferences
) {
    val repository = remember { VaultRepository(database.vaultEntryDao()) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Определяем, нужна ли нижняя навигация для текущего экрана
    val showBottomBar = currentRoute in listOf(
        Screen.Vault.route,
        Screen.Generator.route,
        Screen.Settings.route
    )
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Pin.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            // PIN Screen - без padding от bottomBar
            composable(Screen.Pin.route) {
                Box(modifier = Modifier.padding(paddingValues)) {
                    val viewModel = remember { PinViewModel(securityPreferences) }
                    PinScreen(
                        viewModel = viewModel,
                        onAuthenticated = {
                            navController.navigate(Screen.Vault.route) {
                                popUpTo(Screen.Pin.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
            
            // Vault List Screen - имеет свой внутренний Scaffold
            composable(Screen.Vault.route) {
                val viewModel = remember { VaultViewModel(repository) }
                VaultListScreen(
                    viewModel = viewModel,
                    onEntryClick = { entryId ->
                        navController.navigate(Screen.DetailEntry.createRoute(entryId))
                    },
                    onAddClick = {
                        navController.navigate(Screen.AddEntry.route)
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            // Detail Entry Screen - без padding от bottomBar
            composable(
                route = Screen.DetailEntry.route,
                arguments = listOf(
                    navArgument("entryId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getLong("entryId")
                var entry by remember { mutableStateOf<VaultEntry?>(null) }
                
                LaunchedEffect(entryId) {
                    entry = entryId?.let { repository.getEntryById(it) }
                }
                
                entry?.let { vaultEntry ->
                    com.sunguard.vault.ui.screens.detail.EntryDetailScreen(
                        entry = vaultEntry,
                        onNavigateBack = { navController.popBackStack() },
                        onEdit = {
                            navController.navigate(Screen.EditEntry.createRoute(vaultEntry.id))
                        },
                        onDelete = {
                            CoroutineScope(Dispatchers.IO).launch {
                                repository.deleteEntry(vaultEntry)
                            }
                            navController.popBackStack()
                        }
                    )
                }
            }
            
            // Add Entry Screen - без padding от bottomBar
            composable(Screen.AddEntry.route) {
                val viewModel = remember { EntryViewModel(repository, null) }
                EntryFormScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGenerator = { currentPassword ->
                        // TODO: pass current password if needed
                        navController.navigate(Screen.Generator.route)
                    }
                )
            }
            
            // Edit Entry Screen - без padding от bottomBar
            composable(
                route = Screen.EditEntry.route,
                arguments = listOf(
                    navArgument("entryId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getLong("entryId")
                val viewModel = remember { EntryViewModel(repository, entryId) }
                EntryFormScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGenerator = { currentPassword ->
                        navController.navigate(Screen.Generator.route)
                    }
                )
            }
            
            // Generator Screen - имеет свой внутренний Scaffold
            composable(Screen.Generator.route) {
                val viewModel = remember { GeneratorViewModel() }
                val previousRoute = navController.previousBackStackEntry?.destination?.route

                GeneratorScreen(
                    viewModel = viewModel,
                    onNavigateBack = if (previousRoute != null &&
                        (previousRoute.startsWith("add_entry") ||
                         previousRoute.startsWith("edit_entry"))) {
                        { navController.popBackStack() }
                    } else null,
                    onPasswordSelected = if (previousRoute != null &&
                        (previousRoute.startsWith("add_entry") ||
                         previousRoute.startsWith("edit_entry"))) {
                        { password ->
                            // TODO: pass password back
                            navController.popBackStack()
                        }
                    } else null,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            // Settings Screen - имеет свой внутренний Scaffold
            composable(Screen.Settings.route) {
                val viewModel = remember { SettingsViewModel(repository, securityPreferences) }
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onChangePinClick = {
                        securityPreferences.clearPin()
                        navController.navigate(Screen.Pin.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

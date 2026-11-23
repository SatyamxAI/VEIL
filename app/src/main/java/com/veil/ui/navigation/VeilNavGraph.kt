package com.veil.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.veil.core.model.CloudAccount
import com.veil.core.model.CloudProviderType
import com.veil.core.model.MediaItem
import com.veil.core.model.MediaType
import com.veil.core.model.SyncStatus
import com.veil.ui.components.NavDestination
import com.veil.ui.components.VeilNavigationBar
import com.veil.ui.screens.AlbumsScreen
import com.veil.ui.screens.CloudAccountsScreen
import com.veil.ui.screens.GalleryScreen
import com.veil.ui.screens.MediaViewerScreen
import com.veil.ui.screens.SettingsScreen
import com.veil.ui.screens.VaultLockScreen
import com.veil.ui.screens.VaultSetupScreen

sealed class Screen(val route: String) {
    object Lock : Screen("lock")
    object Setup : Screen("setup")
    object Main : Screen("main")
    object Viewer : Screen("viewer/{itemId}") {
        fun createRoute(itemId: String) = "viewer/$itemId"
    }
}

@Composable
fun VeilNavGraph(
    navController: NavHostController = rememberNavController(),
    initialRoute: String = Screen.Lock.route,
    onTriggerBiometrics: () -> Unit = {}
) {
    // In-memory demo state for instant testing
    var mediaItems by remember {
        mutableStateOf(
            listOf(
                MediaItem(
                    id = "photo_1",
                    vaultId = "default_vault",
                    uri = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                    fileName = "Yosemite_Mist.jpg",
                    mimeType = "image/jpeg",
                    mediaType = MediaType.IMAGE,
                    sizeBytes = 3_450_000,
                    width = 3840,
                    height = 2160,
                    captureTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                    syncStatus = SyncStatus.SYNCED,
                    isFavorite = true
                ),
                MediaItem(
                    id = "photo_2",
                    vaultId = "default_vault",
                    uri = "https://images.unsplash.com/photo-1511556532299-8f662fc26c06?w=800",
                    fileName = "Architecture_Clean.jpg",
                    mimeType = "image/jpeg",
                    mediaType = MediaType.IMAGE,
                    sizeBytes = 2_150_000,
                    width = 4000,
                    height = 3000,
                    captureTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                    syncStatus = SyncStatus.SYNCING,
                    isFavorite = false
                ),
                MediaItem(
                    id = "photo_3",
                    vaultId = "default_vault",
                    uri = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800",
                    fileName = "Foggy_Forest.jpg",
                    mimeType = "image/jpeg",
                    mediaType = MediaType.IMAGE,
                    sizeBytes = 4_820_000,
                    width = 4200,
                    height = 2800,
                    captureTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 48,
                    syncStatus = SyncStatus.LOCAL_ONLY,
                    isFavorite = false
                )
            )
        )
    }

    val accounts = remember {
        listOf(
            CloudAccount(
                id = "acc_local",
                vaultId = "default_vault",
                providerType = CloudProviderType.LOCAL_SANDBOX,
                accountName = "Private Device Sandbox",
                isPrimary = true
            )
        )
    }

    NavHost(
        navController = navController,
        startDestination = initialRoute
    ) {
        composable(Screen.Lock.route) {
            VaultLockScreen(
                vaultName = "VEIL Main Vault",
                onUnlockWithPassword = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Lock.route) { inclusive = true }
                    }
                },
                onTriggerBiometrics = onTriggerBiometrics,
                onNavigateToSetup = { navController.navigate(Screen.Setup.route) },
                onNavigateToRecovery = { navController.navigate(Screen.Setup.route) }
            )
        }

        composable(Screen.Setup.route) {
            VaultSetupScreen(
                onVaultCreated = { _, _, _ ->
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Main.route) {
            val mainNavController = rememberNavController()
            val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination?.route ?: NavDestination.Photos.route

            Scaffold(
                bottomBar = {
                    VeilNavigationBar(
                        currentRoute = currentDestination,
                        onNavigate = { route ->
                            mainNavController.navigate(route) {
                                popUpTo(mainNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) { innerPadding ->
                NavHost(
                    navController = mainNavController,
                    startDestination = NavDestination.Photos.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(NavDestination.Photos.route) {
                        GalleryScreen(
                            vaultName = "VEIL",
                            items = mediaItems,
                            onItemClick = { item ->
                                navController.navigate(Screen.Viewer.createRoute(item.id))
                            },
                            onLockVault = {
                                navController.navigate(Screen.Lock.route) {
                                    popUpTo(Screen.Main.route) { inclusive = true }
                                }
                            },
                            onTriggerSync = { /* Sync trigger */ }
                        )
                    }

                    composable(NavDestination.Albums.route) {
                        AlbumsScreen(
                            onAlbumClick = { /* open album */ },
                            onCreateAlbum = { /* create */ }
                        )
                    }

                    composable(NavDestination.Cloud.route) {
                        CloudAccountsScreen(
                            accounts = accounts,
                            onAddProvider = { /* add provider */ }
                        )
                    }

                    composable(NavDestination.Settings.route) {
                        SettingsScreen(
                            onLockVault = {
                                navController.navigate(Screen.Lock.route) {
                                    popUpTo(Screen.Main.route) { inclusive = true }
                                }
                            },
                            onClearCache = { /* clear */ }
                        )
                    }
                }
            }
        }

        composable(Screen.Viewer.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            val item = mediaItems.firstOrNull { it.id == itemId }
            if (item != null) {
                MediaViewerScreen(
                    item = item,
                    onBack = { navController.popBackStack() },
                    onToggleFavorite = { clicked ->
                        mediaItems = mediaItems.map {
                            if (it.id == clicked.id) it.copy(isFavorite = !it.isFavorite) else it
                        }
                    },
                    onDelete = { deleted ->
                        mediaItems = mediaItems.filter { it.id != deleted.id }
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

package com.veil.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.veil.ui.theme.VeilBlack
import com.veil.ui.theme.VeilEmerald
import com.veil.ui.theme.VeilSurfaceVariant
import com.veil.ui.theme.VeilTextPrimary
import com.veil.ui.theme.VeilTextSecondary

sealed class NavDestination(val route: String, val label: String, val icon: ImageVector) {
    object Photos : NavDestination("photos", "Photos", Icons.Default.PhotoLibrary)
    object Albums : NavDestination("albums", "Albums", Icons.Default.Folder)
    object Cloud : NavDestination("cloud", "Cloud", Icons.Default.Cloud)
    object Settings : NavDestination("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun VeilNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavDestination.Photos,
        NavDestination.Albums,
        NavDestination.Cloud,
        NavDestination.Settings
    )

    NavigationBar(
        containerColor = VeilBlack,
        modifier = modifier
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VeilEmerald,
                    selectedTextColor = VeilEmerald,
                    indicatorColor = VeilSurfaceVariant,
                    unselectedIconColor = VeilTextSecondary,
                    unselectedTextColor = VeilTextSecondary
                )
            )
        }
    }
}

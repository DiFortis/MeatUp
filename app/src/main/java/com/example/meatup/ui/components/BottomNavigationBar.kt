package com.example.meatup.ui.components

import androidx.camera.core.Camera
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.example.meatup.AuthState

data class NavItem(val label: String, val icon: ImageVector, val screen: AuthState)

@Composable
fun BottomNavigationBar(selectedItem: AuthState, onItemSelected: (AuthState, Boolean) -> Unit) {
    val items = listOf(
        NavItem("Profile", Icons.Default.Person, AuthState.PROFILE),
        NavItem("Shops", Icons.Default.ShoppingCart, AuthState.NEAREST_MEAT_SHOPS),
        NavItem("Sausage", Icons.Default.Notifications, AuthState.SAUSAGE_ANIMATION),
        NavItem("Scan", Icons.Default.Add, AuthState.BARCODE_SCANNER),
        NavItem("List", Icons.AutoMirrored.Filled.List, AuthState.BARCODE_LIST)
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = {
                    Text(
                        item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = selectedItem == item.screen,
                onClick = {
                    onItemSelected(item.screen, item.screen == AuthState.NEAREST_MEAT_SHOPS)
                }
            )
        }
    }
}
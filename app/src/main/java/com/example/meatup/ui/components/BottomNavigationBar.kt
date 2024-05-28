package com.example.meatup.ui.components

import androidx.compose.material.icons.Icons
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
        NavItem("Details", Icons.Default.Info, AuthState.USER_DETAILS),
        NavItem("Password", Icons.Default.Lock, AuthState.CHANGE_PASSWORD),
        NavItem("Meat Shops", Icons.Default.ShoppingCart, AuthState.NEAREST_MEAT_SHOPS),
        NavItem("Sausage", Icons.Default.Notifications, AuthState.SAUSAGE_ANIMATION)
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

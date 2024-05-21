package com.example.meatup.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.meatup.AuthState

data class NavItem(val label: String, val icon: ImageVector, val screen: AuthState)

@Composable
fun BottomNavigationBar(selectedItem: AuthState, onItemSelected: (AuthState) -> Unit) {
    val items = listOf(
        NavItem("Profile", Icons.Default.Person, AuthState.PROFILE),
        NavItem("Details", Icons.Default.Info, AuthState.USER_DETAILS),
        NavItem("Change Password", Icons.Default.Lock, AuthState.CHANGE_PASSWORD)
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selectedItem == item.screen,
                onClick = { onItemSelected(item.screen) }
            )
        }
    }
}

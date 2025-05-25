package com.kotlin.connectit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home_tab", "Home", Icons.Default.Home) // Tambahkan ikon
    object Profile : Screen("profile_tab", "Profile", Icons.Default.Person) // Tambahkan ikon
    object Search : Screen("search_tab", "Search", Icons.Default.Search) // Tambahkan ikon
}
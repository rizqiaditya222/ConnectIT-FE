package com.kotlin.connectit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val filledIcon: ImageVector, // Ikon saat terpilih
    val outlinedIcon: ImageVector // Ikon saat tidak terpilih
) {
    object Home : Screen(
        AppDestinations.HOME_TAB,
        "Home",
        Icons.Filled.Home,
        Icons.Outlined.Home
    )
    object Search : Screen(
        AppDestinations.SEARCH_TAB,
        "Search",
        Icons.Filled.Search, // Menggunakan Filled.Search untuk konsistensi jika terpilih
        Icons.Outlined.Search
    )
    object Profile : Screen(
        AppDestinations.PROFILE_TAB,
        "Profile",
        Icons.Filled.Person,
        Icons.Outlined.Person
    )
    // Tambahkan item lain jika ada
}
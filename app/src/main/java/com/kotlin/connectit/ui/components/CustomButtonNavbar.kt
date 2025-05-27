package com.kotlin.connectit.ui.home // Sesuaikan package Anda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar // ✨ Menggunakan NavigationBar M3
import androidx.compose.material3.NavigationBarItem // ✨ Menggunakan NavigationBarItem M3
import androidx.compose.material3.NavigationBarItemDefaults // ✨ Untuk kustomisasi warna
import androidx.compose.material3.Surface // Untuk elevasi dan background
import androidx.compose.material3.Text // Jika ingin menampilkan label
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination // Import yang benar
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kotlin.connectit.navigation.Screen // Import Screen yang sudah diupdate

@Composable
fun CustomBottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Profile,
    )

    // Menggunakan Surface untuk memberi background dan elevasi (bayangan)
    // Ini menggantikan Column dan Box divider atas Anda
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp), // Memberi bayangan seperti BottomAppBar
        color = Color(0xFF191A1F) // Warna background bottom bar
    ) {
        NavigationBar(
            modifier = Modifier.height(64.dp), // Tinggi standar bottom navigation bar
            containerColor = Color.Transparent, // Biarkan Surface yang menangani background
            tonalElevation = 0.dp // Hapus tonal elevation default jika sudah ada shadow di Surface
        ) {
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    },
                    icon = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (isSelected) screen.filledIcon else screen.outlinedIcon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(26.dp) // Ukuran ikon sedikit lebih besar
                            )
                            // Indikator garis kustom Anda
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .height(3.dp)
                                        .width(28.dp) // Lebar indikator disesuaikan
                                        .background(
                                            Color(0xFF8B5CF6), // Warna indikator
                                            RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                        )
                                )
                            } else {
                                // Beri ruang kosong agar tinggi item konsisten
                                Spacer(modifier = Modifier.height(7.dp)) // 4dp (spasi) + 3dp (tinggi box)
                            }
                        }
                    },
                    // Anda bisa menambahkan label teks di sini jika mau:
                    // label = { Text(screen.title, fontSize = 10.sp) },
                    alwaysShowLabel = false, // Tampilkan label hanya jika terpilih, atau selalu (jika label ada)
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF8B5CF6), // Warna ikon saat terpilih
                        unselectedIconColor = Color.White.copy(alpha = 0.8f), // Warna ikon saat tidak terpilih
                        selectedTextColor = Color(0xFF8B5CF6), // Jika menggunakan label
                        unselectedTextColor = Color.Gray, // Jika menggunakan label
                        indicatorColor = Color.Transparent // Kita menggunakan indikator garis kustom
                    )
                )
            }
        }
    }
}
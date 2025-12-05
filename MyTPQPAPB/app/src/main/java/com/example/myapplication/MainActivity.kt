package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * MainActivity - Aktivitas utama aplikasi TPQ
 * Mengatur navigasi bottom navigation dengan 4 menu utama:
 * 1. Siswa - untuk mengelola data santri
 * 2. Absensi - untuk pencatatan kehadiran
 * 3. Laporan - untuk melihat statistik dan laporan
 * 4. Pengaturan - untuk konfigurasi aplikasi
 */
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Menggunakan tema Islami dengan warna hijau dan emas
            MyApplicationTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Setup navigasi controller untuk perpindahan antar halaman
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    Scaffold(
                        // Bottom Navigation Bar dengan 4 menu utama
                        bottomBar = {
                            NavigationBar {
                                // Loop untuk membuat item navigasi dari Screen.bottomNavItems
                                Screen.bottomNavItems.forEach { screen ->
                                    // Cek apakah screen saat ini sedang aktif
                                    val selected = currentDestination?.hierarchy?.any { 
                                        it.route == screen.route 
                                    } == true
                                    
                                    NavigationBarItem(
                                        // Icon dinamis berdasarkan screen.iconRoute
                                        icon = { 
                                            Icon(
                                                imageVector = when(screen.iconRoute) {
                                                    "person" -> Icons.Default.Person      // Icon untuk menu Siswa
                                                    "date_range" -> Icons.Default.DateRange // Icon untuk menu Absensi
                                                    "bar_chart" -> Icons.Default.BarChart   // Icon untuk menu Laporan
                                                    "settings" -> Icons.Default.Settings    // Icon untuk menu Pengaturan
                                                    else -> Icons.Default.Person
                                                },
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = { Text(screen.title) },
                                        selected = selected,
                                        onClick = {
                                            // Navigasi ke screen yang dipilih dengan konfigurasi state
                                            navController.navigate(screen.route) {
                                                // Pop ke start destination untuk menghindari back stack yang panjang
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        // Container untuk konten utama dengan padding dari bottom navigation
                        AppNavigation(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
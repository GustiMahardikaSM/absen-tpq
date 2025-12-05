package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.attendance.AttendanceScreen
import com.example.myapplication.ui.screens.student.AddEditStudentScreen
import com.example.myapplication.ui.screens.student.StudentListScreen
import com.example.myapplication.ui.screens.student.StudentReportScreen
import com.example.myapplication.ui.screens.student.ReportScreen
import com.example.myapplication.ui.screens.settings.SettingsScreen

/**
 * AppNavigation - Komponen navigasi utama aplikasi TPQ
 * Mengatur routing antar screen dengan parameter passing
 * Menggunakan Navigation Component untuk perpindahan halaman
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.StudentList.route, // Halaman awal: Daftar Siswa
        modifier = modifier
    ) {
        // Route untuk halaman daftar siswa (halaman utama)
        composable(Screen.StudentList.route) {
            StudentListScreen(
                onAddStudent = { navController.navigate(Screen.AddEditStudent.createRoute()) }, // Navigasi ke form tambah siswa
                onEditStudent = { studentCode -> 
                    navController.navigate(Screen.AddEditStudent.createRoute(studentCode)) // Navigasi ke form edit siswa dengan parameter
                },
                onNavigateToAttendance = { navController.navigate(Screen.Attendance.route) }, // Navigasi ke halaman absensi
                onViewStudentReport = { studentCode ->
                    navController.navigate(Screen.StudentReport.createRoute(studentCode)) // Navigasi ke laporan siswa dengan parameter
                }
            )
        }

        // Route untuk form tambah/edit siswa dengan parameter studentCode
        composable(
            route = Screen.AddEditStudent.route,
            arguments = listOf(
                navArgument("studentCode") {
                    type = NavType.StringType
                    nullable = true // Nullable untuk mode tambah siswa baru
                    defaultValue = null
                }
            )
        ) {
            AddEditStudentScreen(
                onNavigateBack = { navController.popBackStack() } // Kembali ke halaman sebelumnya
            )
        }

        // Route untuk halaman absensi
        composable(Screen.Attendance.route) {
            AttendanceScreen(
                onNavigateBack = { navController.popBackStack() } // Kembali ke halaman sebelumnya
            )
        }

        // Route untuk laporan detail siswa dengan parameter studentCode
        composable(
            route = Screen.StudentReport.route,
            arguments = listOf(
                navArgument("studentCode") {
                    type = NavType.StringType // Required parameter untuk laporan siswa
                }
            )
        ) {
            StudentReportScreen(
                onNavigateBack = { navController.popBackStack() } // Kembali ke halaman sebelumnya
            )
        }

        // Route untuk halaman laporan umum
        composable(Screen.Report.route) {
            ReportScreen(
                onViewStudentReport = { studentCode ->
                    navController.navigate(Screen.StudentReport.createRoute(studentCode)) // Navigasi ke laporan detail siswa
                }
            )
        }

        // Route untuk halaman pengaturan
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() } // Kembali ke halaman sebelumnya
            )
        }
    }
} 
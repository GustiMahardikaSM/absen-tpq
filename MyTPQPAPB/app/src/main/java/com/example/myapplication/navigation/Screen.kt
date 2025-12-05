package com.example.myapplication.navigation

/**
 * Screen - Sealed class untuk mendefinisikan route navigasi aplikasi TPQ
 * Mengatur semua route dan parameter yang digunakan dalam navigasi
 */
sealed class Screen(val route: String) {
    // Route untuk halaman daftar siswa (halaman utama)
    object StudentList : Screen("student_list")
    
    // Route untuk form tambah/edit siswa dengan parameter studentCode
    object AddEditStudent : Screen("add_edit_student?studentCode={studentCode}") {
        /**
         * Helper function untuk membuat route dengan parameter studentCode
         * @param studentCode Kode siswa (null untuk mode tambah baru)
         * @return Route string dengan parameter
         */
        fun createRoute(studentCode: String? = null) = "add_edit_student?studentCode=$studentCode"
    }
    
    // Route untuk halaman absensi
    object Attendance : Screen("attendance")
    
    // Route untuk laporan detail siswa dengan parameter studentCode
    object StudentReport : Screen("student_report/{studentCode}") {
        /**
         * Helper function untuk membuat route dengan parameter studentCode
         * @param studentCode Kode siswa (required)
         * @return Route string dengan parameter
         */
        fun createRoute(studentCode: String) = "student_report/$studentCode"
    }
    
    // Route untuk halaman laporan umum
    object Report : Screen("report")
    
    // Route untuk halaman pengaturan
    object Settings : Screen("settings")

    /**
     * BottomNavItem - Sealed class untuk item bottom navigation
     * Mendefinisikan route, title, dan icon untuk setiap menu utama
     */
    sealed class BottomNavItem(
        val route: String, // Route untuk navigasi
        val title: String, // Label yang ditampilkan
        val iconRoute: String // Nama icon yang digunakan
    ) {
        // Menu Siswa - untuk mengelola data santri
        object Students : BottomNavItem(
            route = "student_list",
            title = "Siswa",
            iconRoute = "person"
        )
        
        // Menu Absensi - untuk pencatatan kehadiran
        object Attendance : BottomNavItem(
            route = "attendance",
            title = "Absensi",
            iconRoute = "date_range"
        )
        
        // Menu Laporan - untuk melihat statistik dan laporan
        object Report : BottomNavItem(
            route = "report",
            title = "Laporan",
            iconRoute = "bar_chart"
        )
        
        // Menu Pengaturan - untuk konfigurasi aplikasi
        object Settings : BottomNavItem(
            route = "settings",
            title = "Pengaturan",
            iconRoute = "settings"
        )
    }

    companion object {
        /**
         * List semua item bottom navigation
         * Digunakan untuk membuat bottom navigation bar
         */
        val bottomNavItems = listOf(
            BottomNavItem.Students,    // Menu Siswa
            BottomNavItem.Attendance,  // Menu Absensi
            BottomNavItem.Report,      // Menu Laporan
            BottomNavItem.Settings     // Menu Pengaturan
        )
    }
} 
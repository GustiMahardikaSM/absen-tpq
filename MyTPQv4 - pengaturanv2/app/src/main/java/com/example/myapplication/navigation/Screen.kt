package com.example.myapplication.navigation

sealed class Screen(val route: String) {
    object StudentList : Screen("student_list")
    object AddEditStudent : Screen("add_edit_student?studentId={studentId}") {
        fun createRoute(studentId: Long = -1L) = "add_edit_student?studentId=$studentId"
    }
    object Attendance : Screen("attendance")
    object StudentReport : Screen("student_report/{studentId}") {
        fun createRoute(studentId: Long) = "student_report/$studentId"
    }
    object Report : Screen("report")
    object Settings : Screen("settings")

    // Bottom Navigation Items
    sealed class BottomNavItem(
        val route: String,
        val title: String,
        val iconRoute: String
    ) {
        object Students : BottomNavItem(
            route = "student_list",
            title = "Siswa",
            iconRoute = "person"
        )
        object Attendance : BottomNavItem(
            route = "attendance",
            title = "Absensi",
            iconRoute = "date_range"
        )
        object Report : BottomNavItem(
            route = "report",
            title = "Laporan",
            iconRoute = "bar_chart"
        )
        object Settings : BottomNavItem(
            route = "settings",
            title = "Pengaturan",
            iconRoute = "settings"
        )
    }

    companion object {
        val bottomNavItems = listOf(
            BottomNavItem.Students,
            BottomNavItem.Attendance,
            BottomNavItem.Report,
            BottomNavItem.Settings
        )
    }
} 
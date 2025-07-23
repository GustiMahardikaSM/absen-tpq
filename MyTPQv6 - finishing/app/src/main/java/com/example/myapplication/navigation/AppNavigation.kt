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

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.StudentList.route,
        modifier = modifier
    ) {
        composable(Screen.StudentList.route) {
            StudentListScreen(
                onAddStudent = { navController.navigate(Screen.AddEditStudent.createRoute()) },
                onEditStudent = { studentId -> 
                    navController.navigate(Screen.AddEditStudent.createRoute(studentId))
                },
                onNavigateToAttendance = { navController.navigate(Screen.Attendance.route) },
                onViewStudentReport = { studentId ->
                    navController.navigate(Screen.StudentReport.createRoute(studentId))
                }
            )
        }

        composable(
            route = Screen.AddEditStudent.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddEditStudentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Attendance.route) {
            AttendanceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.StudentReport.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.LongType
                }
            )
        ) {
            StudentReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Report.route) {
            ReportScreen(
                onViewStudentReport = { studentId ->
                    navController.navigate(Screen.StudentReport.createRoute(studentId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 
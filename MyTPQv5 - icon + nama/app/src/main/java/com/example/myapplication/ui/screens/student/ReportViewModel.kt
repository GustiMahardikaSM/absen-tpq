package com.example.myapplication.ui.screens.student

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val studentDao = database.studentDao()
    private val attendanceDao = database.attendanceDao()

    private val _reportList = MutableStateFlow<List<StudentReportItem>>(emptyList())
    val reportList: StateFlow<List<StudentReportItem>> = _reportList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadReport()
    }

    private fun loadReport() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val students = studentDao.getAllStudents().first()
                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(29)
                val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val report = students.map { student ->
                    val presentCount = attendanceDao.getAttendanceCountInRange(student.id, startMillis, endMillis)
                    StudentReportItem(
                        studentId = student.id,
                        studentName = student.name,
                        presentCount = presentCount
                    )
                }
                _reportList.value = report
            } finally {
                _isLoading.value = false
            }
        }
    }
}

data class StudentReportItem(
    val studentId: Long,
    val studentName: String,
    val presentCount: Int
) 
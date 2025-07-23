package com.example.myapplication.ui.screens.student

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Student
import com.example.myapplication.data.Attendance
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine

class StudentListViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val studentDao = database.studentDao()
    private val attendanceDao = database.attendanceDao()

    val students = studentDao.getAllStudents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Map studentId ke attendance terakhir
    private val _latestAttendanceMap = MutableStateFlow<Map<Long, Attendance>>(emptyMap())
    val latestAttendanceMap: StateFlow<Map<Long, Attendance>> = _latestAttendanceMap.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            students.collect { list ->
                val map = mutableMapOf<Long, Attendance>()
                list.forEach { student ->
                    val att = attendanceDao.getLastAttendanceForStudent(student.id)
                    if (att != null) map[student.id] = att
                }
                _latestAttendanceMap.value = map
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Simulasi delay untuk loading, Room Flow auto-update
            delay(700)
            _isRefreshing.value = false
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            studentDao.deleteStudent(student)
        }
    }
} 
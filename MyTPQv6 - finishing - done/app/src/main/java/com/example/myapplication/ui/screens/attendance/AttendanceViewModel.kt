package com.example.myapplication.ui.screens.attendance

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Attendance
import com.example.myapplication.data.Student
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val studentDao = database.studentDao()
    private val attendanceDao = database.attendanceDao()

    var selectedDate by mutableStateOf(LocalDate.now())
        private set

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    private val _attendanceMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val attendanceState = combine(_students, _attendanceMap) { students, attendanceMap ->
        students.map { student ->
            StudentAttendance(
                student = student,
                isPresent = attendanceMap[student.studentCode] ?: false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val attendanceStats = combine(_students, _attendanceMap) { students, attendanceMap ->
        AttendanceStats(
            totalStudents = students.size,
            presentCount = attendanceMap.count { it.value }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AttendanceStats(0, 0)
    )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            studentDao.getAllStudents().collect { students ->
                _students.value = students
                loadAttendanceForDate()
            }
        }
    }

    private fun loadAttendanceForDate() {
        viewModelScope.launch {
            val date = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            attendanceDao.getAttendanceByDate(date).collect { attendanceList ->
                _attendanceMap.value = attendanceList.associate { it.studentCode to it.isPresent }
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        selectedDate = date
        loadAttendanceForDate()
    }

    fun toggleAttendance(studentCode: String) {
        viewModelScope.launch {
            val date = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val currentValue = _attendanceMap.value[studentCode] ?: false
            val attendance = Attendance(
                studentCode = studentCode,
                date = date,
                isPresent = !currentValue
            )
            attendanceDao.insertOrUpdateAttendance(attendance)
        }
    }

    fun submitAttendanceWithDetail(
        studentCode: String,
        isPresent: Boolean,
        iqroNumber: Int?,
        iqroPage: Int?,
        quranSurah: Int?,
        quranAyat: Int?,
        isPassed: Boolean,
        catatanGuru: String?
    ) {
        viewModelScope.launch {
            val date = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val attendance = Attendance(
                studentCode = studentCode,
                date = date,
                isPresent = isPresent,
                iqroNumber = iqroNumber,
                iqroPage = iqroPage,
                quranSurah = quranSurah,
                quranAyat = quranAyat,
                isPassed = isPassed,
                catatanGuru = catatanGuru
            )
            attendanceDao.insertOrUpdateAttendance(attendance)
            if (isPassed) {
                // Update Student jika lulus
                val student = studentDao.getStudentByCode(studentCode)
                if (student != null) {
                    val updatedStudent = student.copy(
                        iqroNumber = iqroNumber ?: student.iqroNumber,
                        iqroPage = iqroPage ?: student.iqroPage,
                        quranSurah = quranSurah ?: student.quranSurah,
                        quranAyat = quranAyat ?: student.quranAyat
                    )
                    studentDao.updateStudent(updatedStudent)
                }
            }
            loadAttendanceForDate()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadData()
            _isRefreshing.value = false
        }
    }
}

data class StudentAttendance(
    val student: Student,
    val isPresent: Boolean
)

data class AttendanceStats(
    val totalStudents: Int,
    val presentCount: Int
) 
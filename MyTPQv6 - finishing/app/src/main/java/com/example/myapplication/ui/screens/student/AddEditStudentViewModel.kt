package com.example.myapplication.ui.screens.student

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Student
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class AddEditStudentViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val studentDao = database.studentDao()
    private val studentId: Long = savedStateHandle["studentId"] ?: -1L

    var studentName by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(true)
        private set

    var gender by mutableStateOf("")
        private set
    var birthDate by mutableStateOf<Long?>(null)
        private set
    var positionType by mutableStateOf("Iqro") // default ke Iqro
        private set
    var iqroNumber by mutableStateOf(1)
        private set
    var iqroPage by mutableStateOf<Int?>(null)
        private set
    var quranSurah by mutableStateOf(1)
        private set
    var quranAyat by mutableStateOf(1)
        private set

    init {
        if (studentId != -1L) {
            viewModelScope.launch {
                studentDao.getStudentById(studentId)?.let { student ->
                    studentName = student.name
                    gender = student.gender ?: ""
                    birthDate = student.birthDate
                    positionType = student.positionType ?: "Iqro"
                    iqroNumber = student.iqroNumber ?: 1
                    iqroPage = student.iqroPage
                    quranSurah = student.quranSurah ?: 1
                    quranAyat = student.quranAyat ?: 1
                }
                isLoading = false
            }
        } else {
            iqroPage = null
            isLoading = false
        }
    }

    fun onNameChange(newName: String) {
        studentName = newName
    }

    fun onGenderChange(newGender: String) { gender = newGender }
    fun onBirthDateChange(newDate: Long?) { birthDate = newDate }
    fun onPositionTypeChange(newType: String) { positionType = newType }
    fun onIqroNumberChange(newNum: Int) { iqroNumber = newNum }
    fun onIqroPageChange(newPage: Int?) { iqroPage = newPage }
    fun onQuranSurahChange(newSurah: Int) { quranSurah = newSurah }
    fun onQuranAyatChange(newAyat: Int) { quranAyat = newAyat }

    suspend fun saveStudent(): Boolean {
        if (studentName.isBlank()) return false
        return try {
            val now = System.currentTimeMillis()
            val code = if (studentId == -1L) {
                SimpleDateFormat("yyMMddHHmmss").format(Date(now))
            } else null
            val student = Student(
                id = if (studentId == -1L) 0 else studentId,
                name = studentName,
                gender = gender,
                birthDate = birthDate,
                positionType = positionType,
                iqroNumber = if (positionType == "Iqro") iqroNumber else null,
                iqroPage = if (positionType == "Iqro") iqroPage else null,
                quranSurah = if (positionType == "Quran") quranSurah else null,
                quranAyat = if (positionType == "Quran") quranAyat else null,
                studentCode = code
            )
            if (studentId == -1L) {
                studentDao.insertStudent(student)
            } else {
                studentDao.updateStudent(student)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
} 
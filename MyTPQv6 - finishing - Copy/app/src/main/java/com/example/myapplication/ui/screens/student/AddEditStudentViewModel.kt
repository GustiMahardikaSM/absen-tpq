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
    private val studentCode: String? = savedStateHandle["studentCode"]

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
    var iqroNumber by mutableStateOf(1) // default 1
        private set
    var iqroPage by mutableStateOf(1) // default 1
        private set
    var quranSurah by mutableStateOf(1)
        private set
    var quranAyat by mutableStateOf(1)
        private set

    init {
        if (studentCode != null) {
            viewModelScope.launch {
                studentDao.getStudentByCode(studentCode)?.let { student ->
                    studentName = student.name
                    gender = student.gender ?: ""
                    birthDate = student.birthDate
                    positionType = student.positionType ?: "Iqro"
                    iqroNumber = student.iqroNumber ?: 1
                    iqroPage = student.iqroPage ?: 1
                    quranSurah = student.quranSurah ?: 1
                    quranAyat = student.quranAyat ?: 1
                }
                isLoading = false
            }
        } else {
            iqroPage = 1
            iqroNumber = 1
            positionType = "Iqro"
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
    fun onIqroPageChange(newPage: Int?) { iqroPage = newPage ?: 1 }
    fun onQuranSurahChange(newSurah: Int) { quranSurah = newSurah }
    fun onQuranAyatChange(newAyat: Int) { quranAyat = newAyat }

    suspend fun saveStudent(): Boolean {
        if (studentName.isBlank()) return false
        return try {
            val now = System.currentTimeMillis()
            val code = if (studentCode == null) {
                SimpleDateFormat("yyMMddHHmmss").format(Date(now))
            } else studentCode
            val student = Student(
                studentCode = code,
                name = studentName,
                gender = gender,
                birthDate = birthDate,
                positionType = positionType,
                iqroNumber = if (positionType == "Iqro") (iqroNumber.takeIf { it > 0 } ?: 1) else null,
                iqroPage = if (positionType == "Iqro") (iqroPage.takeIf { it > 0 } ?: 1) else null,
                quranSurah = if (positionType == "Quran") quranSurah else null,
                quranAyat = if (positionType == "Quran") quranAyat else null
            )
                studentDao.insertStudent(student)
            true
        } catch (e: Exception) {
            false
        }
    }
} 
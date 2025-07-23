package com.example.myapplication.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Attendance
import com.example.myapplication.data.Student
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class SettingsUiState(
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val message: String? = null,
    val isError: Boolean = false
)

data class ExportData(
    val students: List<Student>,
    val attendances: List<Attendance>,
    val exportTimestamp: Long = System.currentTimeMillis()
)

class SettingsViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    loadingMessage = "Mengekspor data..."
                )
                
                withContext(Dispatchers.IO) {
                    val database = AppDatabase.getDatabase(context)
                    val studentDao = database.studentDao()
                    val attendanceDao = database.attendanceDao()
                    
                    // Get all data
                    val students = studentDao.getAllStudentsList()
                    val attendances = attendanceDao.getAllAttendances()
                    
                    // Create export data object
                    val exportData = ExportData(
                        students = students,
                        attendances = attendances
                    )
                    
                    // Convert to JSON
                    val gson = Gson()
                    val jsonData = gson.toJson(exportData)
                    
                    // Write to file
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(jsonData)
                        }
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Data berhasil diekspor!",
                    isError = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Gagal mengekspor data: ${e.message}",
                    isError = true
                )
            }
        }
    }
    
    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    loadingMessage = "Mengimpor data..."
                )
                
                withContext(Dispatchers.IO) {
                    val database = AppDatabase.getDatabase(context)
                    val studentDao = database.studentDao()
                    val attendanceDao = database.attendanceDao()
                    
                    // Read JSON data from file
                    val jsonData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            reader.readText()
                        }
                    } ?: throw Exception("Tidak dapat membaca file")
                    
                    // Parse JSON
                    val gson = Gson()
                    val exportData = gson.fromJson(jsonData, ExportData::class.java)
                    
                    // Import students - menggunakan studentCode untuk overwrite
                    var newStudentsCount = 0
                    var updatedStudentsCount = 0
                    
                    for (student in exportData.students) {
                        val existingStudent = studentDao.getStudentByCode(student.studentCode)
                        if (existingStudent == null) {
                            // New student - add it
                            studentDao.insertStudent(student)
                            newStudentsCount++
                        } else {
                            // Existing student - update it (overwrite)
                            studentDao.updateStudent(student)
                            updatedStudentsCount++
                        }
                    }
                    
                    // Import attendances - menggunakan studentCode untuk overwrite
                    var newAttendancesCount = 0
                    var updatedAttendancesCount = 0
                    
                    for (attendance in exportData.attendances) {
                        val existingAttendance = attendanceDao.getAttendanceById(attendance.studentCode, attendance.date)
                        if (existingAttendance == null) {
                            // New attendance - add it
                            attendanceDao.insertAttendance(attendance)
                            newAttendancesCount++
                        } else {
                            // Existing attendance - update it (overwrite)
                            attendanceDao.insertOrUpdateAttendance(attendance)
                            updatedAttendancesCount++
                        }
                    }
                    
                    // Create summary message
                    val summary = buildString {
                        append("Data berhasil diimpor!\n\n")
                        append("Siswa: $newStudentsCount baru, $updatedStudentsCount diperbarui\n")
                        append("Kehadiran: $newAttendancesCount baru, $updatedAttendancesCount diperbarui")
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = summary,
                        isError = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Gagal mengimpor data: ${e.message}",
                    isError = true
                )
            }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
} 
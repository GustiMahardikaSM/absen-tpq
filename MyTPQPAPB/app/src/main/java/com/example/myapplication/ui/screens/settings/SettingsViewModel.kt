package com.example.myapplication.ui.screens.settings

// Import yang diperlukan untuk operasi Android, URI, ViewModel, Coroutine, DB, dan JSON
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

// Data class untuk merepresentasikan state UI pada layar Settings
data class SettingsUiState(
    val isLoading: Boolean = false,      // apakah sedang memproses (import/export)
    val loadingMessage: String? = null,  // pesan yang ditampilkan saat loading
    val message: String? = null,         // pesan hasil operasi (sukses/gagal)
    val isError: Boolean = false         // flag jika terjadi error
)

// Data class yang menggambungkan data yang akan diekspor / diimpor
data class ExportData(
    val students: List<Student>,         // daftar siswa
    val attendances: List<Attendance>,   // daftar kehadiran
    val exportTimestamp: Long = System.currentTimeMillis() // waktu ekspor
)

// ViewModel untuk fitur pengaturan (termasuk import/export data)
class SettingsViewModel : ViewModel() {
    
    // StateFlow untuk menampung UI state yang dapat di-observe oleh UI
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    // Fungsi untuk mengekspor data ke file (URI diberikan oleh sistem/Storage Access Framework)
    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                // Set state menjadi loading dan tampilkan pesan
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    loadingMessage = "Mengekspor data..."
                )
                
                // Operasi I/O dilakukan di Dispatcher IO untuk tidak memblok UI thread
                withContext(Dispatchers.IO) {
                    // Dapatkan instance database dan DAO
                    val database = AppDatabase.getDatabase(context)
                    val studentDao = database.studentDao()
                    val attendanceDao = database.attendanceDao()
                    
                    // Ambil semua data siswa dan kehadiran dari database
                    val students = studentDao.getAllStudentsList()
                    val attendances = attendanceDao.getAllAttendances()
                    
                    // Bungkus data ke dalam objek ExportData
                    val exportData = ExportData(
                        students = students,
                        attendances = attendances
                    )
                    
                    // Konversi objek menjadi JSON menggunakan Gson
                    val gson = Gson()
                    val jsonData = gson.toJson(exportData)
                    
                    // Tulis JSON ke output stream yang diberikan oleh URI (file tujuan)
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(jsonData)
                        }
                    }
                }
                
                // Jika berhasil, update UI state dengan pesan sukses
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Data berhasil diekspor!",
                    isError = false
                )
                
            } catch (e: Exception) {
                // Jika terjadi error, simpan pesan error ke UI state
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Gagal mengekspor data: ${e.message}",
                    isError = true
                )
            }
        }
    }
    
    // Fungsi untuk mengimpor data dari file (URI diberikan oleh sistem/Storage Access Framework)
    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                // Set state menjadi loading dan tampilkan pesan
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    loadingMessage = "Mengimpor data..."
                )
                
                // Operasi I/O dan DB dilakukan di Dispatcher IO
                withContext(Dispatchers.IO) {
                    // Dapatkan instance database dan DAO
                    val database = AppDatabase.getDatabase(context)
                    val studentDao = database.studentDao()
                    val attendanceDao = database.attendanceDao()
                    
                    // Baca seluruh JSON dari file tujuan (URI)
                    val jsonData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            reader.readText() // baca keseluruhan text JSON
                        }
                    } ?: throw Exception("Tidak dapat membaca file")
                    
                    // Parse JSON menjadi objek ExportData menggunakan Gson
                    val gson = Gson()
                    val exportData = gson.fromJson(jsonData, ExportData::class.java)
                    
                    // Import siswa:
                    // - gunakan studentCode sebagai acuan untuk menimpa atau menambah
                    var newStudentsCount = 0
                    var updatedStudentsCount = 0
                    
                    for (student in exportData.students) {
                        // Cari apakah siswa sudah ada berdasarkan studentCode
                        val existingStudent = studentDao.getStudentByCode(student.studentCode)
                        if (existingStudent == null) {
                            // Jika tidak ada, insert sebagai siswa baru
                            studentDao.insertStudent(student)
                            newStudentsCount++
                        } else {
                            // Jika ada, update data siswa (overwrite)
                            studentDao.updateStudent(student)
                            updatedStudentsCount++
                        }
                    }
                    
                    // Import kehadiran:
                    // - gunakan kombinasi studentCode + date untuk menemukan entri yang sama
                    var newAttendancesCount = 0
                    var updatedAttendancesCount = 0
                    
                    for (attendance in exportData.attendances) {
                        // Cek apakah entri kehadiran sudah ada
                        val existingAttendance = attendanceDao.getAttendanceById(attendance.studentCode, attendance.date)
                        if (existingAttendance == null) {
                            // Jika tidak ada, insert sebagai entri baru
                            attendanceDao.insertAttendance(attendance)
                            newAttendancesCount++
                        } else {
                            // Jika ada, insertOrUpdate (atau overwrite) sesuai implementasi DAO
                            attendanceDao.insertOrUpdateAttendance(attendance)
                            updatedAttendancesCount++
                        }
                    }
                    
                    // Buat ringkasan hasil import untuk ditampilkan ke pengguna
                    val summary = buildString {
                        append("Data berhasil diimpor!\n\n")
                        append("Siswa: $newStudentsCount baru, $updatedStudentsCount diperbarui\n")
                        append("Kehadiran: $newAttendancesCount baru, $updatedAttendancesCount diperbarui")
                    }
                    
                    // Update UI state dengan ringkasan dan hentikan loading
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = summary,
                        isError = false
                    )
                }
                
            } catch (e: Exception) {
                // Jika terjadi error saat import, update UI state dengan pesan error
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Gagal mengimpor data: ${e.message}",
                    isError = true
                )
            }
        }
    }
    
    // Fungsi utilitas untuk menghapus pesan (digunakan UI untuk clear notifikasi)
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
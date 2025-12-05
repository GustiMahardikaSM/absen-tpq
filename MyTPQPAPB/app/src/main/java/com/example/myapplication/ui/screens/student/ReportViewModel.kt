package com.example.myapplication.ui.screens.student
// Paket tempat file ini berada, sesuai struktur aplikasi

import android.app.Application
// Import untuk kelas Application Android
import androidx.lifecycle.AndroidViewModel
// Import untuk AndroidViewModel agar ViewModel memiliki akses ke Application
import androidx.lifecycle.viewModelScope
// Import untuk coroutine scope yang terkait dengan ViewModel
import com.example.myapplication.data.AppDatabase
// Import untuk mendapatkan instance database aplikasi
import kotlinx.coroutines.flow.MutableStateFlow
// Import untuk StateFlow yang dapat diubah (untuk menyimpan state)
import kotlinx.coroutines.flow.StateFlow
// Import untuk tipe StateFlow publik (immutable)
import kotlinx.coroutines.flow.asStateFlow
// Import untuk mengonversi MutableStateFlow menjadi StateFlow publik
import kotlinx.coroutines.launch
// Import untuk meluncurkan coroutine
import java.time.LocalDate
// Import untuk manipulasi tanggal lokal (java.time)
import java.time.ZoneId
// Import untuk zona waktu sistem
import kotlinx.coroutines.flow.first
// Import ekstensi untuk mengambil nilai pertama dari Flow

// ViewModel untuk layar laporan siswa, mewarisi AndroidViewModel agar bisa akses Application
class ReportViewModel(application: Application) : AndroidViewModel(application) {
    // Mengambil instance database aplikasi
    private val database = AppDatabase.getDatabase(application)
    // DAO untuk operasi pada tabel siswa
    private val studentDao = database.studentDao()
    // DAO untuk operasi pada tabel absensi
    private val attendanceDao = database.attendanceDao()

    // StateFlow internal yang menampung list laporan siswa (dapat diubah)
    private val _reportList = MutableStateFlow<List<StudentReportItem>>(emptyList())
    // StateFlow publik yang bisa dibaca oleh UI (immutable)
    val reportList: StateFlow<List<StudentReportItem>> = _reportList.asStateFlow()

    // StateFlow internal untuk status loading awal
    private val _isLoading = MutableStateFlow(true)
    // StateFlow publik untuk status loading
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // StateFlow internal untuk status refresh (mis. swipe to refresh)
    private val _isRefreshing = MutableStateFlow(false)
    // StateFlow publik untuk status refresh
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Inisialisasi ViewModel: langsung muat laporan saat ViewModel dibuat
    init {
        loadReport()
    }

    // Fungsi untuk memuat data laporan siswa secara asynchronous
    private fun loadReport() {
        viewModelScope.launch {
            // Set indikator loading true saat mulai memuat
            _isLoading.value = true
            try {
                // Ambil seluruh siswa dari database (Flow -> ambil nilai pertama)
                val students = studentDao.getAllStudents().first()
                // Hitung rentang tanggal 30 hari terakhir (hari ini sebagai akhir)
                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(29)
                // Konversi tanggal mulai ke millisecond (awal hari)
                val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                // Konversi tanggal akhir ke millisecond (awal hari)
                val endMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                // Buat daftar laporan untuk setiap siswa dengan menghitung jumlah hadir di rentang waktu
                val report = students.map { student ->
                    // Hitung jumlah kehadiran siswa dalam rentang waktu menggunakan DAO
                    val presentCount = attendanceDao.getAttendanceCountInRange(student.studentCode, startMillis, endMillis)
                    // Bentuk objek laporan per siswa
                    StudentReportItem(
                        studentCode = student.studentCode,
                        studentName = student.name,
                        presentCount = presentCount
                    )
                }
                // Update StateFlow dengan hasil laporan yang sudah dibuat
                _reportList.value = report
            } finally {
                // Matikan indikator loading setelah proses selesai atau terjadi error
                _isLoading.value = false
            }
        }
    }

    // Fungsi publik untuk melakukan refresh manual (mis. pull-to-refresh)
    fun refresh() {
        viewModelScope.launch {
            // Tandai sedang refresh
            _isRefreshing.value = true
            // Panggil ulang pemuatan laporan
            loadReport()
            // Matikan indikator refresh setelah selesai
            _isRefreshing.value = false
        }
    }
}

// Data class yang merepresentasikan satu baris laporan siswa
data class StudentReportItem(
    val studentCode: String, // Kode unik siswa
    val studentName: String, // Nama siswa
    val presentCount: Int    // Jumlah kehadiran di rentang waktu yang dihitung
)
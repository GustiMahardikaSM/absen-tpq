package com.example.myapplication.ui.screens.student
// paket tempat ViewModel berada

import android.app.Application
// kelas Application Android, diperlukan untuk AndroidViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
// impor dari Jetpack Compose untuk state yang dapat di-observe

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
// impor dari Lifecycle: AndroidViewModel (butuh Application), SavedStateHandle untuk state restore, viewModelScope untuk coroutine

import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Student
// impor kelas database dan entitas Student dari modul data aplikasi

import kotlinx.coroutines.launch
// untuk menjalankan coroutine di viewModelScope

import java.text.SimpleDateFormat
import java.util.Date
// untuk membuat kode unik berdasarkan waktu (timestamp)

class AddEditStudentViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    // ViewModel untuk layar tambah/edit siswa; mewarisi AndroidViewModel agar dapat mengakses Application

    private val database = AppDatabase.getDatabase(application)
    // inisialisasi instance database (singleton), menggunakan Application sebagai context

    private val studentDao = database.studentDao()
    // mendapatkan DAO untuk operasi CRUD pada Student

    private val studentCode: String? = savedStateHandle["studentCode"]
    // mengambil kode siswa dari SavedStateHandle jika sedang edit; null jika tambah baru

    var studentName by mutableStateOf("")
        private set
    // state untuk nama siswa, diobserbasi oleh UI; hanya ViewModel yang bisa set

    var isLoading by mutableStateOf(true)
        private set
    // state untuk menandakan apakah data masih dimuat (loading) atau sudah siap

    var gender by mutableStateOf("")
        private set
    // state untuk jenis kelamin siswa

    var birthDate by mutableStateOf<Long?>(null)
        private set
    // state untuk tanggal lahir siswa dalam epoch millis; nullable jika kosong

    var positionType by mutableStateOf("Iqro") // default ke Iqro
        private set
    // state untuk tipe posisi (mis. "Iqro" atau "Quran"); default "Iqro"

    var iqroNumber by mutableStateOf(1) // default 1
        private set
    // state untuk nomor iqro (jika positionType = "Iqro"); default 1

    var iqroPage by mutableStateOf(1) // default 1
        private set
    // state untuk halaman iqro (jika positionType = "Iqro"); default 1

    var quranSurah by mutableStateOf(1)
        private set
    // state untuk surah Al-Quran (jika positionType = "Quran"); default 1

    var quranAyat by mutableStateOf(1)
        private set
    // state untuk ayat Al-Quran (jika positionType = "Quran"); default 1

    init {
        // inisialisasi: jika ada studentCode maka ambil data dari DB untuk edit, kalau tidak set nilai default
        if (studentCode != null) {
            viewModelScope.launch {
                // jalankan query DB di coroutine (background)
                studentDao.getStudentByCode(studentCode)?.let { student ->
                    // jika ditemukan data siswa, isi state dengan data tersebut
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
                // setelah data dimuat, set loading false
            }
        } else {
            // kasus tambah baru: set nilai default
            iqroPage = 1
            iqroNumber = 1
            positionType = "Iqro"
            isLoading = false
        }
    }

    fun onNameChange(newName: String) {
        studentName = newName
    }
    // pemanggilan dari UI saat nama berubah

    fun onGenderChange(newGender: String) { gender = newGender }
    // panggilan UI untuk update gender

    fun onBirthDateChange(newDate: Long?) { birthDate = newDate }
    // panggilan UI untuk update tanggal lahir (epoch millis atau null)

    fun onPositionTypeChange(newType: String) { positionType = newType }
    // panggilan UI untuk mengubah tipe posisi (Iqro/Quran)

    fun onIqroNumberChange(newNum: Int) { iqroNumber = newNum }
    // update nomor iqro dari UI

    fun onIqroPageChange(newPage: Int?) { iqroPage = newPage ?: 1 }
    // update halaman iqro; jika null fallback ke 1

    fun onQuranSurahChange(newSurah: Int) { quranSurah = newSurah }
    // update nomor surah dari UI

    fun onQuranAyatChange(newAyat: Int) { quranAyat = newAyat }
    // update nomor ayat dari UI

    suspend fun saveStudent(): Boolean {
        // menyimpan (insert/update) data siswa ke database; mengembalikan true jika sukses
        if (studentName.isBlank()) return false
        // validasi sederhana: nama tidak boleh kosong

        return try {
            val now = System.currentTimeMillis()
            // waktu sekarang untuk membuat kode unik jika perlu

            val code = if (studentCode == null) {
                // jika tambah baru, buat code unik berdasarkan timestamp (format yyMMddHHmmss)
                SimpleDateFormat("yyMMddHHmmss").format(Date(now))
            } else studentCode
            // jika edit, pakai kode yang sudah ada

            val student = Student(
                studentCode = code,
                name = studentName,
                gender = gender,
                birthDate = birthDate,
                positionType = positionType,
                // jika posisi Iqro, simpan nomor dan halaman; selain itu biarkan null
                iqroNumber = if (positionType == "Iqro") (iqroNumber.takeIf { it > 0 } ?: 1) else null,
                iqroPage = if (positionType == "Iqro") (iqroPage.takeIf { it > 0 } ?: 1) else null,
                // jika posisi Quran, simpan surah dan ayat; selain itu biarkan null
                quranSurah = if (positionType == "Quran") quranSurah else null,
                quranAyat = if (positionType == "Quran") quranAyat else null
            )
            studentDao.insertStudent(student)
            // simpan ke database (insert atau replace, tergantung implementasi DAO)
            true
        } catch (e: Exception) {
            // jika ada kesalahan saat menyimpan, kembalikan false (bisa ditingkatkan dengan logging)
            false
        }
    }
}
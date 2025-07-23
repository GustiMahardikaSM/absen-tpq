package com.example.myapplication.ui.screens.student

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Student
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StudentReportViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val studentCode: String = checkNotNull(savedStateHandle["studentCode"])
    private val database = AppDatabase.getDatabase(application)
    private val studentDao = database.studentDao()
    private val attendanceDao = database.attendanceDao()

    private val _student = MutableStateFlow<Student?>(null)
    val student = _student.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _reportDetail = MutableStateFlow<StudentReportDetail?>(null)
    val reportDetail = _reportDetail.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val student = studentDao.getStudentByCode(studentCode)
                _student.value = student

                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(29)
                val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                // Attendance detail
                val attendanceList = attendanceDao.getAttendanceByStudentAndDateRange(studentCode, startMillis, endMillis).first()
                val presentCount = attendanceList.count { it.isPresent }
                val totalPassed = attendanceDao.getTotalPassedInRange(studentCode, startMillis, endMillis)
                val totalRetake = attendanceDao.getTotalRetakeInRange(studentCode, startMillis, endMillis)

                // Perkembangan bacaan
                val sortedAttendance = attendanceList.filter { it.isPresent }.sortedBy { it.date }
                val awal = sortedAttendance.firstOrNull()
                // Cari absen terakhir yang valid untuk Quran
                val akhirQuran = sortedAttendance.lastOrNull { it.quranSurah != null && it.quranSurah > 0 && it.quranAyat != null && it.quranAyat > 0 }
                // Cari absen terakhir yang valid untuk Iqro
                val akhirIqro = sortedAttendance.lastOrNull { it.iqroNumber != null && it.iqroNumber > 0 && it.iqroPage != null && it.iqroPage > 0 }
                // Cari absen terakhir yang valid apapun (Quran/Iqro)
                val akhirValid = sortedAttendance.lastOrNull { (it.quranSurah != null && it.quranSurah > 0 && it.quranAyat != null && it.quranAyat > 0) || (it.iqroNumber != null && it.iqroNumber > 0 && it.iqroPage != null && it.iqroPage > 0) }
                // Pilih absen terakhir yang valid (Quran lebih prioritas jika ada, lalu Iqro, lalu fallback ke valid apapun)
                val akhir = akhirQuran ?: akhirIqro ?: akhirValid
                val awalBacaan = awal?.let { getReadingPosition(it) } ?: "-"
                val akhirBacaan = akhir?.let { getReadingPosition(it) } ?: "-"
                val readingSummary = if (awalBacaan == akhirBacaan) awalBacaan else "$awalBacaan → $akhirBacaan"

                // Laporan harian (hanya hari hadir)
                val dailyReports = sortedAttendance.map {
                    DailyReportDetail(
                        date = java.time.Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate(),
                        reading = getReadingPosition(it),
                        status = if (it.isPassed == true) "Lulus" else if (it.isPassed == false) "Mengulang" else "-",
                        note = it.catatanGuru ?: ""
                    )
                }.reversed() // terbaru di atas

                val readingTypeSummary = when {
                    akhir?.quranSurah != null && akhir.quranAyat != null -> "Al Quran"
                    akhir?.iqroNumber != null -> "Iqro"
                    else -> "-"
                }

                _reportDetail.value = StudentReportDetail(
                    name = student?.name ?: "-",
                    level = student?.positionType ?: "-",
                    birthDate = student?.birthDate?.let { formatBirthDate(it) } ?: "-",
                    gender = student?.gender ?: "-",
                    attendanceCount = presentCount,
                    startReading = awalBacaan,
                    currentReading = akhirBacaan,
                    readingSummary = readingSummary,
                    readingTypeSummary = readingTypeSummary,
                    totalPassed = totalPassed,
                    totalRetake = totalRetake,
                    dailyReports = dailyReports,
                    generatedAt = formatToday()
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getReadingPosition(att: com.example.myapplication.data.Attendance): String {
        return when {
            att.quranSurah != null && att.quranSurah > 0 && att.quranAyat != null && att.quranAyat > 0 -> "Al Quran: Surat ${getSurahName(att.quranSurah)} ayat ${att.quranAyat}"
            att.iqroNumber != null && att.iqroNumber > 0 && att.iqroPage != null && att.iqroPage > 0 -> "Iqro ${att.iqroNumber} Hal ${att.iqroPage}"
            else -> "-"
        }
    }

    private fun getSurahName(index: Int): String {
        val surahList = listOf(
            "Al-Fatihah", "Al-Baqarah", "Ali-Imran", "An-Nisa", "Al-Maidah", "Al-Anam", "Al-Araf", "Al-Anfal", "At-Taubah", "Yunus", "Hud", "Yusuf", "Ar-Rad", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra", "Al-Kahgi", "Maryam", "Ta Ha", "Al-Anbiya", "Al-Hajj", "Al-Muminun", "An-Nur", "Al-Furqan", "Asy-Syuara", "An-Naml", "Al-Qasas", "Al-Ankabut", "Ar-Ruum", "Luqman", "As-Sajdah", "Al-Ahzab", "Saba", "Fatir", "Ya-Sin", "Ash-Shaffat", "Shad", "Az-Zumar", "Gafir", "Fushshilat", "Asy-Syura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jatsiyah", "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat", "Qaf", "Adz-Dzariyat", "Ath-Thuur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqiah", "Al-Hadid", "Al-Mujadilah", "Al-Hasyr", "Al-Mumtahanah", "Ash-Shaf", "Al-Jumuah", "Al-Munafiqun", "At-Taghabun", "Ath-Thalaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Maarij", "Nuh", "Al-Jin", "Al-Muzammil", "Al-Muddatstsir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba", "An-Naziat", "Abasa", "At-Takwir", "Al-Infithar", "Al-Muthaffifin", "Al-Inshiqaq", "Al-Buruj", "Ath-Thariq", "Al-Alaa", "Al-Ghasyiyah", "Al-Fajr", "Al-Balad", "Asy-Syams", "Al-Lail", "Adh-Dhuha", "Al-Inshirah", "At-Tin", "Al-Alaq", "Al-Qadr", "Al-Bayyinah", "Al-Zalzalah", "Al-Adiyat", "Al-Qariah", "At-Takatsur", "Al-Ashr", "Al-Humazah", "Al-Fil", "Quraysh", "Al-Maun", "Al-Kautsar", "Al-Kafirun", "An-Nashr", "Al-Lahab", "Al-Ikhlas", "Al-Falaq", "An-Nas"
        )
        return surahList.getOrNull(index - 1) ?: "-"
    }

    private fun formatBirthDate(millis: Long): String {
        val date = java.time.Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("id"))
        val month = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("id"))
        return "$dayOfWeek, ${date.dayOfMonth} $month ${date.year}"
    }

    private fun formatToday(): String {
        val today = LocalDate.now()
        return "${today.dayOfMonth}/${today.monthValue}/${today.year}"
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadData()
            _isRefreshing.value = false
        }
    }
}

// Data class untuk laporan detail

data class StudentReportDetail(
    val name: String,
    val level: String,
    val birthDate: String,
    val gender: String,
    val attendanceCount: Int,
    val startReading: String,
    val currentReading: String,
    val readingSummary: String,
    val readingTypeSummary: String,
    val totalPassed: Int,
    val totalRetake: Int,
    val dailyReports: List<DailyReportDetail>,
    val generatedAt: String
)

data class DailyReportDetail(
    val date: LocalDate,
    val reading: String,
    val status: String,
    val note: String
) 
package com.example.myapplication.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.Student
import com.example.myapplication.data.Attendance
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.theme.IslamicTopAppBar
import com.example.myapplication.ui.theme.IslamicFloatingActionButton
import com.example.myapplication.ui.theme.IslamicSearchBar
import com.example.myapplication.ui.theme.IslamicEmptyState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

/**
 * StudentListScreen - Halaman daftar siswa/santri TPQ
 * Menampilkan list siswa dengan fitur pencarian, refresh, dan aksi CRUD
 * @param onAddStudent Callback untuk navigasi ke form tambah siswa
 * @param onEditStudent Callback untuk navigasi ke form edit siswa
 * @param onNavigateToAttendance Callback untuk navigasi ke halaman absensi
 * @param onViewStudentReport Callback untuk navigasi ke laporan siswa
 * @param viewModel ViewModel untuk mengelola data siswa
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    onAddStudent: () -> Unit,
    onEditStudent: (String) -> Unit,
    onNavigateToAttendance: () -> Unit,
    onViewStudentReport: (String) -> Unit,
    viewModel: StudentListViewModel = viewModel()
) {
    // State management untuk data siswa dan UI
    val students by viewModel.students.collectAsState()
    val latestAttendanceMap by viewModel.latestAttendanceMap.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val filteredStudents = if (searchQuery.isBlank()) students else students.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val isRefreshing by viewModel.isRefreshing.collectAsState(initial = false)
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    Scaffold(
        // TopAppBar dengan tema Islami
        topBar = {
            IslamicTopAppBar("Daftar Santri TPQ")
        },
        // FloatingActionButton untuk tambah siswa
        floatingActionButton = {
            IslamicFloatingActionButton(
                onClick = onAddStudent,
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Tambah Santri",
                        modifier = Modifier.size(28.dp)
                    )
            }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(IvoryWhite, CreamWhite) // Gradien background Islami
                    )
                )
                .padding(padding)
        ) {
            // Search Bar dengan desain Islami
            IslamicSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Cari Nama Santri...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // SwipeRefresh untuk pull-to-refresh
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { viewModel.refresh() }
            ) {
                if (filteredStudents.isEmpty()) {
                    // Empty state ketika tidak ada siswa
                    IslamicEmptyState(
                        icon = "\uD83D\uDCD6", // Emoji buku
                        title = "Belum ada santri terdaftar",
                        subtitle = "Mulai dengan menambahkan santri baru"
                    )
                } else {
                    // List siswa dengan LazyColumn
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredStudents) { student ->
                            IslamicStudentItem(
                                student = student,
                                latestAttendance = latestAttendanceMap[student.studentCode],
                                onEdit = { onEditStudent(student.studentCode) },
                                onDelete = { viewModel.deleteStudent(student) },
                                onViewReport = { onViewStudentReport(student.studentCode) }
                            )
                        }
                    }
                }
            }
        }
    }
}



/**
 * IslamicStudentItem - Komponen card untuk menampilkan data siswa
 * Menggunakan desain Islami dengan gradien berdasarkan gender
 * @param student Data siswa yang akan ditampilkan
 * @param latestAttendance Data kehadiran terakhir siswa
 * @param onEdit Callback untuk edit siswa
 * @param onDelete Callback untuk hapus siswa
 * @param onViewReport Callback untuk lihat laporan siswa
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IslamicStudentItem(
    student: Student,
    latestAttendance: Attendance?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewReport: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Warna background berdasarkan gender dengan gradien
    val bgColor = when (student.gender) {
        "Laki-laki" -> Brush.linearGradient(
            colors = listOf(BoyBlue, BoyBlue.copy(alpha = 0.8f)) // Gradien biru untuk laki-laki
        )
        "Perempuan" -> Brush.linearGradient(
            colors = listOf(GirlPink, GirlPink.copy(alpha = 0.8f)) // Gradien pink untuk perempuan
        )
        else -> Brush.linearGradient(
            colors = listOf(CreamWhite, Color.White) // Default gradien putih
        )
    }

    // Info bacaan - List nama surah Al-Quran
    val surahList = listOf(
        "Al-Fatihah", "Al-Baqarah", "Ali-Imran", "An-Nisa", "Al-Maidah", "Al-Anam", "Al-Araf", "Al-Anfal", "At-Taubah", "Yunus", "Hud", "Yusuf", "Ar-Rad", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra", "Al-Kahfi", "Maryam", "Ta Ha", "Al-Anbiya", "Al-Hajj", "Al-Muminun", "An-Nur", "Al-Furqan", "Asy-Syuara", "An-Naml", "Al-Qasas", "Al-Ankabut", "Ar-Ruum", "Luqman", "As-Sajdah", "Al-Ahzab", "Saba", "Fatir", "Ya-Sin", "Ash-Shaffat", "Shad", "Az-Zumar", "Gafir", "Fushshilat", "Asy-Syura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jatsiyah", "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat", "Qaf", "Adz-Dzariyat", "Ath-Thuur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqiah", "Al-Hadid", "Al-Mujadilah", "Al-Hasyr", "Al-Mumtahanah", "Ash-Shaf", "Al-Jumuah", "Al-Munafiqun", "At-Taghabun", "Ath-Thalaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Maarij", "Nuh", "Al-Jin", "Al-Muzammil", "Al-Muddatstsir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba", "An-Naziat", "Abasa", "At-Takwir", "Al-Infithar", "Al-Muthaffifin", "Al-Inshiqaq", "Al-Buruj", "Ath-Thariq", "Al-Alaa", "Al-Ghasyiyah", "Al-Fajr", "Al-Balad", "Asy-Syams", "Al-Lail", "Adh-Dhuha", "Al-Inshirah", "At-Tin", "Al-Alaq", "Al-Qadr", "Al-Bayyinah", "Al-Zalzalah", "Al-Adiyat", "Al-Qariah", "At-Takatsur", "Al-Ashr", "Al-Humazah", "Al-Fil", "Quraysh", "Al-Maun", "Al-Kautsar", "Al-Kafirun", "An-Nashr", "Al-Lahab", "Al-Ikhlas", "Al-Falaq", "An-Nas"
    )
    
    // Menentukan info bacaan berdasarkan data terbaru atau data siswa
    val bacaan = when {
        // Prioritas: data dari kehadiran terakhir (Quran)
        latestAttendance != null && latestAttendance.quranSurah != null && latestAttendance.quranSurah > 0 && latestAttendance.quranAyat != null && latestAttendance.quranAyat > 0 ->
            "🕌 Surah ${surahList.getOrNull(latestAttendance.quranSurah-1) ?: "-"} Ayat ${latestAttendance.quranAyat}"
        // Prioritas: data dari kehadiran terakhir (Iqro)
        latestAttendance != null && latestAttendance.iqroNumber != null && latestAttendance.iqroNumber > 0 && latestAttendance.iqroPage != null && latestAttendance.iqroPage > 0 ->
            if (latestAttendance.iqroNumber == 0) "📖 Iqro / Qiroati Pra-TK Halaman ${latestAttendance.iqroPage}" else "📖 Iqro / Qiroati ${latestAttendance.iqroNumber} Halaman ${latestAttendance.iqroPage}"
        // Fallback: data dari profil siswa (Iqro)
        student.positionType == "Iqro" && student.iqroNumber != null && student.iqroPage != null ->
            if (student.iqroNumber == 0) "📖 Iqro / Qiroati Pra-TK Halaman ${student.iqroPage}" else "📖 Iqro / Qiroati ${student.iqroNumber} Halaman ${student.iqroPage}"
        // Fallback: data dari profil siswa (Quran)
        student.positionType == "Quran" && student.quranSurah != null && student.quranAyat != null ->
            "🕌 Surah ${surahList.getOrNull(student.quranSurah-1) ?: "-"} Ayat ${student.quranAyat}"
        else -> "-"
    }

    // Menghitung umur siswa berdasarkan tanggal lahir
    val umur = student.birthDate?.let {
        val now = java.util.Calendar.getInstance()
        val birth = java.util.Calendar.getInstance().apply { timeInMillis = it }
        var tahun = now.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
        var bulan = now.get(java.util.Calendar.MONTH) - birth.get(java.util.Calendar.MONTH)
        if (bulan < 0) {
            tahun--
            bulan += 12
        }
        "👤 Umur: $tahun tahun $bulan bulan"
    } ?: "👤 Umur: -"

    // Dialog konfirmasi hapus dengan tema Islami
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Text(
                    "Konfirmasi Hapus",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen // Warna hijau untuk judul
                    )
                ) 
            },
            text = { 
                Text(
                    "Apakah Anda yakin ingin menghapus santri ${student.name}?",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        "Ya, Hapus",
                        color = Color.Red, // Warna merah untuk konfirmasi hapus
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        "Batal",
                        color = IslamicGreen, // Warna hijau untuk batal
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            containerColor = Color.White, // Background putih
            shape = RoundedCornerShape(16.dp) // Border radius yang lembut
        )
    }

    // Card utama untuk menampilkan data siswa
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewReport), // Klik untuk lihat laporan
        shape = RoundedCornerShape(16.dp), // Border radius yang lembut
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Shadow untuk efek floating
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Transparent untuk menampilkan gradien
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor, RoundedCornerShape(16.dp)) // Background dengan gradien berdasarkan gender
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Header dengan nama siswa dan tombol aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                    // Nama siswa dengan style bold
                    Text(
                        text = student.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkGreen // Warna hijau tua untuk nama
                            )
                    )
                    // ID siswa jika ada
                    if (!student.studentCode.isNullOrBlank()) {
                        Text(
                                text = "🆔 ID: ${student.studentCode}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = IslamicGreen.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium
                                )
                        )
                    }
                }
                    
                    // Tombol aksi (Edit dan Hapus)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Tombol Edit dengan background hijau transparan
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = IslamicGreen.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = IslamicGreen,
                                modifier = Modifier.size(20.dp)
                            )
                    }
                        // Tombol Hapus dengan background merah transparan
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color.Red.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                    }
                }
            }
                
                Spacer(Modifier.height(12.dp))
                
                // Info bacaan dengan emoji
                Text(
                    text = bacaan,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DarkGreen,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Spacer(Modifier.height(4.dp))
                
                // Info umur dengan emoji
                Text(
                    text = umur,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = IslamicGreen.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
} 
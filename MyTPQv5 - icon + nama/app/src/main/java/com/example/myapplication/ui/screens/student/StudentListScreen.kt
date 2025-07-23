package com.example.myapplication.ui.screens.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.Student
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    onAddStudent: () -> Unit,
    onEditStudent: (Long) -> Unit,
    onNavigateToAttendance: () -> Unit,
    onViewStudentReport: (Long) -> Unit,
    viewModel: StudentListViewModel = viewModel()
) {
    val students by viewModel.students.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val filteredStudents = if (searchQuery.isBlank()) students else students.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Siswa TPQ") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddStudent) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Siswa")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari Nama Siswa") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada siswa terdaftar")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(filteredStudents) { student ->
                        StudentItem(
                            student = student,
                            onEdit = { onEditStudent(student.id) },
                            onDelete = { viewModel.deleteStudent(student) },
                            onViewReport = { onViewStudentReport(student.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentItem(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewReport: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Warna background berdasarkan gender
    val bgColor = when (student.gender) {
        "Laki-laki" -> Color(0xFFD0E8FF) // biru muda
        "Perempuan" -> Color(0xFFFFD6E0) // pink muda
        else -> MaterialTheme.colorScheme.surface
    }

    // Info bacaan
    val surahList = listOf(
        "Al-Fatihah", "Al-Baqarah", "Ali-Imran", "An-Nisa", "Al-Maidah", "Al-Anam", "Al-Araf", "Al-Anfal", "At-Taubah", "Yunus", "Hud", "Yusuf", "Ar-Rad", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra", "Al-Kahfi", "Maryam", "Ta Ha", "Al-Anbiya", "Al-Hajj", "Al-Muminun", "An-Nur", "Al-Furqan", "Asy-Syuara", "An-Naml", "Al-Qasas", "Al-Ankabut", "Ar-Ruum", "Luqman", "As-Sajdah", "Al-Ahzab", "Saba", "Fatir", "Ya-Sin", "Ash-Shaffat", "Shad", "Az-Zumar", "Gafir", "Fushshilat", "Asy-Syura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jatsiyah", "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat", "Qaf", "Adz-Dzariyat", "Ath-Thuur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqiah", "Al-Hadid", "Al-Mujadilah", "Al-Hasyr", "Al-Mumtahanah", "Ash-Shaf", "Al-Jumuah", "Al-Munafiqun", "At-Taghabun", "Ath-Thalaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Maarij", "Nuh", "Al-Jin", "Al-Muzammil", "Al-Muddatstsir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba", "An-Naziat", "Abasa", "At-Takwir", "Al-Infithar", "Al-Muthaffifin", "Al-Inshiqaq", "Al-Buruj", "Ath-Thariq", "Al-Alaa", "Al-Ghasyiyah", "Al-Fajr", "Al-Balad", "Asy-Syams", "Al-Lail", "Adh-Dhuha", "Al-Inshirah", "At-Tin", "Al-Alaq", "Al-Qadr", "Al-Bayyinah", "Al-Zalzalah", "Al-Adiyat", "Al-Qariah", "At-Takatsur", "Al-Ashr", "Al-Humazah", "Al-Fil", "Quraysh", "Al-Maun", "Al-Kautsar", "Al-Kafirun", "An-Nashr", "Al-Lahab", "Al-Ikhlas", "Al-Falaq", "An-Nas"
    )
    val bacaan = when (student.positionType) {
        "Iqro" -> if (student.iqroNumber != null && student.iqroPage != null) {
            if (student.iqroNumber == 0) "Iqro / Qiroati Pra-TK Halaman ${student.iqroPage}" else "Iqro / Qiroati ${student.iqroNumber} Halaman ${student.iqroPage}"
        } else "-"
        "Quran" -> if (student.quranSurah != null && student.quranAyat != null) "Surah ${surahList.getOrNull(student.quranSurah-1) ?: "-"} Ayat ${student.quranAyat}" else "-"
        else -> "-"
    }

    // Info umur
    val umur = student.birthDate?.let {
        val now = java.util.Calendar.getInstance()
        val birth = java.util.Calendar.getInstance().apply { timeInMillis = it }
        var tahun = now.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
        var bulan = now.get(java.util.Calendar.MONTH) - birth.get(java.util.Calendar.MONTH)
        if (bulan < 0) {
            tahun--
            bulan += 12
        }
        "Umur: $tahun tahun $bulan bulan"
    } ?: "Umur: -"

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Konfirmasi") },
            text = { Text("Apakah Anda yakin ingin menghapus ${student.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Ya")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Tidak")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewReport),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (!student.studentCode.isNullOrBlank()) {
                        Text(
                            text = "ID: ${student.studentCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus")
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(bacaan, style = MaterialTheme.typography.bodySmall)
            Text(umur, style = MaterialTheme.typography.bodySmall)
        }
    }
} 